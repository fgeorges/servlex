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
import java.util.regex.Pattern;
import org.expath.servlex.components.Component;
import org.expath.servlex.model.AddressHandler;
import org.expath.servlex.model.Servlet;

/**
 * Represent a servlet while parsing.
 *
 * @author Florent Georges
 * @date   2012-05-07
 */
class ParsingServlet
        extends ParsingHandler
{
    public ParsingServlet(String name)
    {
        myName = name;
    }

    public void setImplem(Component implem)
    {
        myImplem = implem;
    }

    public void addMatchGroup(String g)
    {
        myMatchGroups.add(g);
    }

    @Override
    protected AddressHandler makeIt(Pattern regex, String java_regex)
    {
        String[] groups  = myMatchGroups.toArray(new String[]{ });
        Servlet  servlet = new Servlet(myName, myImplem, regex, groups);
        return servlet;
    }

    private String       myName        = null;
    private Component    myImplem      = null;
    private List<String> myMatchGroups = new ArrayList<String>();
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
