/****************************************************************************/
/*  File:       ParsingGroup.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.expath.servlex.model.Wrapper;

/**
 * Represent a group while parsing.
 *
 * @author Florent Georges
 * @date   2012-05-07
 */
class ParsingGroup
{
    public ParsingGroup(ParsingGroup parent)
    {
        myParent  = parent;
        myFilters = new ArrayList<QName>();
        myInScopeFilters = null;
    }

    public void addFilter(QName name)
    {
        myFilters.add(name);
    }

    /**
     * Return the filters declared on this group (using @filters).
     */
    public List<QName> getThisFilters()
    {
        return myFilters;
    }

    /**
     * Return all the filters applying on this group.
     * 
     * This method, as opposed to {@link #getThisFilters()}, take into account
     * the filters of parent and ancestor groups.  The first filters are the
     * one of the top-level ancestor, the last filters are the one declared on
     * this group.
     */
    public synchronized List<Wrapper> getInScopeFilters(ParsingContext ctxt)
            throws ParseException
    {
        if ( myInScopeFilters == null ) {
            myInScopeFilters = new ArrayList<Wrapper>();
            if ( myParent != null ) {
                List<Wrapper> from_parent = myParent.getInScopeFilters(ctxt);
                myInScopeFilters.addAll(from_parent);
            }
            for ( QName n : myFilters ) {
                Wrapper w = ctxt.getWrapper(n);
                myInScopeFilters.add(w);
            }
        }
        return myInScopeFilters;
    }

    private ParsingGroup  myParent;
    private List<QName>   myFilters;
    private List<Wrapper> myInScopeFilters;
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
