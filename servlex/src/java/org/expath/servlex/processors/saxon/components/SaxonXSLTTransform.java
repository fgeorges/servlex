/****************************************************************************/
/*  File:       SaxonXSLTTransform.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.components;

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.TransformerException;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
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
 * An XSLT stylesheet component implemented for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonXSLTTransform
        implements Component
{
    public SaxonXSLTTransform(SaxonCalabash procs, String stylesheet)
    {
        mySaxon = procs.getSaxon();
        myStyle = stylesheet;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("saxon xslt transform");
    }

    @Override
    public void logApplication(Log log)
    {
        log.debug("      XSLT Transform");
        log.debug("         style: " + myStyle);
    }

    @Override
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
        throws ServlexException
             , ComponentError
    {
        auditor.run("xslt transform");
        try {
            XsltExecutable exec = getCompiled();
            Xslt30Transformer trans = exec.load30();
            MyInstance instance = new MyInstance();
            connector.connectToStylesheet(instance, config);
            XdmDestination dest = new XdmDestination();
            XdmValue value = instance.getValue();
            XdmNode  node  = getContextNode(value);
            trans.setInitialContextNode(node);
            Map<QName, XdmValue> params = new HashMap<>();
            params.put(NAME, value);
            trans.setStylesheetParameters(params);
            trans.applyTemplates(value, dest);
            // TODO: As per XSLT, this is always a doc node.  Check that.  But for
            // now, I take the doc's children as the result sequence...
            // TODO: BTW, check this is a document node...
            XdmNode doc = dest.getXdmNode();
            XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
            Sequence seq = new SaxonSequence(it);
            return new XdmConnector(seq, auditor);
        }
        catch ( PackageException | TransformerException | TechnicalException ex ) {
            LOG.error("Internal error", ex);
            throw new ServlexException(500, "Internal error", ex);
        }
        catch ( SaxonApiException ex ) {
            LOG.error("User error in pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
    }
 
    private synchronized XsltExecutable getCompiled()
            throws PackageException
                 , SaxonApiException
                 , TransformerException
    {
        if ( myCompiled == null ) {
            myCompiled = SaxonXSLTFunction.compile(mySaxon, myStyle);
        }
        return myCompiled;
    }

    /**
     * Extract the context node out of the input sequence.
     */
    private XdmNode getContextNode(XdmValue seq)
            throws TechnicalException
    {
        if ( seq.size() == 0 ) {
            throw new TechnicalException("The input to the transform is empty");
        }
        XdmItem first = seq.itemAt(0);
        if ( first.isAtomicValue() ) {
            String msg = "An atomic value cannot be set as the input to a transform: ";
            throw new TechnicalException(msg + first);
        }
        XdmNode node = (XdmNode) first;
        node = adjustNode(node);
        return node;
    }

    /**
     * Adjust the node if needed.
     * 
     * If the node is an element, child of a document with no other element
     * child, then the parent document is return instead.  If not, the element
     * is returned as is.
     */
    private XdmNode adjustNode(XdmNode node)
            throws TechnicalException
    {
        XdmNodeKind kind = node.getNodeKind();
        if ( kind == XdmNodeKind.DOCUMENT ) {
            // nothing
        }
        else if ( kind == XdmNodeKind.ELEMENT ) {
            node = tryParentDocument(node);
        }
        else {
            String msg = "The input to the transform is neither a document nor an element node: ";
            throw new TechnicalException(msg + kind);
        }
        return node;
    }

    /**
     * Return the parent node if it is a suitable document node.
     */
    private XdmNode tryParentDocument(XdmNode node)
    {
        XdmNode parent = node.getParent();
        if ( parent == null ) {
            return node;
        }
        else if ( parent.getNodeKind() == XdmNodeKind.DOCUMENT ) {
            int elem_count = 0;
            XdmSequenceIterator children = parent.axisIterator(Axis.CHILD);
            while ( children.hasNext() ) {
                XdmItem child = children.next();
                if ( child.isAtomicValue() ) {
                    // nothing
                }
                else if ( ((XdmNode) child).getNodeKind() == XdmNodeKind.ELEMENT ) {
                    ++elem_count;
                }
                if ( elem_count > 1 ) {
                    return node;
                }
            }
            return parent;
        }
        else {
            return node;
        }
    }

    /** The logger. */
    private static final Log LOG = new Log(SaxonXSLTTransform.class);

    private static final String PREFIX = ServlexConstants.WEBAPP_PREFIX;
    private static final String NS     = ServlexConstants.WEBAPP_NS;
    private static final QName  NAME   = new QName(PREFIX, NS, "input");

    private final Processor mySaxon;
    private final String myStyle;
    private XsltExecutable myCompiled = null;

    /**
     * An instance of this component.
     */
    private static class MyInstance
            implements ComponentInstance
    {
        /**
         * Connect the sequence to $web:input and $web:input[1] as the input tree.
         * 
         * Connecting the first item in the input sequence will succeed only if
         * it is a document node or an element node.  If it is an element node
         * and that element is a direct child of a document node and that this
         * element has no sibling element node, then its parent document node is
         * used instead.
         * 
         * TODO: That imposes the user to declare the parameter in the stylesheet,
         * isn't it?  Even if there is only one node in the sequence (this is a
         * common case: we want to apply a stylesheet and we know we have a single
         * document, then the intuitive way is just to apply the stylesheet to the
         * node, no need to declare a parameter...)
         * 
         * TODO: Saxon 9.7 XSLT 3.0 transformer object does not allow anymore to
         * set parameters one after the other.  Will most likely have to refactor
         * the way errors are handled.
         */
        @Override
        public void connect(Sequence input)
                throws TechnicalException
        {
            if ( ! (input instanceof SaxonSequence) ) {
                throw new IllegalStateException("Not a Saxon sequence: " + input);
            }
            SaxonSequence seq = (SaxonSequence) input;
            myValue = seq.makeSaxonValue();
        }

        // TODO: error(), setErrorOptions(), writeErrorRequest(), writeErrorData()
        // and the several constants are mostly duplicated from CalabashPipeline...
        @Override
        public void error(ComponentError error, Document request)
                throws TechnicalException
        {
            throw new UnsupportedOperationException("Not supported yet.");
//            setErrorOptions(error);
//            writeErrorRequest(request);
//            writeErrorData(error);
        }

//        private void setErrorOptions(ComponentError error)
//                throws TechnicalException
//        {
//            // the code
//            QName code = null;
//            javax.xml.namespace.QName name = error.getName();
//            if ( name != null ) {
//                String prefix = name.getPrefix();
//                String local  = name.getLocalPart();
//                String ns     = name.getNamespaceURI();
//                code = new QName(prefix, ns, local);
//            }
//            // the message
//            String msg = error.getMsg();
//            // set them as options
//            myTrans.setParameter(CODE_NAME, code == null ? null : new XdmAtomicValue(code));
//            myTrans.setParameter(MESSAGE,   new XdmAtomicValue(msg));
//        }
//
//        private void writeErrorRequest(Document request)
//                throws TechnicalException
//        {
//            if ( ! (request instanceof SaxonDocument) ) {
//                throw new TechnicalException("Not a Saxon doc: " + request);
//            }
//            SaxonDocument doc = (SaxonDocument) request;
//            XdmNode node = doc.getSaxonNode();
//            // connect the web request as the context node
//            myTrans.setInitialContextNode(node);
//            // connect the web request to the web:input parameter
//            // TODO: What about the bodies?
//            myTrans.setParameter(NAME, node);
//        }
//
//        private void writeErrorData(ComponentError error)
//                throws TechnicalException
//        {
//            // connect the user sequence to the user-data port
//            Sequence sequence = error.getSequence();
//            XdmValue userdata = SaxonHelper.toXdmValue(sequence);
//            if ( userdata != null ) {
//                myTrans.setParameter(ERROR, userdata);
//            }
//        }

        public XdmValue getValue()
        {
            return myValue;
        }

//        private static final QName  ERROR     = new QName(PREFIX, NS, "error-data");
//        private static final QName  CODE_NAME = new QName(PREFIX, NS, "error-code");
//        private static final QName  MESSAGE   = new QName(PREFIX, NS, "error-message");

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
