/****************************************************************************/
/*  File:       Servlex.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.model.Application;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.parser.ParseException;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.Properties;


/**
 * TODO: ...
 *
 * @author Florent Georges
 * @date   2009-12-10
 */
public class Servlex
        extends HttpServlet
{
    /**
     * Get the request properties.
     * 
     * TODO: Use a Properties object, like getServerMap().
     */
    public static Properties getRequestMap()
            throws TechnicalException
    {
        HttpServletRequest request = myCurrentRequest.get();
        Object obj = request.getAttribute(REQUEST_MAP_ATTR);
        if ( obj == null ) {
            Properties props = new Properties("web:");
            // TODO: Add a request unique identifier in the properties, in order to identify a
            // request uniquely, e.g. to create file name for the audit..., say "web:request-id"...
            try {
                String now  = NOW_FORMAT.format(new Date());
                String uuid = UUID.randomUUID().toString();
                props.setPrivate("web:request-id", now + "-" + uuid);
            }
            catch ( TechnicalException ex ) {
                throw new TechnicalException("Unexpected exception", ex);
            }
            request.setAttribute(REQUEST_MAP_ATTR, props);
            return props;
        }
        else if ( ! ( obj instanceof Properties ) ) {
            throw new TechnicalException(REQUEST_MAP_ATTR + " is invalid: " + obj.getClass());
        }
        else {
            return ( Properties ) obj;
        }
    }

    /**
     * Get the session properties.
     * 
     * TODO: Use a Properties object, like getServerMap().
     */
    public static Properties getSessionMap()
            throws TechnicalException
    {
        HttpServletRequest request = myCurrentRequest.get();
        HttpSession session = request.getSession();
        Object obj = session.getAttribute(SESSION_MAP_ATTR);
        if ( obj == null ) {
            Properties props = new Properties("web:");
            session.setAttribute(SESSION_MAP_ATTR, props);
            return props;
        }
        else if ( ! ( obj instanceof Properties ) ) {
            throw new TechnicalException(SESSION_MAP_ATTR + " is invalid: " + obj.getClass());
        }
        else {
            return ( Properties ) obj;
        }
    }

    /**
     * Get the webapp properties.
     * 
     * TODO: Use a Properties object, like getServerMap().
     */
    public static Properties getWebappMap()
            throws TechnicalException
    {
        HttpServletRequest request = myCurrentRequest.get();
        Object obj = request.getAttribute(WEBAPP_ATTR);
        if ( obj == null ) {
            throw new TechnicalException(WEBAPP_ATTR + " is not set on the request");
        }
        if ( ! ( obj instanceof Application ) ) {
            throw new TechnicalException(WEBAPP_ATTR + " is invalid: " + obj.getClass());
        }
        Application app = (Application) obj;
        return app.getProperties();
    }

    /**
     * Get the server properties.
     */
    public static Properties getServerMap()
            throws TechnicalException
    {
        if ( ourServletConfig == null ) {
            // Servlex has not been initialized yet (that is, the servlet has not been used yet)
            return null;
        }
        ServletContext ctxt = ourServletConfig.getServletContext();
        Object obj = ctxt.getAttribute(SERVER_MAP_ATTR);
        if ( obj == null ) {
            Properties props = new Properties("web:");
            // TODO: Define the standard system properties.  See XSLT 2.0.
            try {
                ServerConfig config = ServerConfig.getInstance(ourServletConfig);
                String ver = config.getVersion();
                String rev = config.getRevision();
                String product = "Servlex version " + ver + " (revision #" + rev + ")";
                props.setPrivate("web:product", product);
                String product_html
                        = "<a href='https://servlex.net/'>Servlex</a> version "
                        + ver + " (revision #<a href='https://github.com/fgeorges/servlex/commit/"
                        + rev + "'>" + rev + "</a>)";
                props.setPrivate("web:product-html", product_html);
                String vendor = "Florent Georges, from H2O Consulting, for EXPath";
                props.setPrivate("web:vendor", vendor);
                String vendor_html
                        = "<a href='http://fgeorges.org/'>Florent Georges</a>,"
                        + " from <a href='http://h2oconsulting.be/'>H2O Consulting</a>,"
                        + " for <a href='http://expath.org/'>EXPath</a>";
                props.setPrivate("web:vendor-html", vendor_html);
            }
            catch ( TechnicalException ex ) {
                throw new TechnicalException("Unexpected exception", ex);
            }
            catch ( ParseException ex ) {
                throw new TechnicalException("Unexpected exception", ex);
            }
            catch ( PackageException ex ) {
                throw new TechnicalException("Unexpected exception", ex);
            }
            ctxt.setAttribute(SERVER_MAP_ATTR, props);
            return props;
        }
        else if ( ! ( obj instanceof Properties ) ) {
            throw new TechnicalException(SERVER_MAP_ATTR + " is invalid: " + obj.getClass());
        }
        else {
            return ( Properties ) obj;
        }
    }

    protected ServerConfig getConfig()
    {
        return myConfig;
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "The Servlex controller, dispatching to XML components";
    }

    /**
     * Initialize the server config object.
     */
    @Override
    public void init(ServletConfig config)
            throws ServletException
    {
        ourServletConfig = config;
        try {
            myConfig = ServerConfig.getInstance(config);
        }
        catch ( ParseException ex ) {
            String msg = "Error in the servlet initialization...";
            LOG.info(msg, ex);
            throw new ServletException(msg, ex);
        }
        catch ( PackageException ex ) {
            String msg = "Error in the servlet initialization...";
            LOG.info(msg, ex);
            throw new ServletException(msg, ex);
        }
    }

    /**
     * Handles HTTP requests.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException
    {
        LOG.info("Received request: " + req.getMethod() + " " + req.getRequestURL());
        // set the contextual request
        myCurrentRequest.set(req);
        // set the encoding if not explicit
        if ( req.getCharacterEncoding() == null ) {
            String charset = myConfig.getDefaultCharset();
            if ( charset != null ) {
                req.setCharacterEncoding(charset);
            }
        }
        // do it!
        try {
            if ( req.getPathInfo().equals("/") ) {
                welcome(resp);
            }
            else {
                invoke(req, resp);
            }
        }
        catch ( ServlexException ex ) {
            LOG.error("Servlet threw an exception", ex);
            ex.sendError(resp);
        }
        finally {
            myCurrentRequest.set(null);
        }
    } 

    /**
     * Display a welcome message and a link to the manager.
     */
    private void welcome(HttpServletResponse resp)
            throws IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        try {
            out.println("<html>");
            out.println("   <head>");
            out.println("      <title>Servlex</title>");
            out.println("   </head>");
            out.println("   <body>");
            out.println("      <h1>Servlex</h1>");
            out.println("      <p>Welcome!  Your Servlex server has been installed.");
            out.println("      You can go to the applications you already installed,");
            out.println("      or go to the Servlex <a href='manager/home'>manager</a>.</p>");
            out.println("   </body>");
            out.println("</html>");
        }
        finally {
            out.close();
        }
    }

    /**
     * TODO: ...
     */
    private void invoke(HttpServletRequest req, HttpServletResponse resp)
            throws IOException
                 , ServlexException
    {
        String pathinfo = req.getPathInfo();
        int slash = pathinfo.indexOf('/', 1);
        String appname;
        String path;
        // if no slash in pathinfo, then it is the app name
        if ( slash < 1 ) {
            appname = pathinfo.substring(1);
            path = "/";
        }
        else {
            appname = pathinfo.substring(1, slash);
            path = pathinfo.substring(slash);
        }
        // retrieve the application
        Application app = myConfig.getApplication(appname);
        req.setAttribute("servlex.webapp", app);
        // resolve the component
        RequestConnector request = new RequestConnector(req, path, appname);
        Invocation invoc = app.resolve(path, req.getMethod(), request);
        // log request and profiling info
        Auditor auditor = new Auditor(myConfig);
        auditor.begin(request);
        // invoke the component
        Connector result;
        try {
            result = invoc.invoke(request, myConfig, auditor);
        }
        catch ( ComponentError ex ) {
            // TODO: Shouldn't we set the result even in this case...?
            throw new ServlexException(500, "Internal error", ex);
        }
        // connect the result to the client
        result.connectToResponse(resp, myConfig);
        // end the audit
        auditor.end();
    }

    /** The name of the attributes used in this class (on the requests, sessions, and contexts). */
    private static final String WEBAPP_ATTR      = "servlex.webapp";
    private static final String REQUEST_MAP_ATTR = "servlex.request.map";
    private static final String SESSION_MAP_ATTR = "servlex.session.map";
    private static final String SERVER_MAP_ATTR  = "servlex.server.map";

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(Servlex.class);

    /** The date formatter. */
    private static final DateFormat NOW_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

    /** The HTTP request currently handled (thread-local storage). */
    private static final ThreadLocal<HttpServletRequest> myCurrentRequest
            = new ThreadLocal<HttpServletRequest>();
    /**
     * The config of this servlet.
     *
     * It can be stored in a static variable, as it must be exactly one instance
     * of this servlet (and anyway the config must always be the same).
     */
    private static ServletConfig ourServletConfig;

    /** The server configuration. */
    private ServerConfig myConfig;
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
