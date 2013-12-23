/****************************************************************************/
/*  File:       SaxonSequence.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-30                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Element;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * A sequence for Saxon.
 * 
 * TODO: Review the design.  Maybe storing the underlying {@link XdmValue}
 * would be better here...?
 *
 * @author Florent Georges
 * @date   2013-04-30
 */
public class SaxonSequence
        implements Sequence
{
    public SaxonSequence(Iterable<Item> items)
    {
        if ( items == null ) {
            throw new NullPointerException("Underlying collection is null for Saxon sequence");
        }
        myItems = items;
    }

    public SaxonSequence(Iterator<Item> iter)
    {
        if ( iter == null ) {
            throw new NullPointerException("Iterator is null for Saxon sequence");
        }
        init(iter);
    }

    public SaxonSequence(XdmSequenceIterator iter)
    {
        if ( iter == null ) {
            throw new NullPointerException("Iterator is null for Saxon sequence");
        }
        init(iter);
    }

    public SaxonSequence(XdmValue seq)
    {
        if ( seq == null ) {
            throw new NullPointerException("Underlying sequence is null for Saxon sequence");
        }
        init(seq.iterator());
    }

    public SaxonSequence(net.sf.saxon.om.Sequence seq)
            throws TechnicalException
    {
        if ( seq == null ) {
            throw new NullPointerException("Sequence is null for Saxon sequence");
        }
        SequenceIterator it;
        try {
            it = seq.iterate();
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error getting an iterator out of the sequence", ex);
        }
        init(it);
    }

    public SaxonSequence(SequenceIterator iter)
            throws TechnicalException
    {
        if ( iter == null ) {
            throw new NullPointerException("Iterator is null for Saxon sequence");
        }
        init(iter);
    }

    private void init(Iterator<Item> iter)
    {
        List<Item> items = new ArrayList<>();
        while ( iter.hasNext() ) {
            items.add(iter.next());
        }
        myItems = items;
    }

    private void init(XdmSequenceIterator iter)
    {
        List<Item> items = new ArrayList<>();
        while ( iter.hasNext() ) {
            XdmItem item = iter.next();
            items.add(new SaxonItem(item));
        }
        myItems = items;
    }

    private void init(SequenceIterator iter)
            throws TechnicalException
    {
        List<Item> items = new ArrayList<>();
        net.sf.saxon.om.Item item;
        try {
            while ( (item = iter.next()) != null ) {
                items.add(new SaxonItem(item));
            }
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error iterating the Saxon sequence", ex);
        }
        myItems = items;
    }

    @Override
    public Iterator<Item> iterator()
    {
        return myItems.iterator();
    }

    @Override
    public Item itemAt(int position)
    {
        Iterator<Item> it = iterator();
        while ( position > 0 && it.hasNext() ) {
            --position;
            it.next();
        }
        if ( position == 0 && it.hasNext() ) {
            return it.next();
        }
        else {
            return null;
        }
    }

    @Override
    public Element elementAt(int position)
            throws TechnicalException
    {
        // If the item at that position is exactly one document node, and its
        // children are exactly one element node (ignoring whitespace-only text
        // nodes), then it is returned directly instead.
        SaxonDocument doc = isDocument(position);
        if ( doc == null ) {
            Item item = itemAt(position);
            return SaxonHelper.toSaxonElement(item);
        }
        else {
            XdmNode elem = SaxonHelper.getDocumentRootElement(doc);
            return new SaxonElement(elem);
        }
    }

    @Override
    public Sequence subSequence(int start)
    {
        // If the sequence is exactly one document node, then its children
        // nodes are used directly instead.
        SaxonDocument doc = isSingleDocument();
        if ( doc == null ) {
            // if index is < 0, return ()
            if ( start < 0 ) {
                return SaxonEmptySequence.getInstance();
            }
            // get an iterator and iterate 'start' times
            Iterator<Item> iter = iterator();
            while ( start > 0 && iter.hasNext() ) {
                iter.next();
                --start;
            }
            // if reached the end, return ()
            if ( ! iter.hasNext() ) {
                return SaxonEmptySequence.getInstance();
            }
            // if not, return the sub-sequence, till the end
            return new SaxonSequence(iter);
        }
        else {
            Sequence seq = doc.getChildren();
            return seq.subSequence(start);
        }
    }

    // TODO: Should be package visible, but is used in XdmConnector (which
    // should use instead a method on SaxonHelper which should be move here...)
    public XdmValue makeSaxonValue()
    {
        Iterable<XdmItem> impl = new ItemIterable(myItems);
        return new XdmValue(impl);
    }

    /**
     * Test whether the item at that position is a document node.
     */
    private SaxonDocument isDocument(int position)
    {
        Item at = itemAt(position);
        if ( at == null ) {
            // no item at that position
            return null;
        }
        XdmItem item = SaxonItem.getXdmItem(at);
        if ( item.isAtomicValue() ) {
            // if the item is atomic
            return null;
        }
        XdmNode node = (XdmNode) item;
        try {
            // try to convert to SaxonDocument...
            return new SaxonDocument(node);
        }
        catch ( TechnicalException ex ) {
            // ... ctor raises an exception if 'node' is not a node
            return null;
        }
    }

    /**
     * Test whether this sequence is a single document node.
     * 
     * If the sequence is empty or has 2 or more items, it returns null.  If it
     * contains 1 single item which is not a document node, it returns null as
     * well.  If the sequence is a single item which is a document node, then
     * this node is returned as a {@link SaxonDocument}.
     */
    private SaxonDocument isSingleDocument()
    {
        Item second = itemAt(1);
        if ( second != null ) {
            // if 2 or more items in the sequence
            return null;
        }
        return isDocument(0);
    }

    /** The items. */
    private Iterable<Item> myItems;

    private static class ItemIterable
            implements Iterable<XdmItem>
    {
        public ItemIterable(Iterable<Item> original)
        {
            myOriginal = original;
        }

        @Override
        public Iterator<XdmItem> iterator()
        {
            return new ItemIterator(myOriginal.iterator());
        }

        private final Iterable<Item> myOriginal;
    }

    private static class ItemIterator
            implements Iterator<XdmItem>
    {
        private ItemIterator(Iterator<Item> original)
        {
            myOriginal = original;
        }

        @Override
        public boolean hasNext()
        {
            return myOriginal.hasNext();
        }

        @Override
        public XdmItem next()
        {
            Item item = myOriginal.next();
            return SaxonItem.getXdmItem(item);
        }

        @Override
        public void remove()
        {
            myOriginal.remove();
        }

        private final Iterator<Item> myOriginal;
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
