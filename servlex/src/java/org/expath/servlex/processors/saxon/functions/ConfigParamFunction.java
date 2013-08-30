/****************************************************************************/
/*  File:       ConfigParamFunction.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-08-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
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
 * Implements web:config-param().
 * 
 * Two different arities exist:
 *
 *     web:config-param($name as xs:string) as xs:string?
 * 
 *     web:config-param($name    as xs:string,
 *                      $default as xs:string?) as xs:string?
 *
 * @author Florent Georges
 * @date   2013-08-22
 */
public class ConfigParamFunction
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
    public int getMaximumNumberOfArguments()
    {
        return 2;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        final int      required = StaticProperty.EXACTLY_ONE;
        final int      optional = StaticProperty.ALLOWS_ZERO_OR_ONE;
        final ItemType string   = BuiltInAtomicType.STRING;
        SequenceType   first    = SequenceType.makeSequenceType(string, required);
        SequenceType   second   = SequenceType.makeSequenceType(string, optional);
        return new SequenceType[]{ first, second };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        final int      any   = StaticProperty.ALLOWS_ZERO_OR_MORE;
        final ItemType atomic = BuiltInAtomicType.ANY_ATOMIC;
        return SequenceType.makeSequenceType(atomic, any);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new ConfigParamCall();
    }

    private static final String LOCAL_NAME = "config-param";
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
