/****************************************************************************/
/*  File:       ParsingContext.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.expath.servlex.model.Wrapper;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.tools.Log;

/**
 * The context while parsing a webapp descriptor (aka expath-web.xml).
 *
 * @author Florent Georges
 */
class ParsingContext
{
    public void setProcessors(Processors procs) {
        myProcs = procs;
    }
    public Processors getProcessors() {
        return myProcs;
    }

    public void setAbbrev(String a) {
        myAbbrev = a;
    }
    public String getAbbrev() {
        return myAbbrev;
    }

    public void setTitle(String t) throws ParseException {
        if ( myTitle != null ) {
            parseError("Title already set; old: " + myTitle + ", new: " + t);
        }
        myTitle = t;
    }
    public String getTitle() {
        return myTitle;
    }

    public void setApplication(ParsingApp a) throws ParseException {
        if ( myApp != null ) {
            parseError("Only one application element allowed");
        }
        myApp = a;
    }
    public ParsingApp getApplication() {
        return myApp;
    }

    public void setBase(URI b) throws ParseException {
        myBase = b;
    }
    public URI getBase() {
        return myBase;
    }

    public void addConfigParam(ParsingConfigParam c) {
        String id = c.getId();
        ParsingConfigParam existing = myConfigParams.get(id);
        if ( existing == null ) {
            myConfigParams.put(id, c);
        }
        else {
            existing.setName(c.getName());
            existing.setDescription(c.getDescription());
        }
    }
    public Map<String, ParsingConfigParam> getConfigParams() {
        return myConfigParams;
    }

    public void addHandler(ParsingHandler h) {
        myHandlers.add(h);
    }
    public List<ParsingHandler> getHandlers() {
        return myHandlers;
    }

    public Collection<ParsingWrapper> getWrappers() {
        return myWrappers.values();
    }

    public void addWrapper(ParsingWrapper w) throws ParseException {
        String n = w.getName();
        if ( n == null ) {
            parseError("Cannot have an anonymous top-level filter or chain");
        }
        if ( myWrappers.containsKey(n) ) {
            parseError("Cannot have two top-level filters or chains with the same name: " + n);
        }
        myWrappers.put(n, w);
    }

    public ParsingWrapper getWrapper(String n) throws ParseException {
        ParsingWrapper w = myWrappers.get(n);
        if ( w == null ) {
            parseError("Top-level filter or chain dores not exist: " + n);
        }
        return w;
    }

    public void addWrapper(ParsingWrapper pw, Wrapper w) throws ParseException {
        if ( myActualWrappers.containsKey(pw) ) {
            parseError("Try to create twice the same wrapper");
        }
        myActualWrappers.put(pw, w);
    }

    public Wrapper getWrapper(ParsingWrapper w) throws ParseException {
        return myActualWrappers.get(w);
    }

    public void pushGroup(ParsingGroup g) {
        myInScopeGroups.push(g);
    }
    public ParsingGroup popGroup() {
        return myInScopeGroups.pop();
    }
    public ParsingGroup getCurrentGroup() {
        if ( myInScopeGroups.empty() ) {
            return null;
        }
        return myInScopeGroups.peek();
    }

    private static void parseError(String msg)
            throws ParseException
    {
        LOG.error(msg);
        throw new ParseException(msg);
    }

    /** The logger. */
    private static final Log LOG = new Log(ParsingContext.class);

    private Processors myProcs  = null;
    private String     myAbbrev = null;
    private String     myTitle  = null;
    private ParsingApp myApp    = null;
    private URI        myBase   = null;
    private final Map<String, ParsingConfigParam> myConfigParams   = new HashMap<>();
    private final List<ParsingHandler>            myHandlers       = new ArrayList<>();
    private final Stack<ParsingGroup>             myInScopeGroups  = new Stack<>();
    private final Map<String, ParsingWrapper>     myWrappers       = new HashMap<>();
    private final Map<ParsingWrapper, Wrapper>    myActualWrappers = new HashMap<>();
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
