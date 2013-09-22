/****************************************************************************/
/*  File:       ErrorConnector.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.components.ComponentInstance;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;

/**
 * Connect an XPath error to component implementations.
 *
 * @author Florent Georges
 * @date   2011-02-09
 */
public class ErrorConnector
        implements Connector
{
    /**
     * Build a new object, based on the XPath error (name, message and sequence).
     */
    public ErrorConnector(ComponentError error, RequestConnector request, Auditor auditor)
    {
        myError   = error;
        myRequest = request;
        myAuditor = auditor;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("error");
        myRequest.cleanup(auditor);
    }

    @Override
    public Auditor getAuditor()
    {
        return myAuditor;
    }

    /**
     * TODO: Mapping to define, then implement.
     *
     * FIXME: And BOOM!  The mapping here is pretty straightforward: the function
     * accepts 3 parameters: {@code f($errname, $errmsg, $errobject)}.  But the
     * signature really is defined by the component itself (in this case, this is
     * in XQueryFunction), which says: {@code f($request, $bodies)} for all cases.
     *
     * The solution: move the "call sheet" from the components to the connectors
     * (the "call sheets" are the stylesheet generated to get global parameters
     * and pass them to a function or template, importing the original stylesheet
     * by using its public import URI).  Because clearly, the call sheet is part
     * of the mapping.  For pipelines that's a bit different as the ports can be
     * access through the API of XPipeline, passed to the connectors.
     */
    @Override
    public void connectToXQueryFunction(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectToQuery(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToXSLTComponent(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToStylesheet(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("error", "style");
        try {
            Document request = myRequest.getWebRequest(config);
            comp.error(myError, request);
        }
        catch ( TechnicalException ex ) {
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToPipeline(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("error", "pipeline");
        try {
            Document request = myRequest.getWebRequest(config);
            comp.error(myError, request);
        }
        catch ( TechnicalException ex ) {
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToResponse(HttpServletResponse resp, ServerConfig config, Processors procs)
            throws ServlexException
                 , IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private ComponentError   myError;
    private RequestConnector myRequest;
    /** The auditor object. */
    private Auditor          myAuditor;
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
