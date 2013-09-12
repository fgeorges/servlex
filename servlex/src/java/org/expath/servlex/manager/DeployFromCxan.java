/****************************************************************************/
/*  File:       DeployFromCxan.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;

/**
 * Servlet used to install a webapp/package from CXAN.
 *
 * @author Florent Georges
 * @date   2013-02-03
 */
public class DeployFromCxan
        extends HttpServlet
{
    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "Deploy a XAW (or XAR) file from CXAN.";
    }

    /**
     * Initialize the server config object.
     */
    @Override
    public void init(ServletConfig config)
            throws ServletException
    {
        try {
            myConfig = ServerConfig.getInstance(config);
        }
        catch ( TechnicalException ex ) {
            String msg = "Error initializing the server configuration...";
            LOG.info(msg, ex);
            throw new ServletException(msg, ex);
        }
        myRepo = myConfig.getRepository();
    }

    /** 
     * GET is not supported.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException
    {
        resp.addHeader("Allow", "POST");
        resp.sendError(405, "Method Not Allowed");
    }

    /** 
     * Deploy a XAW file.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException
                 , ServletException
    {
        View view = new View(resp, "deploy", "Deploy");
        view.print("<p>");
        try {
            if ( ! myRepo.canInstall() ) {
                error(501, "Install not supported, storage is read-only");
            }
            String cxanid = getNonEmptyParam(req, "id");
            String name   = getNonEmptyParam(req, "name");
            // name will be null if the package is not a webapp
            String abbrev = doInstall(req, cxanid, name);
            if ( NOT_FOUND == abbrev ) {
                view.print("The package with the");
                if ( cxanid != null ) {
                    view.print(" CXAN ID '" + cxanid);
                }
                else {
                    view.print(" name '" + name);
                }
                view.print("' does not exist.");
            }
            else {
                if ( abbrev == null ) {
                    view.print("The package");
                }
                else {
                    view.print("<a href='../");
                    view.print(abbrev);
                    view.print("/'>");
                    view.print(abbrev);
                    view.print("</a>");
                }
                view.print(" has been successfully installed.");
            }
        }
        catch ( ServlexException ex ) {
            view.print("<b>Error</b>: " + ex.getMessage());
            ex.setStatus(resp);
        }
        finally {
            view.print("</p>\n");
            view.close();
        }
    }

    private String doInstall(HttpServletRequest req, String id, String name)
            throws ServlexException
    {
        String version = getNonEmptyParam(req, "version");
        String server  = getNonEmptyParam(req, "server");

        if ( server == null ) {
            error(400, "The CXAN server to use has not been passed.");
        }
        if ( ! "prod".equals(server) && ! "sandbox".equals(server) ) {
            error(400, "The CXAN server to use must be either 'prod' or 'sandbox', but is '" + server + "'.");
        }
        if ( id == null && name == null ) {
            error(400, "Neither CXAN ID or package name provided, at least one is required.");
        }
        else if ( id != null && name != null ) {
            error(400, "Both CXAN ID and package name provided: resp. '" + id + "' and '" + name + "'.");
        }

        String uri = "prod".equals(server) ? "http://cxan.org/" : "http://test.cxan.org/";
        if ( name == null ) {
            uri += "file?id=" + id;
        }
        else {
            uri += "file?name=" + name;
        }
        if ( version != null ) {
            uri += "&version=" + version;
        }

        try {
            // TODO: Set the context root (instead of null) and whether to
            // override an existing package (instead of false), form a form
            // filled by the user...
            return myRepo.install(new URI(uri), null, false);
        }
        catch ( URISyntaxException ex ) {
            error(500, "Error constructing the package URI on CXAN: " + uri, ex);
        }
        catch ( Repository.AlreadyInstalledException ex ) {
            error(400, "Package is already installed: " + ex.getName() + " / " + ex.getVersion(), ex);
        }
        catch ( PackageException ex ) {
            if ( ex.getCause() != null && ex.getCause() instanceof FileNotFoundException ) {
                return NOT_FOUND;
            }
            else {
                error(500, "Error installing the webapp", ex);
            }
        }
        catch ( TechnicalException ex ) {
            error(500, "Error installing the webapp", ex);
        }
        // cannot happen, because error() always throws an exception
        throw new ServlexException(500, "Cannot happen (doInstall)");
    }

    private String getNonEmptyParam(HttpServletRequest req, String name)
    {
        String value = req.getParameter(name);
        if ( value == null ) {
            return null;
        }
        else if ( Pattern.matches("^\\s*$", value) ) {
            return null;
        }
        else {
            return value;
        }
    }

    // TODO: Error management!
    private void error(int code, String msg)
            throws ServlexException
    {
        LOG.error(code + ": " + msg);
        throw new ServlexException(code, msg);
    }

    private void error(int code, String msg, Throwable ex)
            throws ServlexException
    {
        LOG.error(code + ": " + msg, ex);
        throw new ServlexException(code, msg, ex);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(DeployFromCxan.class);
    /** Special marker... */
    private static final String NOT_FOUND = "*not*found*on*cxan*";

    /** The server configuration. */
    private ServerConfig myConfig;
    /** The web repository. */
    private WebRepository myRepo;
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
