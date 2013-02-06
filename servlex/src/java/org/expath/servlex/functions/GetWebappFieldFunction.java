/****************************************************************************/
/*  File:       GetWebappFieldFunction.java                                 */
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
 *     web:get-webapp-field($name as xs:string) as item()*
 *
 * TODO: Add a second arity with the default value to use in case the webapp
 * field for that name is not defined:
 *
 *     web:get-webapp-field($name as xs:string, $default as item()*) as item()*
 *
 * @author Florent Georges
 * @date   2010-11-22
 */
public class GetWebappFieldFunction
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
        return 1;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        final int      any   = StaticProperty.ALLOWS_ZERO_OR_MORE;
        final ItemType itype = BuiltInAtomicType.STRING;
        SequenceType   stype = SequenceType.makeSequenceType(itype, any);
        return new SequenceType[]{ stype };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        final int      any   = StaticProperty.ALLOWS_ZERO_OR_MORE;
        final ItemType itype = BuiltInAtomicType.ANY_ATOMIC;
        return SequenceType.makeSequenceType(itype, any);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new GetWebappFieldCall();
    }

    private static final String LOCAL_NAME = "get-webapp-field";
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
