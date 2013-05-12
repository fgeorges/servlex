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
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
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

    public SaxonSequence(SequenceIterator iter)
    {
        if ( iter == null ) {
            throw new NullPointerException("Iterator is null for Saxon sequence");
        }
        init(iter);
    }

    private void init(Iterator<Item> iter)
    {
        List<Item> items = new ArrayList<Item>();
        while ( iter.hasNext() ) {
            items.add(iter.next());
        }
        myItems = items;
    }

    private void init(XdmSequenceIterator iter)
    {
        List<Item> items = new ArrayList<Item>();
        while ( iter.hasNext() ) {
            XdmItem item = iter.next();
            items.add(new SaxonItem(item));
        }
        myItems = items;
    }

    private void init(SequenceIterator iter)
    {
        List<Item> items = new ArrayList<Item>();
        net.sf.saxon.om.Item item;
        while ( (item = iter.current()) != null ) {
            items.add(new SaxonItem(item));
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
        Item item = itemAt(position);
        SaxonElement elem;
        try {
            return SaxonHelper.toSaxonElement(item);
        }
        catch ( TechnicalException ex ) {
            SaxonDocument doc = SaxonHelper.toSaxonDocument(item);
            return doc.getRootElement();
        }
    }

    public Sequence subSequence(int start)
    {
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

    // TODO: Should be package visible, but is used in XdmConnector (which
    // should use instead a method on SaxonHelper which should be move here...)
    public XdmValue makeSaxonValue()
    {
        Iterable<XdmItem> impl = new ItemIterable(myItems);
        return new XdmValue(impl);
    }

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

        private Iterable<Item> myOriginal;
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
            if ( ! (item instanceof SaxonItem) ) {
                throw new IllegalStateException("Not a Saxon item: " + item);
            }
            return ((SaxonItem) item).getSaxonItem();
        }

        @Override
        public void remove()
        {
            myOriginal.remove();
        }

        private Iterator<Item> myOriginal;
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
