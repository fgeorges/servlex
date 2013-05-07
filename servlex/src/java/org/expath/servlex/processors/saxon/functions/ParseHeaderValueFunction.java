/****************************************************************************/
/*  File:       ParseHeaderValueFunction.java                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.processors.Processors;

/**
 * TODO: Doc...
 *
 *     web:parse-header-value($value as xs:string) as element(web:header)
 *
 * web:parse-header-value('text/html,application/xhtml+xml,application/xml;q=0.9,* /*;q=0.8')
 * =>
 *    <web:header>
 *       <web:element name="text/html"/>
 *       <web:element name="application/xhtml+xml"/>
 *       <web:element name="application/xml">
 *          <web:param name="q" value="0.9"/>
 *       </web:element>
 *       <web:element name="* /*">
 *          <web:param name="q" value="0.8"/>
 *       </web:element>
 *    </web:header>
 *
 * @author Florent Georges
 * @date   2010-11-26
 */
public class ParseHeaderValueFunction
        extends ExtensionFunctionDefinition
{
    public ParseHeaderValueFunction(Processors procs, Processor saxon)
    {
        myProcs = procs;
        mySaxon = saxon;
    }

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
        final int      one   = StaticProperty.EXACTLY_ONE;
        final ItemType itype = BuiltInAtomicType.STRING;
        SequenceType   stype = SequenceType.makeSequenceType(itype, one);
        return new SequenceType[]{ stype };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        final int      one    = StaticProperty.EXACTLY_ONE;
        final int      kind   = Type.ELEMENT;
        final String   uri    = ServlexConstants.WEBAPP_NS;
        final NamePool pool   = mySaxon.getUnderlyingConfiguration().getNamePool();
        final ItemType itype  = new NameTest(kind, uri, ELEMENT_NAME, pool);
        return SequenceType.makeSequenceType(itype, one);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new ParseHeaderValueCall(myProcs);
    }

    private static final String LOCAL_NAME = "parse-header-value";
    private static final String ELEMENT_NAME = "header";
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
