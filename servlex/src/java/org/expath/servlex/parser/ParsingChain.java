/****************************************************************************/
/*  File:       ParsingChain.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import org.expath.servlex.model.Chain;
import org.expath.servlex.model.Wrapper;

/**
 * Represent a chain while parsing.
 *
 * @author Florent Georges
 * @date   2012-05-08
 */
class ParsingChain
        extends ParsingWrapper
{
    public ParsingChain(String name)
    {
        super(name);
    }

    @Override
    public Chain instantiate(ParsingContext ctxt)
            throws ParseException
    {
        String    name     = getName();
        Wrapper[] wrappers = getWrappers(ctxt);
        return new Chain(name, wrappers);
    }
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
