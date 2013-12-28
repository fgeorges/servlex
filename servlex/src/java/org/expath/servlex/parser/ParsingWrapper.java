/****************************************************************************/
/*  File:       ParsingWrapper.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import org.expath.servlex.model.Wrapper;

/**
 * Represent a wrapper while parsing.
 *
 * @author Florent Georges
 */
abstract class ParsingWrapper
        extends ParsingFiltered
{
    public ParsingWrapper(String name)
    {
        myName = name;
    }

    public String getName()
    {
        return myName;
    }

    public Wrapper makeIt(ParsingContext ctxt)
            throws ParseException
    {
        Wrapper w = ctxt.getWrapper(this);
        if ( w == null ) {
            w = instantiate(ctxt);
            ctxt.addWrapper(this, w);
        }
        return w;
    }

    public abstract Wrapper instantiate(ParsingContext ctxt)
            throws ParseException;

    private String myName;
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
