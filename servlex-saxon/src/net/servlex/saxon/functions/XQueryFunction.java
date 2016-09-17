/****************************************************************************/
/*  File:       XQueryFunction.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2016-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2016 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.value.SequenceType;

/**
 * Implements web:xquery().
 * 
 *     web:xquery($query as xs:string,
 *                $input as document-node()) as item()*
 *
 * @author Florent Georges
 * 
 * TODO: Externalize it in a package?
 */
public class XQueryFunction
        extends ExtensionFunctionDefinition
{
    public XQueryFunction(Processor saxon)
    {
        mySaxon = saxon;
    }

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
    public SequenceType[] getArgumentTypes()
    {
        return FunTypes.types(FunTypes.SINGLE_STRING, FunTypes.SINGLE_DOCUMENT);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.ANY_ITEM;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new XQueryCall(mySaxon);
    }

    /** The local name of the function. */
    static final String LOCAL_NAME = "xquery";
    /** The Saxon processor. */
    private final Processor mySaxon;
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
