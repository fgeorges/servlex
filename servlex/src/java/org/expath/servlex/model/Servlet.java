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
import org.apache.log4j.Logger;
import org.expath.servlex.ServlexException;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.runtime.ServletInvocation;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.tools.Auditor;


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

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        super.cleanup(auditor);
        auditor.cleanup("servlet " + myName);
        myImpl.cleanup(auditor);
    }

    public String getName()
    {
        return myName;
    }

    public Component getImplementation()
    {
        return myImpl;
    }

    public String[] getGroupNames()
    {
        return myGroups;
    }

    @Override
    public void logApplication(Logger log)
    {
        super.logApplication(log);
        log.debug("   (is a Servlet):");
        log.debug("      name   : " + myName);
        log.debug("      groups : " + myGroups);
        log.debug("      impl   : " + myImpl);
        if ( myImpl != null ) {
            myImpl.logApplication(log);
        }
    }

    @Override
    protected Invocation makeInvocation(String path, String method, RequestConnector connector)
    {
        connector.setServlet(this);
        return new ServletInvocation(myImpl, path, connector);
    }

    private String myName;
    private Component myImpl;
    /** Match group names (group[i] is null if it is not set). */
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
