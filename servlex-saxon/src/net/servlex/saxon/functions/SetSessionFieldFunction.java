/****************************************************************************/
/*  File:       SetSessionFieldFunction.java                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-06-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

/**
 * Set the value of a property in the session.
 * 
 * The XPath prototype:
 *
 *     web:set-session-field($name as xs:string, $value as item()*)
 *        as empty-sequence()
 *
 * @author Florent Georges
 */
public class SetSessionFieldFunction
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
        return FunTypes.types(FunTypes.SINGLE_STRING, FunTypes.ANY_ITEM);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.EMPTY_SEQUENCE;
    }

    @Override
    public boolean hasSideEffects()
    {
        return true;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new SetSessionFieldCall();
    }

    static final String LOCAL_NAME = "set-session-field";
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
