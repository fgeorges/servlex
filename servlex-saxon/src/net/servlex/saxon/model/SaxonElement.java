/****************************************************************************/
/*  File:       SaxonElement.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.model;

import java.net.URI;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Attribute;
import org.expath.servlex.processors.Element;
import org.expath.servlex.processors.Item;

/**
 * An element for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonElement
        extends SaxonItem
        implements Element
{
    public SaxonElement(XdmNode elem)
            throws TechnicalException
    {
        super(elem);
        if ( elem == null ) {
            throw new NullPointerException("Underlying node is null for Saxon element");
        }
        XdmNodeKind kind = elem.getNodeKind();
        if ( kind != XdmNodeKind.ELEMENT ) {
            throw new TechnicalException("Node is not an element, for Saxon element: " + kind);
        }
        myElem = elem;
    }

    @Override
    public QName name()
    {
        net.sf.saxon.s9api.QName name = myElem.getNodeName();
        String ns     = name.getNamespaceURI();
        String local  = name.getLocalName();
        String prefix = name.getPrefix();
        return new QName(ns, local, prefix);
    }

    @Override
    public URI baseUri()
    {
        return myElem.getBaseURI();
    }

    @Override
    public Iterator<Attribute> attributes()
    {
        XdmSequenceIterator iter = myElem.axisIterator(Axis.ATTRIBUTE);
        return new AttributeIterator(iter);
    }

    @Override
    public Iterator<Element> elements()
    {
        XdmSequenceIterator iter = myElem.axisIterator(Axis.CHILD);
        return new ElementIterator(iter);
    }

    @Override
    public Iterator<Item> children()
    {
        XdmSequenceIterator iter = myElem.axisIterator(Axis.CHILD);
        return new ChildIterator(iter);
    }

    private XdmNode myElem;

    /**
     * Wrap an {@link XdmSequenceIterator} to behave as an iterator over {@link Attribute}.
     */
    private static class AttributeIterator
            implements Iterator<Attribute>
    {
        public AttributeIterator(XdmSequenceIterator iter)
        {
            myIter = iter;
        }

        public boolean hasNext()
        {
            return myIter.hasNext();
        }

        public Attribute next()
        {
            XdmItem item = myIter.next();
            if ( item.isAtomicValue() ) {
                throw new IllegalStateException("Attribute is an atomic value?!?");
            }
            XdmNode node = (XdmNode) item;
            try {
                return new SaxonAttribute(node);
            }
            catch ( TechnicalException ex ) {
                throw new IllegalStateException("The element node is not a node?!?", ex);
            }
        }

        public void remove()
        {
            myIter.remove();
        }

        private XdmSequenceIterator myIter;
    }

    /**
     * Wrap an {@link XdmSequenceIterator} to behave as an iterator over {@link Element}.
     * 
     * TODO: Should probably ignore whitespace-only text nodes and comment nodes.
     * For that, must implements read-ahead for hasNext() (if the last node is
     * whitespace-only text, then hasNext() must return false then...)
     */
    private static class ElementIterator
            implements Iterator<Element>
    {
        public ElementIterator(XdmSequenceIterator iter)
        {
            myIter = iter;
            setNext();
        }

        public boolean hasNext()
        {
            return myNext != null;
        }

        public Element next()
        {
            try {
                Element elem = new SaxonElement(myNext);
                setNext();
                return elem;
            }
            catch ( TechnicalException ex ) {
                throw new IllegalStateException("Error creating the Saxon element?!? - " + myNext, ex);
            }
        }

        public void remove()
        {
            myIter.remove();
        }

        private void setNext()
        {
            // something else in the sequence?
            if ( ! myIter.hasNext() ) {
                myNext = null;
                return;
            }
            // is the item a node?
            XdmItem item = myIter.next();
            if ( item.isAtomicValue() ) {
                myNext = null;
                throw new IllegalStateException("Atomic value not accepted in an element iterator: " + item);
            }
            XdmNode node = (XdmNode) item;
            XdmNodeKind kind = node.getNodeKind();
            // this element is the next one
            if ( kind == XdmNodeKind.ELEMENT ) {
                myNext = node;
            }
            // ignore comment nodes
            else if ( kind == XdmNodeKind.COMMENT ) {
                setNext();
            }
            // ignore whitespace-only text nodes, other text nodes are errors
            else if ( kind == XdmNodeKind.TEXT ) {
                String text = node.getStringValue();
                Matcher matcher = WHITESPACES.matcher(text);
                if ( matcher.matches() ) {
                    setNext();
                }
                else {
                    throw new IllegalStateException("Non-whitespace-only text nodes not accepted in an element iterator: " + item);
                }
            }
            // all other kind of nodes are errors
            else {
                throw new IllegalStateException("Element iterator only accept element nodes: " + kind);
            }
        }

        private XdmSequenceIterator myIter;
        private XdmNode myNext;
        private static final Pattern WHITESPACES = Pattern.compile("[ \t\n]+");
    }

    /**
     * Wrap an {@link XdmSequenceIterator} to behave as an iterator over {@link Item}.
     */
    private static class ChildIterator
            implements Iterator<Item>
    {
        public ChildIterator(XdmSequenceIterator iter)
        {
            myIter = iter;
        }

        public boolean hasNext()
        {
            return myIter.hasNext();
        }

        public Item next()
        {
            XdmItem item = myIter.next();
            return new SaxonItem(item);
        }

        public void remove()
        {
            myIter.remove();
        }

        private XdmSequenceIterator myIter;
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
