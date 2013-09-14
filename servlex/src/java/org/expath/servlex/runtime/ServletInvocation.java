/****************************************************************************/
/*  File:       ServletInvocation.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.runtime;

import org.expath.servlex.components.Component;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.model.Application;
import org.expath.servlex.tools.Auditor;

/**
 * Represent a specific invocation of an application's servlet, at a specific URI.
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public class ServletInvocation
        extends Invocation
{
    public ServletInvocation(Component impl, String path, RequestConnector request)
    {
        super(path, request);
        myImpl = impl;
    }

    @Override
    public Connector invoke(Connector connector, Application app, ServerConfig config, Auditor auditor)
            throws ServlexException
                 , ComponentError
    {
        return myImpl.run(connector, config, auditor);
    }

    /** ... */
    private Component myImpl;
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
