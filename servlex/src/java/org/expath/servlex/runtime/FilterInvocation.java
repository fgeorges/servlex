/****************************************************************************/
/*  File:       FilterInvocation.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.runtime;

import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.Component;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.model.Application;
import org.expath.servlex.tools.Auditor;

/**
 * The invocation of a filter, with its filtering components and it wrapped invocation.
 *
 * @author Florent Georges
 * @date   2011-02-07
 */
public class FilterInvocation
        extends Invocation
{
    public FilterInvocation(Component in, Component out, Invocation wrapped, String path, RequestConnector request)
    {
        super(path, request);
        myIn = in;
        myOut = out;
        myWrapped = wrapped;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("filter invocation");
        myWrapped.cleanup(auditor);
        if ( myIn != null ) {
            myIn.cleanup(auditor);
        }
        if ( myOut != null ) {
            myOut.cleanup(auditor);
        }
    }

    @Override
    public Connector invoke(Connector connector, Application app, ServerConfig config, Auditor auditor)
            throws ServlexException
                 , ComponentError
    {
        auditor.invoke("filter");
        // inbound filter
        if ( myIn != null ) {
            // TODO: If this returns a web:response, we should return straight
            // to the client, without calling the filtered component...
            // Use case: an authentication filter (if non authenticated, the
            // filter returns an authentication demand to the client).
            connector = myIn.run(connector, config, auditor);
        }
        // the filtered component
        connector = myWrapped.invoke(connector, app, config, auditor);
        // outbound filter
        if ( myOut != null ) {
            connector = myOut.run(connector, config, auditor);
        }
        // return the filtered result
        return connector;
    }

    private Component myIn;
    private Component myOut;
    private Invocation myWrapped;
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
