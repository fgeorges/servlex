/****************************************************************************/
/*  File:       SetServerFieldFunction.java                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.functions;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;
import org.expath.servlex.ServlexConstants;

/**
 * TODO: Doc...
 *
 *     web:set-server-field($name as xs:string, $value as item()*)
 *        as empty-sequence()
 *
 * (return value is the previous value is any)
 *
 * @author Florent Georges
 * @date   2010-11-22
 */
public class SetServerFieldFunction
        extends ExtensionFunctionDefinition
{
    @Override
    public StructuredQName getFunctionQName()
    {
        final String uri    = ServlexConstants.WEBAPP_NS;
        final String prefix = ServlexConstants.WEBAPP_PREFIX;
        return new StructuredQName(prefix, uri, LOCAL_NAME);
    }

    @Override
    public int getMinimumNumberOfArguments()
    {
        return 2;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        // xs:string
        final int      one    = StaticProperty.EXACTLY_ONE;
        final ItemType itype  = BuiltInAtomicType.STRING;
        SequenceType   string = SequenceType.makeSequenceType(itype, one);
        // item()*
        final int      any    = StaticProperty.ALLOWS_ZERO_OR_MORE;
        final ItemType atomic = BuiltInAtomicType.ANY_ATOMIC;
        SequenceType   items  = SequenceType.makeSequenceType(atomic, any);
        // xs:string, item()*
        return new SequenceType[]{ string, items };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return SequenceType.EMPTY_SEQUENCE;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new SetServerFieldCall();
    }

    private static final String LOCAL_NAME = "set-server-field";
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
