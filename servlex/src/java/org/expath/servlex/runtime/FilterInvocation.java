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
    public Connector invoke(Connector connector, ServerConfig config)
            throws ServlexException
                 , ComponentError
    {
        // inbound filter
        if ( myIn != null ) {
            connector = myIn.run(config, connector);
        }
        // the filtered component
        connector = myWrapped.invoke(connector, config);
        // outbound filter
        if ( myOut != null ) {
            connector = myOut.run(config, connector);
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
