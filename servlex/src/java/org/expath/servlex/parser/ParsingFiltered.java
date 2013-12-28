/****************************************************************************/
/*  File:       ParsingFiltered.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.ArrayList;
import java.util.List;
import org.expath.servlex.model.Chain;
import org.expath.servlex.model.Wrapper;

/**
 * Represent a filtered object while parsing.
 *
 * @author Florent Georges
 * @date   2013-09-16
 */
abstract class ParsingFiltered
{
    public void addFilter(String filter)
    {
        myFilters.add(filter);
    }

    public void setGroup(ParsingGroup group)
    {
        myGroup = group;
    }

    protected Wrapper[] getWrappers(ParsingContext ctxt)
            throws ParseException
    {
        List<ParsingWrapper> wrappers = new ArrayList<ParsingWrapper>();
        // add the wrappers from application, if any
        ParsingApp app = ctxt.getApplication();
        if ( app != null ) {
            addOthers(app.getFilters(ctxt), wrappers);
        }
        // add filters from group, if any
        if ( myGroup != null ) {
            addOthers(myGroup.getInScopeFilters(ctxt), wrappers);
        }
        // resolve and add filters declared on this servlet
        for ( String n : myFilters ) {
            addOther(ctxt.getWrapper(n), wrappers);
        }
        // instantiate them in an array of actual wrappers
        Wrapper[] array = new Wrapper[wrappers.size()];
        for ( int i = 0; i < wrappers.size(); ++i ) {
            array[i] = wrappers.get(i).makeIt(ctxt);
        }
        return array;
    }

    protected Wrapper makeWrapper(ParsingContext ctxt)
            throws ParseException
    {
        Wrapper[] wrappers = getWrappers(ctxt);
        // make the final wrapper (null, single wrapper, or wrapping chain)
        if ( wrappers.length == 0 ) {
            return null;
        }
        else if ( wrappers.length == 1 ) {
            return wrappers[0];
        }
        else {
            // return an anonymous chain
            return new Chain(null, wrappers);
        }
    }

    private void addOthers(List<ParsingWrapper> list, List<ParsingWrapper> wrappers)
    {
        for ( ParsingWrapper w : list ) {
            addOther(w, wrappers);
        }
    }

    private void addOther(ParsingWrapper w, List<ParsingWrapper> wrappers)
    {
        if ( w != this ) {
            wrappers.add(w);
        }
    }

    protected List<String> myFilters = new ArrayList<String>();

    private ParsingGroup myGroup = null;
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
