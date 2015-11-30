/****************************************************************************/
/*  File:       SaxonXSLTFunction.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.components;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.Component;
import org.expath.servlex.components.ComponentInstance;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.XdmConnector;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.saxon.SaxonCalabash;
import org.expath.servlex.processors.saxon.model.SaxonSequence;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.processors.saxon.SaxonHelper;
import org.expath.servlex.tools.Log;

/**
 * An XSLT function component implemented for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonXSLTFunction
        implements Component
{
    public SaxonXSLTFunction(SaxonCalabash procs, String import_uri, String ns, String localname)
    {
        mySaxon = procs.getSaxon();
        myImportUri = import_uri;
        myNS = ns;
        myLocal = localname;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("saxon xslt function");
    }

    @Override
    public void logApplication(Log log)
    {
        log.debug("      XSLT Function");
        log.debug("         uri  : " + myImportUri);
        log.debug("         ns   : " + myNS);
        log.debug("         local: " + myLocal);
    }

    @Override
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
        throws ServlexException
             , ComponentError
    {
        auditor.run("xslt function");
        try {
            XsltExecutable exec = getCompiled();
            Xslt30Transformer trans = exec.load30();
            MyInstance instance = new MyInstance();
            connector.connectToXSLTComponent(instance, config);
            XdmDestination dest = new XdmDestination();
            QName name = new QName(myNS, myLocal);
            XdmValue[] args = { instance.getValue() };
            trans.callFunction(name, args, dest);
            // TODO: As per XSLT, this is always a doc node.  Check that.  But for
            // now, I take the doc's children as the result sequence...
            // TODO: BTW, check this is a document node...
            XdmNode doc = dest.getXdmNode();
            XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
            Sequence seq = new SaxonSequence(it);
            return new XdmConnector(seq, auditor);
        }
        catch ( SaxonApiException ex ) {
            LOG.error("User error in pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
        catch ( PackageException | TransformerException ex ) {
            LOG.error("Internal error", ex);
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    private synchronized XsltExecutable getCompiled()
            throws PackageException
                 , SaxonApiException
                 , TransformerException
    {
        if ( myCompiled == null ) {
            myCompiled = compile(mySaxon, myImportUri);
        }
        return myCompiled;
    }

    static XsltExecutable compile(Processor saxon, String uri)
            throws PackageException
                 , SaxonApiException
                 , TransformerException
    {
        XsltCompiler c = saxon.newXsltCompiler();
        // saxon's xslt compiler does not use its uri resolver on the param
        // passed directly to the stream source ctor; the resolver is used
        // only for xsl:import and xsl:include, so we have to call it first
        // explicitely
        URIResolver resolver = c.getURIResolver();
        Source src = ( resolver == null )
                ? null
                : resolver.resolve(uri, null);
        if ( src == null ) {
            src = new StreamSource(uri);
        }
        return c.compile(src);
    }

    /** The logger. */
    private static final Log LOG = new Log(SaxonXSLTFunction.class);

    private final Processor mySaxon;
    private final String myImportUri;
    private final String myNS;
    private final String myLocal;
    private XsltExecutable myCompiled = null;

    /**
     * An instance of this component.
     */
    private static class MyInstance
            implements ComponentInstance
    {
        @Override
        public void connect(Sequence input)
        {
            if ( ! (input instanceof SaxonSequence) ) {
                throw new IllegalStateException("Not a Saxon sequence: " + input);
            }
            SaxonSequence seq = (SaxonSequence) input;
            myValue = seq.makeSaxonValue();
        }

        @Override
        public void error(ComponentError error, Document request)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public XdmValue getValue()
        {
            return myValue;
        }

        private XdmValue myValue;
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
