/****************************************************************************/
/*  File:       FunReturn.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * Utils for return value of extension functions for Saxon.
 * 
 * TODO: Instead of {@code value()} functions, defining for each of them both
 * {@code required()} and {@code optional()} could be better, to handle
 * differently {@code null}s (in the former it is an error, in the latter it
 * returns {@code empty()}).
 *
 * @author Florent Georges
 */
public class FunReturn
{
    public static Sequence empty()
    {
        return EmptySequence.getInstance();
    }

    public static Sequence value(WebRepository repo)
    {
        if ( repo == null ) {
            return empty();
        }
        return new ObjectValue(repo);
    }

    public static Sequence value(XdmNode node)
    {
        if ( node == null ) {
            return empty();
        }
        return node.getUnderlyingNode();
    }

    public static Sequence value(XdmValue value)
    {
        if ( value == null ) {
            return empty();
        }
        return value.getUnderlyingValue();
    }

    public static Sequence value(Boolean b)
    {
        if ( b == null ) {
            return empty();
        }
        else if ( b ) {
            return BooleanValue.TRUE;
        }
        else {
            return BooleanValue.FALSE;
        }
    }

    public static Sequence value(String string)
    {
        if ( string == null ) {
            return empty();
        }
        return new StringValue(string);
    }

    public static Sequence value(Iterable<String> strings)
    {
        if ( strings == null ) {
            return empty();
        }
        List<AtomicValue> items = new ArrayList<>();
        for ( String s : strings ) {
            StringValue v = new StringValue(s);
            items.add(v);
        }
        if ( items.isEmpty() ) {
            return empty();
        }
        return new AtomicArray(items);
    }

    public static Sequence value(org.expath.servlex.processors.Sequence seq)
            throws TechnicalException
    {
        XdmValue val = SaxonHelper.toXdmValue(seq);
        return value(val);
    }

    private static final StringValue[] SV_ARRAY_MARKER = new StringValue[0];
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
