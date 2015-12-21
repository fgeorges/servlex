/****************************************************************************/
/*  File:       ServlexPipeline.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XPipeline;
import com.xmlcalabash.util.Input;
import com.xmlcalabash.util.Output;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import org.expath.pkg.calabash.PkgConfigurer;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.saxon.ConfigHelper;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.components.ComponentInstance;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.XdmConnector;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.saxon.model.SaxonSequence;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;

import static org.expath.servlex.processors.XProcProcessor.OUTPUT_PORT_NAME;
import org.expath.servlex.processors.saxon.model.SaxonDocument;
import org.expath.servlex.tools.Cleanable;
import org.expath.servlex.tools.Log;

/**
 * Abstract an XProc pipeline.
 *
 * @author Florent Georges
 */
public class CalabashPipeline
        implements Cleanable
{
    public CalabashPipeline(CalabashXProc calabash, ServerConfig config, Auditor auditor, Processors procs)
    {
        myCalabash = calabash;
        myConfig = config;
        myAuditor = auditor;
        myProcs = procs;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("calabash pipleline, close the runtime object");
        if ( myRuntime != null ) {
            myRuntime.close();
            myRuntime = null;
        }
    }

    /**
     * Prepare the compiled pipeline for evaluation.
     * 
     * TODO: After discussions with Norm, it seems this is the only correct
     * way to go currently.  XProcRuntime is NOT the Calabash equivalent of
     * Saxon's Processor.  It is, well, the runtime for one pipeline.  So
     * nothing is cacheable with Calabash, because there is no processor,
     * and compiled pipelines cannot be evaluated in a concurrent way (they
     * can be reused in a sequence way, by resetting the pipeline, but not
     * at the same time so not in a web server).  I still hope to have a
     * better design in a later version of Calabash.
     */
    public void compile(String pipe)
            throws ComponentError
                 , ServlexException
    {
        compile(pipe, null);
    }

    public void compile(XdmNode pipe)
            throws ComponentError
                 , ServlexException
    {
        compile(null, pipe);
    }

    private void compile(String href, XdmNode node)
            throws ComponentError
                 , ServlexException
    {
        myAuditor.compilationStarts("xproc");
        try {
            // instantiate the runtime
            myRuntime = getRuntime();
            // compile the pipeline
            if ( node == null ) {
                LOG.debug("About to href the pipeline: " + href);
                Input in = new Input(href);
                myCompiled = myRuntime.load(in);
            }
            else {
                LOG.debug("About to compile the pipeline document: " + node.getBaseURI());
                myCompiled = myRuntime.use(node);
            }
        }
        catch ( SaxonApiException ex ) {
            LOG.error("Error compiling pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
        catch ( PackageException ex ) {
            LOG.error("Error compiling pipeline", ex);
            throw new ServlexException(500, "Error compiling pipeline", ex);
        }
        catch ( TechnicalException ex ) {
            LOG.error("Error compiling pipeline", ex);
            throw new ServlexException(500, "Error compiling pipeline", ex);
        }
        myAuditor.compilationStops();
    }

    /**
     * Prepare a new runtime, not compiling any pipeline, not handling errors.
     */
    private XProcRuntime getRuntime()
            throws PackageException
                 , ServlexException
                 , TechnicalException
    {
        Processor saxon = myCalabash.getSaxon();
        XProcConfiguration xconf = new XProcConfiguration(saxon);
        // < temporary >
        temporary_FIX_BECAUSE_OF_CALABASH(xconf);
        XProcRuntime runtime = new SafeCloseXProcRuntime(xconf);
        // </ temporary >
        SaxonRepository repo = myCalabash.getRepository();
        PkgConfigurer configurer = new PkgConfigurer(runtime, repo.getUnderlyingRepo());
        runtime.setConfigurer(configurer);
        File profiling = myConfig.getProfileFile("xproc-profile");
        if ( profiling != null ) {
            try {
                OutputStream stream = new FileOutputStream(profiling);
                Output out = new Output(stream);
                runtime.setProfile(out);
            }
            catch ( FileNotFoundException ex ) {
                // there is no point in stopping processing if the profile
                // file is not writable
                LOG.error("Error opening the profile file for Calabash: " + profiling);
            }
        }
        // FIXME: Have to reconfigure the Saxon processor, because Calabash
        // install its own resolvers.  Should be ok though, but double-check!
        ConfigHelper helper = new ConfigHelper(repo);
        helper.config(saxon.getUnderlyingConfiguration());
        return runtime;
    }

    /**
     * Make sure that `super.close()` is never called twice (because it does not
     * prevent using `for (... : exFuncs) ...` when `exFuncs` is `null`.  Should
     * be fixed in Calabash itself.
     */
    private static class SafeCloseXProcRuntime
            extends XProcRuntime
    {
        public SafeCloseXProcRuntime(XProcConfiguration conf)
        {
            super(conf);
        }

        @Override
        public synchronized void close()
        {
            if ( ! myClosed ) {
                super.close();
                myClosed = true;
            }
        }

        private boolean myClosed = false;
    }

    // When instantiated with an existing Saxon processor, an XProcConfiguration
    // object is not loaded with the step implementations! (in Calabash 1.1.1,
    // using the new annotation-based system)  This function does exactly that,
    // based on the similar code in Calabash, and in the library it uses for
    // manipulating annotations.
    private void temporary_FIX_BECAUSE_OF_CALABASH(XProcConfiguration conf)
            throws TechnicalException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class annot = XMLCalabash.class;
        String idx = "META-INF/annotations/" + annot.getCanonicalName();
        InputStream in = cl.getResourceAsStream(idx);
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        try {
            String line = r.readLine();
            while ( line != null ) {
                Class<?> klass;
                try {
                    klass = cl.loadClass(line);
                }
                catch ( ClassNotFoundException ex ) {
                    throw new TechnicalException("Error loading the step class: " + line, ex);
                }
                XMLCalabash annotation = klass.getAnnotation(XMLCalabash.class);
                for ( String clarkName: annotation.type().split("\\s+") ) {
                    try {
                        QName name = QName.fromClarkName(clarkName);
                        LOG.debug("Found step type annotation: " + clarkName);
                        if ( conf.implementations.containsKey(name) ) {
                            LOG.debug("Ignoring step type annotation for configured step: " + clarkName);
                        }
                        conf.implementations.put(name, klass);
                    }
                    catch ( IllegalArgumentException ex ) {
                        throw new TechnicalException("Failed to parse step annotation type: " + clarkName, ex);
                    }
                }
                line = r.readLine();
            }
        }
        catch ( IOException ex ) {
            throw new TechnicalException("Error reading the annotation index file", ex);
        }
    }

    /**
     * ...
     * 
     * TODO: Will probably need a specific type of connector, like XProcConnector,
     * to be able to connect to another pipeline based on port names.  For now,
     * this supports only the case where it is called directly from Servlex (no
     * filter nor error handler in between, at least no pipelines, so no other
     * needs than passing through an XDM sequence).
     */
    public Connector evaluate(Connector connector)
            throws ComponentError
                 , ServlexException
    {
        ComponentInstance instance = new MyInstance(myCompiled, myProcs);
        connector.connectToPipeline(instance, myConfig);
        if ( LOG.debug()) {
            LOG.debug("Existing output ports: " + myCompiled.getOutputs());
            for ( String o : myCompiled.getOutputs() ) {
                LOG.debug("Existing output port: " + o);
            }
            LOG.debug("The pipeline: " + myCompiled);
            // LOG.debug("The Calabash processor: " + myCalabash);
            // LOG.debug("The Calabash config: " + config.getSaxon().getUnderlyingConfiguration());
            // LOG.debug("The URI resolver: " + config.getSaxon().getUnderlyingConfiguration().getURIResolver());
            // LOG.debug("The source resolver: " + config.getSaxon().getUnderlyingConfiguration().getSourceResolver());
        }
        // check before running
        if ( ! myCompiled.getOutputs().contains(OUTPUT_PORT_NAME) ) {
            throw new ServlexException(501, "The output port '" + OUTPUT_PORT_NAME + "' is mandatory on an XProc pipeline.");
        }
        try {
            myCompiled.run();
        }
        catch ( SaxonApiException ex ) {
            LOG.error("Error evaluating pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
        ReadablePipe response_port = myCompiled.readFrom(OUTPUT_PORT_NAME);
        try {
            XdmValue result  = decodeResponse(response_port);
            Sequence seq     = new SaxonSequence(result);
            Auditor  auditor = connector.getAuditor();
            return new XdmConnector(seq, auditor);
        }
        catch ( SaxonApiException ex ) {
            LOG.error("Error decoding the response whilst evaluating pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
    }

    /**
     * Create an XdmValue object from the 'response' port.
     *
     * <pre>
     * if sequence                   # sequence? then use it directly
     *   for item
     *     add item to result
     * else if wrapper               # web:wrapper? unwrap the sequence
     *   for unwrapped
     *     add item to result
     * else                          # a single doc, must be web:response
     *   add doc to result
     * </pre>
     */
    private static XdmValue decodeResponse(ReadablePipe port)
            throws SaxonApiException
                 , ServlexException
    {
        List<XdmItem> result = new ArrayList<XdmItem>();
        port.canReadSequence(true);
        // if there are more than 1 docs, the first one must be web:response,
        // and the following ones are the bodies
        int count = port.documentCount();
        if ( count == 0 ) {
            LOG.debug("The pipeline returned no document on '" + org.expath.servlex.processors.XProcProcessor.OUTPUT_PORT_NAME + "'.");
            // TODO: If there is no document on the port, we return an empty
            // sequence.  We should probably throw an error instead...
            return XdmEmptySequence.getInstance();
        }
        else if ( count > 1 ) {
            LOG.debug("The pipeline returned " + count + " documents on '" + org.expath.servlex.processors.XProcProcessor.OUTPUT_PORT_NAME + "'.");
            while ( port.moreDocuments() ) {
                XdmNode doc = port.read();
                addToList(result, doc);
            }
        }
        else {
            LOG.debug("The pipeline returned 1 document on '" + org.expath.servlex.processors.XProcProcessor.OUTPUT_PORT_NAME + "'.");
            XdmNode response = port.read();
            if ( LOG.debug()) {
                LOG.debug("Content of the outpot port '" + org.expath.servlex.processors.XProcProcessor.OUTPUT_PORT_NAME + "': " + response);
            }
            if ( response == null ) {
                // TODO: If there is no web:response, we return an empty sequence.
                // We should probably throw an error instead...
                return XdmEmptySequence.getInstance();
            }
            XdmNode wrapper_elem = getWrapperElem(response);
            // not a web:wrapper, so only one doc, so must be web:response
            if ( wrapper_elem == null ) {
                addToList(result, response);
            }
            // a web:wrapper, so unwrap the sequence
            else {
                XdmSequenceIterator it = wrapper_elem.axisIterator(Axis.CHILD);
                while ( it.hasNext() ) {
                    // TODO: FIXME: For now, due to some strange behaviour in
                    // Calabash, we ignore everything but elements (because it
                    // exposes the indentation as text nodes, which is wrong...)
                    XdmItem child = it.next();
                    if ( child instanceof XdmNode && ((XdmNode) child).getNodeKind() == XdmNodeKind.ELEMENT ) {
                        addToList(result, (XdmNode) child);
                    }
                }
            }
        }
        return new XdmValue(result);
    }

    private static void addToList(List<XdmItem> list, XdmNode node)
            throws ServlexException
    {
        if ( LOG.debug()) {
            // a document node
            if ( node.getNodeKind() == XdmNodeKind.DOCUMENT ) {
                XdmNode child = getDocElement(node);
                // without element children
                if ( child == null ) {
                    LOG.debug("Adding a document node without any element to the list");
                }
                // with an element child
                else {
                    LOG.debug("Adding a document node with child '" + child.getNodeName() + "' to the list");
                }
            }
            // any other kind of node
            else {
                LOG.debug("Adding the node '" + node.getNodeName() + "' of kind " + node.getNodeKind() + " to the list");
            }
        }
        list.add(node);
    }

    /**
     * Return the root element of {@code doc} (which must be a document node).
     * 
     * Error if there is other element children.
     */
    private static XdmNode getDocElement(XdmNode doc)
            throws ServlexException
    {
        XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
        XdmNode child = null;
        while ( ( child == null || child.getNodeKind() != XdmNodeKind.ELEMENT ) && it.hasNext() ) {
            child = (XdmNode) it.next();
        }
        while ( it.hasNext() ) {
            XdmNode n = (XdmNode) it.next();
            if ( n.getNodeKind() == XdmNodeKind.ELEMENT ) {
                throw new ServlexException(500, "More than 1 element in a document");
            }
        }
        return child;
    }

    private static XdmNode getWrapperElem(XdmNode node)
            throws SaxonApiException
                 , ServlexException
    {
        if ( node.getNodeKind() == XdmNodeKind.DOCUMENT ) {
            node = getDocElement(node);
            if ( node == null ) {
                return null;
            }
        }
        QName name = node.getNodeName();
        // element(web:wrapper)
        if ( node.getNodeKind() == XdmNodeKind.ELEMENT && name.equals(WRAPPER_NAME) ) {
            LOG.debug("The pipeline returned a web:wrapper");
            return node;
        }
        // is not a wrapper at all
        LOG.debug("The pipeline did not return a web:wrapper (" + name + ")");
        return null;
    }

    /** The specific logger. */
    private static final Log LOG = new Log(CalabashPipeline.class);
    /** QName for 'web:response'. */
    private static final QName WRAPPER_NAME
            = new QName(ServlexConstants.WEBAPP_PREFIX, ServlexConstants.WEBAPP_NS, "wrapper");

    /** The Calabash processor. */
    private final CalabashXProc myCalabash;
    /** The configuration object. */
    private final ServerConfig myConfig;
    /** The audit trail object. */
    private final Auditor myAuditor;
    /** The processors object. */
    private final Processors myProcs;
    /** The compiled pipeline, to be used only once. */
    private XPipeline myCompiled;
    /** The Calabash runtime object. */
    private XProcRuntime myRuntime;
    /**
     * An instance of an XProc component.
     */
    private static class MyInstance
            implements ComponentInstance
    {
        public MyInstance(XPipeline pipe, Processors procs)
        {
            myPipe = pipe;
            myProcs = procs;
        }

        @Override
        public void connect(Sequence input)
                throws TechnicalException
        {
            if ( ! (input instanceof SaxonSequence) ) {
                throw new IllegalStateException("Not a Saxon sequence: " + input);
            }
            SaxonSequence seq = (SaxonSequence) input;
            CalabashHelper.writeTo(myPipe, NAME, seq.makeSaxonValue(), myProcs);
        }

        // TODO: error(), setErrorOptions(), writeErrorRequest(), writeErrorData()
        // and the several constants are mostly duplicated in SaxonXSLTTransform...
        @Override
        public void error(ComponentError error, Document request)
                throws TechnicalException
        {
            setErrorOptions(error);
            writeErrorRequest(request);
            writeErrorData(error);
        }

        private void setErrorOptions(ComponentError error)
                throws TechnicalException
        {
            // the original QName
            javax.xml.namespace.QName name = error.getName();
            if ( name != null ) {
                // the code-name
                String prefix = name.getPrefix();
                String local  = name.getLocalPart();
                if ( prefix != null && ! prefix.equals("") ) {
                    String n = prefix + ":" + local;
                    myPipe.passOption(CODE_NAME, new RuntimeValue(n));
                }
                else {
                    myPipe.passOption(CODE_NAME, new RuntimeValue(local));
                }
                // the code-namespace
                String ns = name.getNamespaceURI();
                myPipe.passOption(CODE_NS, new RuntimeValue(ns));
            }
            // the message
            String msg = error.getMsg();
            if ( msg != null ) {
                myPipe.passOption(MESSAGE, new RuntimeValue(msg));
            }
        }

        private void writeErrorRequest(Document request)
                throws TechnicalException
        {
            if ( ! (request instanceof SaxonDocument) ) {
                throw new TechnicalException("Not a Saxon doc: " + request);
            }
            SaxonDocument doc = (SaxonDocument) request;
            XdmNode node = doc.getSaxonNode();
            // connect the web request to the source port
            CalabashHelper.writeTo(myPipe, NAME, node, myProcs);
        }

        private void writeErrorData(ComponentError error)
                throws TechnicalException
        {
            // connect the user sequence to the user-data port
            Sequence sequence = error.getSequence();
            XdmValue userdata = SaxonHelper.toXdmValue(sequence);
            if ( userdata != null ) {
                CalabashHelper.writeTo(myPipe, ERROR, userdata, myProcs);
            }
        }

        private static final String NAME   = org.expath.servlex.processors.XProcProcessor.INPUT_PORT_NAME;
        private static final String ERROR  = org.expath.servlex.processors.XProcProcessor.ERROR_PORT_NAME;
        private static final String PREFIX = ServlexConstants.WEBAPP_PREFIX;
        private static final String NS     = ServlexConstants.WEBAPP_NS;
        private static final QName CODE_NAME = new QName(PREFIX, NS, ServlexConstants.OPTION_CODE_NAME);
        private static final QName CODE_NS   = new QName(PREFIX, NS, ServlexConstants.OPTION_CODE_NS);
        private static final QName MESSAGE   = new QName(PREFIX, NS, ServlexConstants.OPTION_MESSAGE);
        private final XPipeline  myPipe;
        private final Processors myProcs;
    }
}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
