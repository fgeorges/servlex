/****************************************************************************/
/*  File:       RegexHelper.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.functions.regex.JDK15RegexTranslator;
import net.sf.saxon.functions.regex.JRegularExpression;
import net.sf.saxon.functions.regex.RegexSyntaxException;
import net.sf.saxon.functions.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.Logger;
import org.expath.servlex.TechnicalException;

/**
 * Helper for XPath regexes (implementation depends on Saxon).
 *
 * @author Florent Georges
 * @date   2013-05-06
 */
public class RegexHelper
{
    /**
     * Translate an XPath regex to a native Java SE 1.5 regex.
     */
    public static String xpathToJava(String regex, Logger logger)
            throws TechnicalException
    {
        try {
            int options = RegularExpression.XML11 | RegularExpression.XPATH20;
            List<RegexSyntaxException> warnings = new ArrayList<RegexSyntaxException>();
            String res = JDK15RegexTranslator.translate(regex, options, 0, warnings);
            for ( RegexSyntaxException w : warnings ) {
                logger.warn("expath-web.xml parser: Warning in regex: '" + w + "'");
            }
            return res;
        }
        catch ( RegexSyntaxException ex ) {
            throw new TechnicalException("The pattern is not a valid XPath regex", ex);
        }
    }

    /**
     * Replace the matches in {@code value}, given the {@code regex} and the {@code rewrite} string.
     * 
     * If {@code rewrite} is null, the value is returned as is.  The {@code regex}
     * is a Java regex.
     */
    public static String replaceMatches(String value, String regex, String rewrite)
            throws TechnicalException
    {
        if ( rewrite == null ) {
            return value;
        }
        else {
            try {
                JRegularExpression re = new JRegularExpression(regex, 0);
                return re.replace(value, rewrite).toString();
            }
            catch ( XPathException ex ) {
                throw new TechnicalException("Error replacing matches in pattern", ex);
            }
        }
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
