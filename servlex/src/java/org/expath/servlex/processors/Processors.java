/****************************************************************************/
/*  File:       Processors.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import javax.xml.transform.Source;
import org.expath.servlex.TechnicalException;

/**
 * Abstract the provider of XSLT, XQuery and XProc processors.
 *
 * @author Florent Georges
 */
public interface Processors
{
    public XSLTProcessor getXSLT()
            throws TechnicalException
                 , LanguageSupportException;

    public XQueryProcessor getXQuery()
            throws TechnicalException
                 , LanguageSupportException;

    public XProcProcessor getXProc()
            throws TechnicalException
                 , LanguageSupportException;

    public Serializer makeSerializer()
            throws TechnicalException;

    public TreeBuilder makeTreeBuilder(String uri, String prefix)
            throws TechnicalException;

    public Sequence emptySequence()
            throws TechnicalException;

    public Sequence buildSequence(Iterable<Item> items)
            throws TechnicalException;

    public Document buildDocument(Source src)
            throws TechnicalException;

    public Item buildString(String value)
            throws TechnicalException;

    public Item buildBinary(byte[] value)
            throws TechnicalException;

    /**
     * Return info about this processor, its configuration, anything relevant.
     * 
     * @return An array of string, suitable to be displayed as lines in logs.
     */
    public String[] info();
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
