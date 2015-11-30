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
import net.sf.saxon.regex.JavaRegularExpression;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
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
        String jre = toJavaRegex(myRegex);
        Pattern p = Pattern.compile(jre);
        Matcher m = p.matcher(value);
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
                RegularExpression re = new JavaRegularExpression(myRegex, "");
                return re.replace(value, rewrite).toString();
            }
            catch ( XPathException ex ) {
                throw new TechnicalException("Error replacing matches in pattern", ex);
            }
        }
    }

    @Override
    public String toString()
    {
        return "#<regex-pattern " + myRegex + ">";
    }

    /**
     * Translate an XPath regex to a native Java SE 1.5 regex.
     */
    private String toJavaRegex(String regex)
            throws TechnicalException
    {
        try {
            JavaRegularExpression re = new JavaRegularExpression(regex, "");
            return re.getJavaRegularExpression();
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("The pattern is not a valid XPath regex", ex);
        }
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
