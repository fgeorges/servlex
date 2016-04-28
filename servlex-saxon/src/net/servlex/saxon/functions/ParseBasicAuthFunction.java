/****************************************************************************/
/*  File:       ParseBasicAuthFunction.java                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-04                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.value.SequenceType;
import org.expath.servlex.processors.Processors;

/**
 * Parse the value of an Authorization HTTP header with Basic scheme.
 * 
 *     web:parse-basic-auth($header as xs:string) as element(web:basic-auth)
 * 
 *     &lt;web:basic-auth username="..." password="..."/&gt;
 * 
 * The value of $header is the value of the Authorization header.  It must be
 * of the form "Basic XXX" where XXX is "user:password" encoded using Base64.
 *
 * @author Florent Georges
 */
public class ParseBasicAuthFunction
        extends ExtensionFunctionDefinition
{
    public ParseBasicAuthFunction(Processors procs, Processor saxon)
    {
        myProcs = procs;
        mySaxon = saxon;
    }

    @Override
    public StructuredQName getFunctionQName()
    {
        return FunTypes.qname(LOCAL_NAME);
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        return FunTypes.types(FunTypes.SINGLE_STRING);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.single_element(ELEMENT_NAME, mySaxon);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new ParseBasicAuthCall(myProcs);
    }

    static final String LOCAL_NAME = "parse-basic-auth";

    private static final String ELEMENT_NAME = "basic-auth";
    private Processors myProcs;
    private Processor mySaxon;
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
