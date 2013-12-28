/****************************************************************************/
/*  File:       RepositoryItem.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import org.expath.servlex.WebRepository;

/**
 * A specific type of item, encapsulating a {@link WebRepository} object.
 *
 * @author Florent Georges
 * @date   2013-09-15
 */
public class RepositoryItem
        extends AtomicValue
{
    public RepositoryItem(WebRepository repo)
    {
        myRepo = repo;
    }

    public WebRepository getRepository()
    {
        return myRepo;
    }

    public SequenceIterator asSequenceIterator()
    {
        return SingletonIterator.makeIterator(this);
    }

    @Override
    public Comparable getSchemaComparable()
    {
        String msg = "A repository item cannot be schema-compared.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public Object getXPathComparable(boolean bln, StringCollator collator, XPathContext ctxt)
            throws NoDynamicContextException
    {
        String msg = "A repository item cannot be XPath-compared.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public boolean equals(Object o)
    {
        if ( ! (o instanceof RepositoryItem) ) {
            return false;
        }
        RepositoryItem other = (RepositoryItem) o;
        return getRepository() == other.getRepository();
    }

    @Override
    public BuiltInAtomicType getPrimitiveType()
    {
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType at)
    {
        String msg = "A repository item cannot be copied as sub-type.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    protected CharSequence getPrimitiveStringValue()
    {
        String msg = "A repository item does not have any primitive string value.";
        throw new UnsupportedOperationException(msg);
    }

    private WebRepository myRepo;
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
