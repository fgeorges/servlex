/****************************************************************************/
/*  File:       TMP_ZipEntryAsXmlFunction.java                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.value.SequenceType;

/**
 * Implements web:tmp--zip-entry-as-xml().
 * 
 * WARNING: This is a temporary function, to be used until we adapt Servlex to
 * Saxon 9.5 (the latest version of the ZIP module, the only one supporting a
 * ZIP file as an in-memory binary, has been developed only for 9.5).  By the
 * way, this raise the question of making most extension libraries available for
 * several major versions of Saxon, especially on CXAN...
 * 
 *     web:tmp--zip-entry-as-xml($zip   as xs:base64Binary,
 *                               $entry as xs:string) as document-node?
 *
 * @author Florent Georges
 * @date   2013-12-19
 */
public class TMP_ZipEntryAsXmlFunction
        extends ExtensionFunctionDefinition
{
    public TMP_ZipEntryAsXmlFunction(Processor saxon)
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
        return FunTypes.types(FunTypes.SINGLE_BASE64, FunTypes.SINGLE_STRING);
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        return FunTypes.OPTIONAL_DOCUMENT;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new TMP_ZipEntryAsXmlCall(mySaxon);
    }

    /** The function local name. */
    static final String LOCAL_NAME = "tmp--zip-entry-as-xml";
    /** The Saxon processor object. */
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
