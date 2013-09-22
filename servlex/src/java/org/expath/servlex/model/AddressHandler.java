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
import org.apache.log4j.Logger;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.Cleanable;

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
        implements Cleanable
{
    public AddressHandler(Pattern url_pattern)
    {
        myPattern = url_pattern;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("address handler");
        if ( myWrapper != null ) {
            myWrapper.cleanup(auditor);
        }
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
            Invocation invoc = makeInvocation(path, method, connector);
            if ( myWrapper != null ) {
                invoc = myWrapper.makeInvocation(path, connector, invoc);
            }
            return invoc;
        }
        else {
            return null;
        }
    }

    /**
     * Set a wrapper (filter, error handler, etc).
     * 
     * If more than one filter has to be set on this servlet, they can be all
     * wrapped within a chain, then this one single chain can be set as the
     * one wrapper.
     */
    public void setWrapper(Wrapper w)
    {
        myWrapper = w;
    }

    public void logApplication(Logger log)
    {
        log.debug("   Address Handler:");
        log.debug("      pattern: " + myPattern);
        log.debug("      wrapper: " + myWrapper);
        if ( myWrapper != null ) {
            myWrapper.logApplication(log);
        }
    }

    protected abstract Invocation makeInvocation(String path, String method, RequestConnector connector)
            throws ServlexException;

    private Pattern myPattern;
    private Application myApp;
    private Wrapper myWrapper = null;
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
