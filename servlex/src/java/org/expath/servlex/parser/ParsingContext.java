/****************************************************************************/
/*  File:       ParsingContext.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.*;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.expath.servlex.model.Resource;
import org.expath.servlex.model.Wrapper;
import org.expath.servlex.processors.Processors;

/**
 * The context while parsing a webapp descriptor (aka expath-web.xml).
 *
 * @author Florent Georges
 * @date   2012-05-07
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

    public void addResource(Resource r) {
        myResources.add(r);
    }
    public List<Resource> getResources() {
        return myResources;
    }

    public void addServlet(ParsingServlet s) {
        myServlets.add(s);
    }
    public List<ParsingServlet> getServlets() {
        return myServlets;
    }

    public void addWrapper(Wrapper w) throws ParseException {
        QName n = w.getName();
        if ( n == null ) {
            parseError("Cannot have an anonymous top-level filter or chain");
        }
        if ( myWrappers.containsKey(n) ) {
            parseError("Cannot have two top-level filters or chains with the same name: " + n);
        }
        myWrappers.put(n, w);
    }

    public Wrapper getWrapper(QName n) throws ParseException {
        Wrapper w = myWrappers.get(n);
        if ( w == null ) {
            parseError("Top-level filter or chain dores not exist: " + n);
        }
        return w;
    }

    public void addChain(ParsingChain c) {
        myChains.add(c);
    }
    public List<ParsingChain> getChains() {
        return myChains;
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
    private static final Logger LOG = Logger.getLogger(ParsingContext.class);

    private Processors           myProcs         = null;
    private String               myAbbrev        = null;
    private String               myTitle         = null;
    private ParsingApp           myApp           = null;
    private List<Resource>       myResources     = new ArrayList<Resource>();
    private List<ParsingServlet> myServlets      = new ArrayList<ParsingServlet>();
    private List<ParsingChain>   myChains        = new ArrayList<ParsingChain>();
    private Stack<ParsingGroup>  myInScopeGroups = new Stack<ParsingGroup>();
    private Map<QName, Wrapper>  myWrappers      = new HashMap<QName, Wrapper>();
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
