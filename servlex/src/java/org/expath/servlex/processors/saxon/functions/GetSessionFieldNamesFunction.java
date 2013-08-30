/****************************************************************************/
/*  File:       GetSessionFieldNamesFunction.java                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-06-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

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
 *     web:get-session-field-names() as xs:string*
 *
 * @author Florent Georges
 * @date   2010-06-12
 */
public class GetSessionFieldNamesFunction
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
        return 0;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        return new SequenceType[]{ SequenceType.EMPTY_SEQUENCE };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        final int      any   = StaticProperty.ALLOWS_ZERO_OR_MORE;
        final ItemType itype = BuiltInAtomicType.STRING;
        return SequenceType.makeSequenceType(itype, any);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new GetSessionFieldNamesCall();
    }

    private static final String LOCAL_NAME = "get-session-field-names";
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