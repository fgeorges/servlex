/****************************************************************************/
/*  File:       SaxonXSLTTransform.java                                     */
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
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.log4j.Logger;
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
import org.expath.servlex.processors.saxon.model.SaxonSequence;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * ...
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public class SaxonXSLTTransform
        implements Component
{
    public SaxonXSLTTransform(Processor saxon, String stylesheet)
    {
        mySaxon = saxon;
        myStyle = stylesheet;
    }

    @Override
    public void logApplication(Logger log)
    {
        log.debug("      XSLT Transform");
        log.debug("         style: " + myStyle);
    }

    @Override
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
        throws ServlexException
             , ComponentError
    {
        auditor.run("style");
        try {
            XsltExecutable exec = getCompiled();
            XsltTransformer trans = exec.load();
            ComponentInstance instance = new MyInstance(trans);
            connector.connectToStylesheet(instance, config);
            XdmDestination dest = new XdmDestination();
            trans.setDestination(dest);
            trans.transform();
            // TODO: As per XSLT, this is always a doc node.  Check that.  But for
            // now, I take the doc's children as the result sequence...
            // TODO: BTW, check this is a document node...
            XdmNode doc = dest.getXdmNode();
            XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
            Sequence seq = new SaxonSequence(it);
            return new XdmConnector(seq, auditor);
        }
        catch ( PackageException ex ) {
            LOG.error("Internal error", ex);
            throw new ServlexException(500, "Internal error", ex);
        }
        catch ( SaxonApiException ex ) {
            LOG.error("User error in pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
        catch ( TransformerException ex ) {
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
            XsltCompiler c = mySaxon.newXsltCompiler();
            // saxon's xslt compiler does not use its uri resolver on the param
            // passed directly to the stream source ctor; the resolver is used
            // only for xsl:import and xsl:include, so we have to call it first
            // explicitely
            URIResolver resolver = c.getURIResolver();
            Source src = ( resolver == null )
                    ? null
                    : resolver.resolve(myStyle, null);
            if ( src == null ) {
                src = new StreamSource(myStyle);
            }
            myCompiled = c.compile(src);
        }
        return myCompiled;
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(SaxonXSLTTransform.class);

    private Processor mySaxon;
    private String myStyle;
    private XsltExecutable myCompiled = null;

    /**
     * An instance of this component.
     */
    private static class MyInstance
            implements ComponentInstance
    {
        public MyInstance(XsltTransformer trans)
        {
            myTrans = trans;
        }

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
         */
        public void connect(Sequence input)
                throws TechnicalException
        {
            if ( ! (input instanceof SaxonSequence) ) {
                throw new IllegalStateException("Not a Saxon sequence: " + input);
            }
            SaxonSequence seq   = (SaxonSequence) input;
            XdmValue      value = seq.makeSaxonValue();
            XdmNode       node  = getContextNode(value);
            // the context node for the transform
            myTrans.setInitialContextNode(node);
            // the whole input sequence as $web:input
            // TODO: Is it possible to set it only if it is declared?  Is this
            // actually an error if it is not declared?
            myTrans.setParameter(NAME, value);
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

        public void error(ComponentError error, Document request)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private static QName NAME = new QName(ServlexConstants.WEBAPP_NS, "input");
        private XsltTransformer myTrans;
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
