/****************************************************************************/
/*  File:       SaxonSequence.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-30                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Sequence;

/**
 * A document for Saxon.
 *
 * @author Florent Georges
 * @date   2013-04-30
 */
public class SaxonSequence
        implements Sequence
{
    public SaxonSequence(Iterable<Item> items)
    {
        myItems = items;
    }

    public SaxonSequence(XdmSequenceIterator iter)
    {
        List<Item> items = new ArrayList<Item>();
        while ( iter.hasNext() ) {
            XdmItem item = iter.next();
            items.add(new SaxonItem(item));
        }
        myItems = items;
    }

    public SaxonSequence(XdmValue seq)
    {
        this(seq.iterator());
    }

    public Iterator<Item> iterator()
    {
        return myItems.iterator();
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

        public boolean hasNext()
        {
            return myOriginal.hasNext();
        }

        public XdmItem next()
        {
            Item item = myOriginal.next();
            if ( ! (item instanceof SaxonItem) ) {
                throw new IllegalStateException("Not a Saxon item: " + item);
            }
            return ((SaxonItem) item).getSaxonItem();
        }

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
