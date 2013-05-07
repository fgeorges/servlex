/****************************************************************************/
/*  File:       SaxonHelper.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-12-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Value;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.saxon.ConfigHelper;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Element;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.saxon.WebappFunctions;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.saxon.SaxonDocument;
import org.expath.servlex.processors.saxon.SaxonElement;
import org.expath.servlex.processors.saxon.SaxonEmptySequence;
import org.expath.servlex.processors.saxon.SaxonItem;
import org.expath.servlex.processors.saxon.SaxonSequence;
import org.expath.servlex.runtime.ComponentError;

/**
 * Helper methods for Saxon.
 *
 * @author Florent Georges
 * @date   2010-12-21
 */
public class SaxonHelper
{
    public static Processor makeSaxon(SaxonRepository repo, Processors procs)
            throws PackageException
    {
        Processor saxon = new Processor(true);
        ConfigHelper helper = new ConfigHelper(repo);
        helper.config(saxon.getUnderlyingConfiguration());
        WebappFunctions.setup(procs, saxon);
        return saxon;
    }

    public static SaxonItem toSaxonItem(Item item)
            throws TechnicalException
    {
        if ( ! (item instanceof SaxonItem) ) {
            throw new TechnicalException("Not a Saxon item: " + item);
        }
        return (SaxonItem) item;
    }

    public static SaxonElement toSaxonElement(Element elem)
            throws TechnicalException
    {
        if ( ! (elem instanceof SaxonElement) ) {
            throw new TechnicalException("Not a Saxon element: " + elem);
        }
        return (SaxonElement) elem;
    }

    public static SaxonElement toSaxonElement(Item item)
            throws TechnicalException
    {
        SaxonItem sitem = toSaxonItem(item);
        XdmItem xdm = sitem.getSaxonItem();
        if ( ! (xdm instanceof XdmNode) ) {
            throw new TechnicalException("Not a node: " + xdm);
        }
        XdmNode node = (XdmNode) xdm;
        return new SaxonElement(node);
    }

    public static SaxonDocument toSaxonDocument(Document doc)
            throws TechnicalException
    {
        if ( ! (doc instanceof SaxonDocument) ) {
            throw new TechnicalException("Not a Saxon document: " + doc);
        }
        return (SaxonDocument) doc;
    }

    public static SaxonDocument toSaxonDocument(Item item)
            throws TechnicalException
    {
        SaxonItem sitem = toSaxonItem(item);
        XdmItem xdm = sitem.getSaxonItem();
        if ( ! (xdm instanceof XdmNode) ) {
            throw new TechnicalException("Not a node: " + xdm);
        }
        XdmNode node = (XdmNode) xdm;
        return new SaxonDocument(node);
    }

    public static SaxonSequence toSaxonSequence(Sequence sequence)
            throws TechnicalException
    {
        if ( ! (sequence instanceof SaxonSequence) ) {
            throw new TechnicalException("Not a Saxon sequence: " + sequence);
        }
        return (SaxonSequence) sequence;
    }

    public static XdmValue toXdmValue(Item item)
            throws TechnicalException
    {
        SaxonItem sitem = toSaxonItem(item);
        return sitem.getSaxonItem();
    }

    public static XdmValue toXdmValue(Document doc)
            throws TechnicalException
    {
        SaxonDocument sdoc = toSaxonDocument(doc);
        return sdoc.getSaxonNode();
    }

    public static XdmValue toXdmValue(Sequence sequence)
            throws TechnicalException
    {
        SaxonSequence sseq = toSaxonSequence(sequence);
        return sseq.makeSaxonValue();
    }

    public static SequenceIterator toSequenceIterator(Sequence sequence)
            throws TechnicalException
    {
        XdmValue value = toXdmValue(sequence);
        ValueRepresentation rep = value.getUnderlyingValue();
        try {
            return Value.asIterator(rep);
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error getting an iterator out of an XDM value", ex);
        }
    }

    /**
     * Return the root element of the document node passed in param.
     *
     * Throw an error is the param is null, is not a document node, or if it
     * not exactly one child which is an element node.
     */
    public static XdmNode getDocumentRootElement(Document doc)
            throws TechnicalException
    {
        if ( doc == null ) {
            throw new TechnicalException("doc is null");
        }
        SaxonDocument sdoc = toSaxonDocument(doc);
        XdmNode node = sdoc.getSaxonNode();
        if ( node.getNodeKind() != XdmNodeKind.DOCUMENT ) {
            throw new TechnicalException("doc is not a document node: " + node.getNodeKind());
        }
        XdmSequenceIterator it = node.axisIterator(Axis.CHILD);
        XdmNode root = ignoreWhitespaceTextNodes(it);
        if ( root == null ) {
            throw new TechnicalException("doc has no child (except whitespace-only text nodes)");
        }
        XdmNode second = ignoreWhitespaceTextNodes(it);
        if ( second != null ) {
            String name_1 = root.getNodeName().getClarkName();
            String name_2 = second.getNodeName().getClarkName();
            String msg    = "doc has several children: " + name_1 + ", " + name_2;
            throw new TechnicalException(msg);
        }
        return root;
    }

    /**
     * Return next node, ignoring all whitespace-only text nodes.
     * 
     * Return null if there is no such next node.
     */
    public static XdmNode ignoreWhitespaceTextNodes(XdmSequenceIterator it)
    {
        final Pattern pattern = Pattern.compile("\\s+");
        for ( ; /* ever */ ; ) {
            if ( ! it.hasNext() ) {
                return null;
            }
            XdmNode node = (XdmNode) it.next();
            if ( node.getNodeKind() != XdmNodeKind.TEXT ) {
                return node;
            }
            String value = node.getStringValue();
            if ( ! pattern.matcher(value).matches() ) {
                return node;
            }
        }
    }

    public static ComponentError makeError(SaxonApiException ex)
            throws ServlexException
    {
        if ( ! (ex.getCause() instanceof XPathException) ) {
            throw new ServlexException(500, "Internal error", ex);
        }
        XPathException cause = (XPathException) ex.getCause();
        QName name = cause.getErrorCodeQName().toJaxpQName();
        String msg = cause.getMessage();
        XdmValue value = MyValue.wrap(cause.getErrorObject());
        Sequence sequence = value == null
                ? SaxonEmptySequence.getInstance()
                : new SaxonSequence(value);
        return new ComponentError(cause, name, msg, sequence);
    }

    /**
     * This class is a trick to make the protected wrap() available.
     */
    private static class MyValue
            extends XdmValue
    {
        public static XdmValue wrap(ValueRepresentation v)
        {
            return XdmValue.wrap(v);
        }
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
