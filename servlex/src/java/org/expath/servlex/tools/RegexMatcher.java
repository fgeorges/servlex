/****************************************************************************/
/*  File:       RegexMatcher.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.regex.Matcher;

/**
 * Encapsulate matching of XPath regex subparts.
 *
 * @author Florent Georges
 */
public class RegexMatcher
{
    public RegexMatcher(Matcher matcher, String value)
    {
        myValue = value;
        myLen = value.length();
        myMatcher = matcher;
        myCount = matcher.groupCount();
    }

    public boolean matches()
    {
        return myMatcher.matches();
    }

    public String next()
    {
        if ( myGroup > myCount ) {
            if ( myLastIndex < myLen ) {
                String res = myValue.substring(myLastIndex);
                myLastIndex = myLen;
                return res;
            }
            else {
                return null;
            }
        }
        int s = myMatcher.start(myGroup);
        if ( myPreGroup && myLastIndex < myMatcher.start(myGroup) ) {
            myPreGroup = false;
            String res = myValue.substring(myLastIndex, s);
            myLastIndex = s;
            return res;
        }
        if ( myMatcher.group(myGroup) == null ) {
            myPreGroup = true;
            ++myGroup;
            return next();
        }
        else {
            myPreGroup = true;
            ++myGroup;
            myLastIndex = myMatcher.end(myGroup);
            return myMatcher.group(myGroup);
        }
    }

    public boolean isGroup()
    {
        return ! myPreGroup;
    }

    public int groupNumber()
    {
        if ( ! isGroup() ) {
            throw new IllegalStateException("Cannot ask the number group when not on a group");
        }
        return myGroup - 1;
    }

    private final String myValue;
    private final Matcher myMatcher;
    private final int myCount;
    private final int myLen;
    private int myLastIndex = 0;
    private int myGroup = 1;
    private boolean myPreGroup = true;
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
