/****************************************************************************/
/*  File:       RegexPattern.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.expath.servlex.TechnicalException;

/**
 * Encapsulate XPath regex matching and replacing.
 *
 * @author Florent Georges
 */
public class RegexPattern
{
    public RegexPattern(String regex)
    {
        myRegex = regex;
    }

    public RegexMatcher matcher(String value)
            throws TechnicalException
    {
        Matcher m = toJavaMatcher(value);
        return new RegexMatcher(m, value);
    }

    public String replace(String value, String rewrite)
            throws TechnicalException
    {
        if ( rewrite == null ) {
            return value;
        }
        else {
            try {
                return toJavaMatcher(value).replaceAll(rewrite);
            }
            catch ( IndexOutOfBoundsException ex ) {
                throw new TechnicalException("Error replacing matches in pattern", ex);
            }
        }
    }

    @Override
    public String toString()
    {
        return "#<regex-pattern " + myRegex + ">";
    }

    private Matcher toJavaMatcher(String value)
            throws TechnicalException
    {
        Pattern p = Pattern.compile(myRegex);
        return p.matcher(value);
    }

    private final String myRegex;
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
