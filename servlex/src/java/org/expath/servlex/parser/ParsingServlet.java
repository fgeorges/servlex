/****************************************************************************/
/*  File:       ParsingServlet.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.expath.servlex.components.Component;
import org.expath.servlex.model.Chain;
import org.expath.servlex.model.Wrapper;

/**
 * Represent a servlet while parsing.
 *
 * @author Florent Georges
 * @date   2012-05-07
 */
class ParsingServlet
{
    public ParsingServlet(String name, ParsingGroup group)
    {
        myName        = name;
        myGroup       = group;
        myFilters     = new ArrayList<QName>();
        myMatchGroups = new ArrayList<String>();
    }

    public Wrapper makeWrapper(ParsingContext ctxt)
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

    public String getName() {
        return myName;
    }

    public void addFilter(QName filter) {
        myFilters.add(filter);
    }

    public Component getImplem() {
        return myImplem;
    }
    public void setImplem(Component implem) {
        myImplem = implem;
    }

    public String getPattern() {
        return myPattern;
    }
    public void setPattern(String pattern) {
        myPattern = pattern;
    }

    public String[] getMatchGroups() {
        return myMatchGroups.toArray(new String[]{});
    }
    public void addMatchGroup(String g) {
        myMatchGroups.add(g);
    }

    private String       myName;
    private ParsingGroup myGroup;
    private List<QName>  myFilters;
    private Component    myImplem;
    private String       myPattern;
    private List<String> myMatchGroups;
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
