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
import org.expath.servlex.WebRepository;

/**
 * Implements web:install-webapp().
 * 
 * The XPath signatures:
 *
 *     web:install-webapp($pkg as xs:base64Binary) as xs:string
 *
 *     web:install-webapp($pkg  as xs:base64Binary,
 *                        $root as xs:string) as xs:string
 *
 * @author Florent Georges
 * @date   2013-09-11
 */
public class InstallWebappFunction
        extends ExtensionFunctionDefinition
{
    public InstallWebappFunction(WebRepository repo)
    {
        myRepo = repo;
    }

    @Override
    public StructuredQName getFunctionQName()
    {
        return FunTypes.qname(LOCAL_NAME);
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
        return FunTypes.types(FunTypes.SINGLE_BASE64, FunTypes.SINGLE_STRING);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new InstallWebappCall(myRepo);
    }

    static final String LOCAL_NAME = "install-webapp";

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
