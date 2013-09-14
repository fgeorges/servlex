/****************************************************************************/
/*  File:       ParsingHandler.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-13                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.model.AddressHandler;
import org.expath.servlex.model.Chain;
import org.expath.servlex.model.Wrapper;
import org.expath.servlex.tools.RegexHelper;

/**
 * Represent an address handler while parsing.
 *
 * @author Florent Georges
 * @date   2013-09-13
 */
abstract class ParsingHandler
{
    public void addFilter(QName filter)
    {
        myFilters.add(filter);
    }

    public void setGroup(ParsingGroup group)
    {
        myGroup = group;
    }

    public void setPattern(String pattern)
    {
        myPattern = pattern;
    }

    public AddressHandler makeAddressHandler(ParsingContext ctxt, Logger log)
            throws ParseException
    {
        String  java_regex;
        Pattern regex;
        try {
            java_regex = RegexHelper.xpathToJava(myPattern, log);
            regex = Pattern.compile(java_regex);
        }
        catch ( TechnicalException ex ) {
            throw new ParseException("The pattern is not a valid XPath regex", ex);
        }
        AddressHandler handler = makeIt(regex, java_regex);
        Wrapper wrapper = makeWrapper(ctxt);
        handler.setWrapper(wrapper);
        return handler;
    }

    protected abstract AddressHandler makeIt(Pattern regex, String java_regex);

    private Wrapper makeWrapper(ParsingContext ctxt)
            throws ParseException
    {
        // initiate the list with the wrappers from application, if any
        ParsingApp app = ctxt.getApplication();
        List<Wrapper> wrappers;
        if ( app == null ) {
            wrappers = new ArrayList<Wrapper>();
        }
        else {
            List<Wrapper> from_app = app.getFilters(ctxt);
            wrappers = new ArrayList<Wrapper>(from_app);
        }
        // add filters from group, if any
        if ( myGroup != null ) {
            List<Wrapper> from_group = myGroup.getInScopeFilters(ctxt);
            wrappers.addAll(from_group);
        }
        // resolve and add filters declared on this servlet
        for ( QName n : myFilters ) {
            Wrapper w = ctxt.getWrapper(n);
            wrappers.add(w);
        }
        // make the final wrapper (null, single wrapper, or wrapping chain)
        if ( wrappers.isEmpty() ) {
            return null;
        }
        else if ( wrappers.size() == 1 ) {
            return wrappers.get(0);
        }
        else {
            // return an anonymous chain
            return new Chain(null, wrappers.toArray(new Wrapper[]{}));
        }
    }

    private ParsingGroup myGroup   = null;
    private List<QName>  myFilters = new ArrayList<QName>();
    private String       myPattern = null;
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
