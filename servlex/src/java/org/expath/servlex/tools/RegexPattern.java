/****************************************************************************/
/*  File:       RegexPattern.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.saxon.trans.XPathException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.tools.regex.JDK15RegexTranslator;
import org.expath.servlex.tools.regex.JRegularExpression;
import org.expath.servlex.tools.regex.RegexSyntaxException;
import org.expath.servlex.tools.regex.RegularExpression;

/**
 * Encapsulate XPath regex matching and replacing.
 *
 * @author Florent Georges
 */
public class RegexPattern
{
    public RegexPattern(String regex, Log log)
            throws TechnicalException
    {
        myLexical = regex;
        String jre = toJavaRegex(regex, log);
        myRegex = Pattern.compile(jre);
    }

    public RegexMatcher matcher(String value)
    {
        Matcher m = myRegex.matcher(value);
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
                JRegularExpression re = new JRegularExpression(myRegex);
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
        return "#<regex-pattern " + myLexical + ">";
    }

    /**
     * Translate an XPath regex to a native Java SE 1.5 regex.
     */
    private String toJavaRegex(String regex, Log log)
            throws TechnicalException
    {
        try {
            int options = RegularExpression.XPATH30;
            List<RegexSyntaxException> warnings = new ArrayList<>();
            String res = JDK15RegexTranslator.translate(regex, options, 0, warnings);
            for ( RegexSyntaxException w : warnings ) {
                log.info("XPath to Java regex compiler: Warning in regex: '" + w + "'");
            }
            return res;
        }
        catch ( RegexSyntaxException ex ) {
            throw new TechnicalException("The pattern is not a valid XPath regex", ex);
        }
    }

    private final Pattern myRegex;
    private final String  myLexical;
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
