/****************************************************************************/
/*  File:       InstallEnabledFunction.java                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

/**
 * Implements web:install-enabled().
 * 
 * The XPath signature:
 *
 *     web:install-enabled($repo as item()) as xs:boolean
 * 
 * The parameter $repo must be a {@link RepositoryItem}.
 *
 * @author Florent Georges
 * @date   2013-09-16
 */
public class InstallEnabledFunction
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
        return FunTypes.types(FunTypes.SINGLE_ITEM);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.SINGLE_BOOLEAN;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new InstallEnabledCall();
    }

    static final String LOCAL_NAME = "install-enabled";
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
