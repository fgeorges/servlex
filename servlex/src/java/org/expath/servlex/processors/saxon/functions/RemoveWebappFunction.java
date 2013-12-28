/****************************************************************************/
/*  File:       RemoveWebappFunction.java                                   */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

/**
 * Implements web:remove-webapp().
 * 
 * The XPath signature:
 *
 *     web:remove-webapp($repo as item(),
 *                       $root as xs:string) as xs:boolean
 *
 * The parameter $repo must be a {@link RepositoryItem}.
 * 
 * The function returns true if the webapp deployed at the context root
 * {@code root} has been properly un-installed.
 *
 * @author Florent Georges
 */
public class RemoveWebappFunction
        extends ExtensionFunctionDefinition
{
    @Override
    public StructuredQName getFunctionQName()
    {
        return FunTypes.qname(LOCAL_NAME);
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        return FunTypes.types(FunTypes.SINGLE_ITEM, FunTypes.SINGLE_STRING);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.SINGLE_BOOLEAN;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new RemoveWebappCall();
    }

    static final String LOCAL_NAME = "remove-webapp";
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
