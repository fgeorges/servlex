/****************************************************************************/
/*  File:       ParsingFilter.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import org.expath.servlex.components.Component;
import org.expath.servlex.model.Filter;

/**
 * Represent a chain while parsing.
 *
 * @author Florent Georges
 */
class ParsingFilter
        extends ParsingWrapper
{
    public ParsingFilter(String name, Component in, Component out)
    {
        super(name);
        myIn  = in;
        myOut = out;
    }

    @Override
    public Filter instantiate(ParsingContext ctxt)
            throws ParseException
    {
        String name = getName();
        return new Filter(name, myIn, myOut);
    }

    private Component myIn;
    private Component myOut;
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
