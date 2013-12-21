/****************************************************************************/
/*  File:       ParsingHandler.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-13                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.model.AddressHandler;
import org.expath.servlex.model.Wrapper;
import org.expath.servlex.tools.RegexHelper;

/**
 * Represent an address handler while parsing.
 *
 * @author Florent Georges
 * @date   2013-09-13
 */
abstract class ParsingHandler
        extends ParsingFiltered
{
    public void setPattern(String pattern)
    {
        myPattern = pattern;
    }

    public AddressHandler makeAddressHandler(ParsingContext ctxt, Logger log)
            throws ParseException
    {
        String java_regex;
        try {
            java_regex = RegexHelper.xpathToJava(myPattern, log);
        }
        catch ( TechnicalException ex ) {
            throw new ParseException("The pattern is not a valid XPath regex", ex);
        }
        Pattern regex = Pattern.compile(java_regex);
        AddressHandler handler = makeIt(ctxt, regex, java_regex);
        Wrapper wrapper = makeWrapper(ctxt);
        handler.setWrapper(wrapper);
        return handler;
    }

    protected abstract AddressHandler makeIt(ParsingContext ctxt, Pattern regex, String java_regex);

    private String myPattern = null;
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
