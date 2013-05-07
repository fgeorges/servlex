/****************************************************************************/
/*  File:       Component.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.components;

import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;

/**
 * A servlet entry point.
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public interface Component
{
    /**
     * Implement an entry point invocation.
     *
     * @param connector
     *          The input of the component. If Servlex is the direct caller,
     *          that means the web:request element and the HTTP request entity
     *          content (aka the bodies). It can be different if there are any
     *          filter or error handler in between.
     * 
     * @param config
     *          The server configuration object.
     * 
     * @param auditor
     *          The auditor object.
     * 
     * @return
     *          The result of the component. If Servlex is the direct caller,
     *          that must be the web:response element and the HTTP response
     *          entity content. It can be different if there are any filter or
     *          error handler in between.
     */
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
            throws ServlexException
                 , ComponentError;
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
