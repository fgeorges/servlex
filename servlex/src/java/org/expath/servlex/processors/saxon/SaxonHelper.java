/****************************************************************************/
/*  File:       SaxonHelper.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-12-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.saxon.ConfigHelper;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Element;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.saxon.model.SaxonDocument;
import org.expath.servlex.processors.saxon.model.SaxonElement;
import org.expath.servlex.processors.saxon.model.SaxonEmptySequence;
import org.expath.servlex.processors.saxon.model.SaxonItem;
import org.expath.servlex.processors.saxon.model.SaxonSequence;
import org.expath.servlex.runtime.ComponentError;

/**
 * Helper methods for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonHelper
{
    public static String clarkName(NodeInfo node)
    {
        return "{" + node.getURI() + "}" + node.getLocalPart();
    }

    public static Processor makeSaxon(SaxonRepository repo, Processors procs, ServerConfig config, String config_file)
            throws PackageException
    {
        Processor saxon;
        if ( config_file == null ) {
            saxon = new Processor(true);
        }
        else {
            File   f = new File(config_file);
            Source c = new StreamSource(f);
            try {
                saxon = new Processor(c);
            }
            catch ( SaxonApiException ex ) {
                throw new PackageException("Error instantiating Saxon with config file: " + config_file, ex);
            }
        }
        ConfigHelper helper = new ConfigHelper(repo);
        helper.config(saxon.getUnderlyingConfiguration());
        WebappFunctions.setup(procs, saxon, config);
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
        XdmItem xdm = SaxonItem.getXdmItem(item);
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
        XdmItem xdm = SaxonItem.getXdmItem(item);
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
        return SaxonItem.getXdmItem(item);
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
        net.sf.saxon.om.Sequence seq = value.getUnderlyingValue();
        try {
            return seq.iterate();
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error getting an iterator out of an XDM value", ex);
        }
    }

    public static SequenceIterator toSequenceIterator(String string)
            throws TechnicalException
    {
        if ( string == null ) {
            return EmptySequence.getInstance().iterate();
        }
        StringValue v = new StringValue(string);
        return SingletonIterator.makeIterator(v);
    }

    public static SequenceIterator toSequenceIterator(boolean bool)
            throws TechnicalException
    {
        BooleanValue v = BooleanValue.get(bool);
        return SingletonIterator.makeIterator(v);
    }

    public static SequenceIterator toSequenceIterator(Iterable<String> strings)
            throws TechnicalException
    {
        List<StringValue> items = new ArrayList<>();
        for ( String s : strings ) {
            StringValue v = new StringValue(s);
            items.add(v);
        }
        return new ListIterator(items);
    }

    /**
     * Return the root element of the document node passed in param.
     * 
     * @param doc The document node to return the root element from.
     *
     * @return The root element of the document node passed in {@code  doc}.
     * 
     * @throws TechnicalException if {@code doc} is null, is not a document node,
     *     or if it not exactly one child which is an element node.
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
     * @param it The sequence where ignoring whitespace text nodes from.
     * 
     * @return The next node, ignoring all whitespace-only text nodes, or null
     *     if there is no such next node.
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
        // TODO: What to do if the error code name is null?
        // Is it only possible?
        QName name = cause.getErrorCodeQName() == null
                ? null
                : cause.getErrorCodeQName().toJaxpQName();
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
        public static XdmValue wrap(net.sf.saxon.om.Sequence s)
        {
            return XdmValue.wrap(s);
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
