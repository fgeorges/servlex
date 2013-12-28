/****************************************************************************/
/*  File:       Connector.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.ComponentInstance;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.Cleanable;

/**
 * Encapsulate a connection between two components.
 *
 * @author Florent Georges
 */
public interface Connector
        extends Cleanable
{
    /**
     * Return the auditor object used by this connector.
     */
    public Auditor getAuditor();

    /**
     * Connect to an XQuery function.
     */
    public void connectToXQueryFunction(ComponentInstance comp, ServerConfig config)
            throws ServlexException;

    /**
     * Connect to an XQuery main module.
     */
    public void connectToQuery(ComponentInstance comp, ServerConfig config)
            throws ServlexException;

    /**
     * Connect to an XSLT component, either a function or a named template.
     */
    public void connectToXSLTComponent(ComponentInstance comp, ServerConfig config)
            throws ServlexException;

    /**
     * Connect to an XSLT stylesheet.
     */
    public void connectToStylesheet(ComponentInstance comp, ServerConfig config)
            throws ServlexException;

    /**
     * Connect to an XProc pipeline.
     */
    public void connectToPipeline(ComponentInstance comp, ServerConfig config)
            throws ServlexException;

    /**
     * Connect to the final HTTP Servlet response.
     */
    public void connectToResponse(HttpServletResponse resp, ServerConfig config, Processors procs)
            throws ServlexException, IOException;
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
