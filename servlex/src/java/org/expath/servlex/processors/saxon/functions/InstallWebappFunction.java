/****************************************************************************/
/*  File:       InstallWebappFunction.java                                  */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

/**
 * Implements web:install-webapp().
 * 
 * The XPath signatures:
 *
 *     web:install-webapp($repo as item(),
 *                        $pkg  as xs:base64Binary) as xs:string?
 *
 *     web:install-webapp($repo as item(),
 *                        $pkg  as xs:base64Binary,
 *                        $root as xs:string) as xs:string?
 *
 *     web:install-webapp($repo   as item(),
 *                        $pkg    as xs:base64Binary,
 *                        $root   as xs:string,
 *                        $config as xs:string*) as xs:string?
 *
 * The parameter $repo must be a {@link RepositoryItem}.  The parameter $config
 * must be an even sequence of strings: a config name then its value, then a
 * config name then its value, etc., one pair for each config parameter.
 * 
 * If the function returns no string, then it installed a regular library
 * package (not a webapp).
 *
 * @author Florent Georges
 * @date   2013-09-11
 */
public class InstallWebappFunction
        extends ExtensionFunctionDefinition
{
    @Override
    public StructuredQName getFunctionQName()
    {
        return FunTypes.qname(LOCAL_NAME);
    }

    @Override
    public int getMinimumNumberOfArguments()
    {
        return 2;
    }

    @Override
    public int getMaximumNumberOfArguments()
    {
        return 4;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        return FunTypes.types(
                FunTypes.SINGLE_ITEM,
                FunTypes.SINGLE_BASE64,
                FunTypes.SINGLE_STRING,
                FunTypes.ANY_STRING);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.OPTIONAL_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new InstallWebappCall();
    }

    static final String LOCAL_NAME = "install-webapp";
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
