/****************************************************************************/
/*  File:       RequestConnector.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import org.expath.servlex.tools.RequestParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.model.Servlet;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.components.ComponentInstance;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.RegexMatcher;

/**
 * Connector to the HTTP servlet request object.
 *
 * @author Florent Georges
 * @date   2011-02-06
 */
public class RequestConnector
        implements Connector
{
    public RequestConnector(HttpServletRequest request, String path, String appname, Processors procs, Auditor auditor)
    {
        myParser  = new RequestParser(request, path, appname, procs);
        myProcs   = procs;
        myAuditor = auditor;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("request");
        myServlet.cleanup(auditor);
    }

    @Override
    public Auditor getAuditor()
    {
        return myAuditor;
    }

    public void setMatcher(RegexMatcher matcher)
    {
        myParser.setMatcher(matcher);
    }

    public void setServlet(Servlet servlet)
    {
        myServlet = servlet;
        myParser.setServlet(servlet);
    }

    // TODO: I should be able to remove the vairable myWebRequest (just put it
    // as first item in the sequence, no need to keep a pointer to it, but it
    // is used here...)
    public Document getWebRequest(ServerConfig config)
            throws ServlexException
    {
        ensureParsing(config);
        return myWebRequest;
    }

    private synchronized void ensureParsing(ServerConfig config)
            throws ServlexException
    {
        if ( myInput == null ) {
            try {
                // where to put the web:request element
                TreeBuilder builder = myProcs.makeTreeBuilder(NS_URI, NS_PREFIX);
                // parse the request (to web:request + sequence of bodies)
                // (parseRequest() puts everything in the list, and returns the
                // web:request document node)
                List<Item> input = new ArrayList<>();
                boolean trace_content = config.isTraceContentEnabled();
                myWebRequest = myParser.parse(builder, input, trace_content);
                myInput = myProcs.buildSequence(input);
            }
            catch ( TechnicalException ex ) {
                error(500, "Internal error", ex);
            }
        }
    }

    @Override
    public void connectToXQueryFunction(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("request", "xquery function");
        ensureParsing(config);
        try {
            comp.connect(myInput);
        }
        catch ( TechnicalException ex ) {
            error(500, "Internal error", ex);
        }
    }

    @Override
    public void connectToQuery(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("request", "query");
        ensureParsing(config);
        try {
            comp.connect(myInput);
        }
        catch ( TechnicalException ex ) {
            error(500, "Internal error", ex);
        }
    }

    @Override
    public void connectToXSLTComponent(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("request", "xslt component");
        ensureParsing(config);
        try {
            comp.connect(myInput);
        }
        catch ( TechnicalException ex ) {
            error(500, "Internal error", ex);
        }
    }

    @Override
    public void connectToStylesheet(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("request", "style");
        ensureParsing(config);
        try {
            comp.connect(myInput);
        }
        catch ( TechnicalException ex ) {
            error(500, "Internal error", ex);
        }
    }

    @Override
    public void connectToPipeline(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("request", "pipeline");
        ensureParsing(config);
        try {
            comp.connect(myInput);
        }
        catch ( TechnicalException ex ) {
            error(500, "Internal error", ex);
        }
    }

    /**
     * Throws an error, as a request cannot be connected directly to the response.
     */
    @Override
    public void connectToResponse(HttpServletResponse resp, ServerConfig config, Processors procs)
            throws ServlexException
                 , IOException
    {
        throw new ServlexException(500, "A request cannot be connected to the response.");
    }

    private void error(int code, String msg, Throwable ex)
            throws ServlexException
    {
        LOG.error(code + ": " + msg, ex);
        throw new ServlexException(code, msg, ex);
    }

    /** The EXPath servlet namespace URI. */
    private static final String NS_URI    = "http://expath.org/ns/webapp";
    /** The usual EXPath servlet namespace prefix. */
    private static final String NS_PREFIX = "web";
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(RequestConnector.class);

    /** The request parser. */
    private final RequestParser myParser;
    /** The auditor object. */
    private final Auditor myAuditor;
    /** The processors to use. */
    private final Processors myProcs;

    /** The servlet to serve this request. */
    private Servlet myServlet = null;
    /** The all input sequence, that is, the request element followed by bodies. */
    private Sequence myInput = null;
    /** The web:request document node, null at beginning, placed here when parsed. */
    private Document myWebRequest = null;
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
