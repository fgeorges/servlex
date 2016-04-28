/****************************************************************************/
/*  File:       ExecuteFunction.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.value.SequenceType;
import org.expath.servlex.processors.Processors;

/**
 * TODO: Doc...
 *
 *     web:execute($exec as element(web:exec-program)) as element(web:exec-result)
 * 
 * &lt;exec-program&gt;
 *    &lt;cwd&gt;/home/servlex/expath-web-content&lt;/cwd&gt;
 *    &lt;program&gt;git&lt;/program&gt;
 *    &lt;option&gt;update&lt;/option&gt;
 * &lt;/exec-program&gt;
 * 
 * &lt;exec-result code="0"&gt;
 *    &lt;stdout&gt;...&lt;/stdout&gt;
 *    &lt;stderr&gt;...&lt;/stderr&gt;
 * &lt;/exec-result&gt;
 * 
 * TODO: Explicitly allow some webapps to use this extension (through a white
 * list, in system properties).
 * 
 * @author Florent Georges
 */
public class ExecuteFunction
        extends ExtensionFunctionDefinition
{
    public ExecuteFunction(Processors procs, Processor saxon)
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
        return FunTypes.types(
                FunTypes.single_element(PROGRAM_NAME, mySaxon));
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.single_element(RESULT_NAME, mySaxon);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new ExecuteCall(myProcs);
    }

    static final String LOCAL_NAME = "execute";

    private static final String PROGRAM_NAME = "exec-program";
    private static final String RESULT_NAME  = "exec-result";
    private final Processor  mySaxon;
    private final Processors myProcs;
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
