/****************************************************************************/
/*  File:       ParsingResource.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-13                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.regex.Pattern;
import org.expath.servlex.model.AddressHandler;
import org.expath.servlex.model.Resource;
import org.expath.servlex.tools.RegexPattern;

/**
 * Represent an address handler while parsing.
 *
 * @author Florent Georges
 */
class ParsingResource
        extends ParsingHandler
{
    public void setRewrite(String rewrite)
    {
        myRewrite = rewrite;
    }

    public void setMediaType(String type)
    {
        myMediaType = type;
    }

    @Override
    protected AddressHandler makeIt(ParsingContext ctxt, RegexPattern regex)
    {
        return new Resource(regex, myRewrite, myMediaType);
    }

    private String myRewrite;
    private String myMediaType;
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
