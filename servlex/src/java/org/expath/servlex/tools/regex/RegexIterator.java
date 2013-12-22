package org.expath.servlex.tools.regex;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

/**
 * This class is an interator that supports the evaluation of xsl:analyze-string.
 * It returns all the matching and non-matching substrings in an input string, and
 * provides access to their captured groups
 */
public interface RegexIterator extends SequenceIterator {

    /**
     * Determine whether the current item in the sequence is a matching item or a non-matching item
     * @return true if the current item is a matching item
     */

    public boolean isMatching();

    /**
    * Get a substring that matches a parenthesised group within the regular expression
    * @param number    the number of the group to be obtained
    * @return the substring of the current item that matches the n'th parenthesized group
    * within the regular expression
    */

    /*@Nullable*/ public String getRegexGroup(int number);

    /**
     * Get a sequence containing all the regex captured groups relating to the current matching item
     * (except group 0, because we want to use indexing from 1).
     * This is used by the saxon:analyze-string() higher-order extension function.
     */

    public SequenceIterator getRegexGroupIterator();

    /**
     * Process a matching substring, performing specified actions at the start and end of each matching
     * group
     */

    public void processMatchingSubstring(XPathContext context, OnGroup action) throws XPathException;

    /**
     * Interface defining a call-back action for processing captured groups
     */

    public static interface OnGroup {

        /**
         * Method to be called when the start of a captured group is encountered
         * @param c the dynamic evaluation context
         * @param groupNumber the group number of the captured group
         */

        public void onGroupStart(XPathContext c, int groupNumber) throws XPathException;

       /**
         * Method to be called when the end of a captured group is encountered
         * @param c the dynamic evaluation context
         * @param groupNumber the group number of the captured group
         */

        public void onGroupEnd(XPathContext c, int groupNumber) throws XPathException;
    }

}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Saxonica Limited.
// Portions created by ___ are Copyright (C) ___. All rights reserved.
//
// Contributor(s):
//