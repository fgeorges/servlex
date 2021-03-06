/****************************************************************************/
/*  File:       ServletInvocation.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-08-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.runtime;

import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.model.Application;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.Cleanable;

/**
 * Represent an invocation of either a servlet or a resource.
 *
 * This is a wrapper around a compiled AddressHandler, gathering the runtime info.
 *
 * TODO: The above comment is wrong, fix it.
 *
 * @author Florent Georges
 */
public abstract class Invocation
        implements Cleanable
{
    public Invocation(String name, String path, RequestConnector request)
    {
        myName = name;
        myPath = path;
        myRequest = request;
    }

    public String getName()
    {
        return myName;
    }

    public String getPath()
    {
        return myPath;
    }

    public RequestConnector getRequest()
    {
        return myRequest;
    }

    public abstract Connector invoke(Connector connector, Application app, ServerConfig config, Auditor auditor)
            throws ServlexException
                 , ComponentError;

    private final String           myName;
    private final String           myPath;
    private final RequestConnector myRequest;
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
