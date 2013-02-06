/****************************************************************************/
/*  File:       Servlet.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import org.expath.servlex.components.Component;
import java.util.regex.Pattern;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.runtime.ServletInvocation;
import org.expath.servlex.connectors.RequestConnector;


/**
 * A servlet in an application, bound to a URI pattern.
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public class Servlet
        extends AddressHandler
{
    public Servlet(String name, Component implem, Pattern url_pattern, String[] groups)
    {
        super(url_pattern);
        myName = name;
        myImpl = implem;
        myGroups = groups;
    }

    public String getName()
    {
        return myName;
    }

    public Component getImplementation()
    {
        return myImpl;
    }

    /**
     * Set a wrapper (filter, error handler, etc.)
     * 
     * If more than one filter has to be set on this servlet, you can wrap them
     * all within a chain, then set this one single chain as the wrapper.
     */
    public void setWrapper(Wrapper w)
    {
        myWrapper = w;
    }

    public Wrapper getWrapper()
    {
        return myWrapper;
    }

    public String[] getGroupNames()
    {
        return myGroups;
    }

    @Override
    protected Invocation makeInvocation(String path, String method, RequestConnector connector)
    {
        connector.setServlet(this);
        Invocation invoc = new ServletInvocation(myImpl, path, connector);
        if ( myWrapper != null ) {
            invoc = myWrapper.makeInvocation(path, connector, invoc);
        }
        return invoc;
    }

    private String myName;
    private Component myImpl;
    private Wrapper myWrapper = null;
    // match group names (null if group[i] not set)
    private String[] myGroups;
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
