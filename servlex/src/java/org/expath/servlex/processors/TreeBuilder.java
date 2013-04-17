/****************************************************************************/
/*  File:       TreeBuilder.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import net.sf.saxon.s9api.XdmNode;
import org.expath.servlex.TechnicalException;

/**
 * A generic interface to build an XML tree, independent on any processor.
 *
 * @author Florent Georges
 * @date   2013-04-16
 */
public interface TreeBuilder
{
    public void startElem(String local)
            throws TechnicalException;

    public void attribute(String local, String value)
            throws TechnicalException;

    public void startContent()
            throws TechnicalException;

    public void characters(String value)
            throws TechnicalException;

    public void endElem()
            throws TechnicalException;

    public void textElem(String local, String value)
            throws TechnicalException;

    // TODO: FIXME: XdmNode is Saxon-specific, this has NOTHING to do here...!
    public XdmNode getRoot()
            throws TechnicalException;
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
