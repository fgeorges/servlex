/****************************************************************************/
/*  File:       SaxonXSLT.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import org.expath.servlex.processors.saxon.components.SaxonXSLTTransform;
import org.expath.servlex.processors.saxon.components.SaxonXSLTFunction;
import org.expath.servlex.processors.saxon.components.SaxonXSLTTemplate;
import net.sf.saxon.s9api.Processor;
import org.expath.servlex.components.Component;
import org.expath.servlex.processors.XSLTProcessor;

/**
 * The Saxon implementation of the XSLT processor.
 *
 * @author Florent Georges
 * @date   2013-04-15
 */
class SaxonXSLT
        implements XSLTProcessor
{
    public SaxonXSLT(Processor saxon)
    {
        mySaxon = saxon;
    }

    public Component makeTransform(String uri)
    {
        return new SaxonXSLTTransform(mySaxon, uri);
    }

    public Component makeFunction(String uri, String ns, String localname)
    {
        return new SaxonXSLTFunction(mySaxon, uri, ns, localname);
    }

    public Component makeTemplate(String uri, String ns, String localname)
    {
        return new SaxonXSLTTemplate(mySaxon, uri, ns, localname);
    }

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
