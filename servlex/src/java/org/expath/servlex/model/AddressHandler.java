/****************************************************************************/
/*  File:       Servlet.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-08-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.RequestConnector;

/**
 * Abstract class that represents either a servlet or a resource.
 *
 * It has an address pattern, and provide a way to say whether it matches a
 * particular path.  It is part of one particular application.
 *
 * @author Florent Georges
 * @date   2010-08-17
 */
public abstract class AddressHandler
{
    public AddressHandler(Pattern url_pattern)
    {
        myPattern = url_pattern;
    }

    public Application getApplication()
    {
        return myApp;
    }

    void setApplication(Application app)
    {
        myApp = app;
    }

    public Invocation resolve(String path, String method, RequestConnector connector)
            throws ServlexException
    {
        Matcher m = myPattern.matcher(path);
        if ( m.matches() ) {
            connector.setMatcher(m);
            return makeInvocation(path, method, connector);
        }
        else {
            return null;
        }
    }

    protected abstract Invocation makeInvocation(String path, String method, RequestConnector connector)
            throws ServlexException;

    private Pattern myPattern;
    private Application myApp;
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
