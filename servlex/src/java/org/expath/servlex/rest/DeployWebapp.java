/****************************************************************************/
/*  File:       DeployWebapp.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;

/**
 * Servlet used to deploy a XAW file.
 *
 * It must receive a POST request of type "application/octet-stream", the content
 * of which is the XAW file to deploy (or the XAR file to install).
 *
 * TODO: This servlet must be protected by Digest Authentication!
 *
 * TODO: When deploying, we should be able to use a URI of the form "{@code
 * .../deploy/my-webapp}" instead of just "{@code .../deploy}", in order to
 * deploy the webapp with a specific context root.  By default it is got from
 * the webapp descriptor, and that's the only way available for now.
 * 
 * TODO: Define a specific content type for XAR and XAW files? (instead of
 * application/octet-stream, something like application/x-expath-xar+zip, and
 * maybe application/x-expath-xaw+zip as well)
 *
 * @author Florent Georges
 * @date   2010-02-19
 */
public class DeployWebapp
        extends HttpServlet
{
    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "Deploy a XAW (or XAR) file.";
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
     * Accepts only POST requests, to install a XAR or XAW file.
     * 
     * @param req The HTTP request object.
     * 
     * @param resp The HTTP response object.
     * 
     * @throws IOException In case of any I/O error.
     * 
     * @throws ServletException In case of any other error.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException
                 , IOException
    {
        String method = req.getMethod().toLowerCase();
        if ( method.equals("post") ) {
            doPost(req, resp);
        }
        else {
            resp.addHeader("Allow", "POST");
            resp.sendError(405, "Method Not Allowed");
        }
    }

    /**
     * Deploy a XAR or a XAW file.
     * 
     * @param req The HTTP request object.
     * 
     * @param resp The HTTP response object.
     * 
     * @throws IOException In case of any I/O error.
     * 
     * @throws ServletException In case of any other error.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException
                 , ServletException
    {
        resp.setContentType("application/xml");
        resp.setCharacterEncoding("UTF-8");
        ServletOutputStream out = resp.getOutputStream();
        try {
            if ( ! myRepo.canInstall() ) {
                error(501, "Not Implemented", "Install not supported, storage is read-only.");
            }
            // name will be null if the package is not a webapp
            String name = doInstall(req);
            out.println("<success>");
            if ( name == null ) {
                out.print("   <msg>The package");
            }
            else {
                out.print("   <msg>The webapp at ");
                out.print(name);
            }
            out.println(" has been successfully installed.</msg>");
            out.println("</success>");
        }
        catch ( RestError ex ) {
            out.println("<error>");
            out.println("   <msg>" + ex.getUserMessage() + "</msg>");
            out.println("</error>");
            ex.sendError(resp);
        }
        catch ( ServlexException ex ) {
            out.println("<error>");
            out.println("   <msg>Unexpected exception: " + ex.getMessage() + "</msg>");
            out.println("</error>");
            ex.sendError(resp);
        }
        catch ( RuntimeException ex ) {
            out.println("<error>");
            out.println("   <msg>Unexpected runtime error: " + ex.getMessage() + "</msg>");
            out.println("   <msg>Please report this to the mailing list, see Servlex logs");
            out.println("      for additional information about the error.</msg>");
            out.println("</error>");
            resp.sendError(500, "Internal Server Error");
        }
    }

    /**
     * TODO: This function should check everything is ok, then pass the input
     * stream to myConfig (in order to do so, I'd have to add the ability to
     * install from an input stream on the repository).
     */
    private String doInstall(HttpServletRequest req)
            throws ServlexException
    {
        // TODO: Test the content type, once defined (see the doc of this class)
//        if ( ! req.getContentType().equals("application/x-expath-xar+zip") ) {
//            error(415, "Unsupported Media Type",
//                    "Unsupported media type: " + req.getContentType()
//                    + ", require application/x-expath-xar+zip.");
//        }
        String root = getContextRoot(req);
        File archive = saveFile(req);
        try {
            // TODO: Set whether to override an existing package (instead of
            // false), from a request URI query parameter.
            return myRepo.install(archive, root, false);
        }
        catch ( Repository.AlreadyInstalledException ex ) {
            error(409, "Conflict", "Package is already installed: " + ex.getName() + " / " + ex.getVersion(), ex);
        }
        catch ( PackageException | TechnicalException ex ) {
            error(500, "Internal Server Error", "Error installing the webapp: " + ex.getMessage(), ex);
        }
        // cannot happen, because error() always throws an exception
        return null;
    }

    /**
     * Return the context root to install the webapp, at the end of the URL.
     */
    private String getContextRoot(HttpServletRequest req)
            throws RestError
    {
        String info = req.getPathInfo();
        if ( info == null ) {
            error(500, "Internal Server Error", "Path info is null.");
        }
        if ( ! info.startsWith("/") ) {
            error(500, "Internal Server Error", "Path info does not start with /.");
        }
        String root = info.substring(1);
        if ( ! CTXT_ROOT_PATTERN.matcher(root).matches() ) {
            error(400, "Bad Request", "The webapp context root is not valid: "
                    + root + "(using the regex: " + CTXT_ROOT_RE + ")");
        }
        return root;
    }

    /**
     * Return a file object to use to store the uploaded file.
     */
    private File saveFile(HttpServletRequest req)
            throws ServlexException
    {
        File file = getFile(req);
        OutputStream out = openFile(file);
        try {
            InputStream in = req.getInputStream();
            IOUtils.copy(in, out);
            out.close();
            return file;
        }
        catch ( IOException ex ) {
            error(500, "Internal Server Error", "Error uploading the file", ex);
            // cannot happen, because error() always throws an exception
            return null;
        }
    }

    /**
     * Return a file object to use to store the uploaded file.
     */
    private File getFile(HttpServletRequest req)
            throws ServlexException
    {
        String id = req.getSession(true).getId();
        File file = null;
        try {
            file = File.createTempFile("servlex-", id);
        }
        catch ( IOException ex ) {
            error(500, "Internal Server Error", "Error creating a temporary dir, with ID: " + id, ex);
        }
        return file;
    }

    /**
     * Open a file as an output stream.
     */
    private OutputStream openFile(File file)
            throws ServlexException
    {
        try {
            return new FileOutputStream(file);
        }
        catch ( FileNotFoundException ex ) {
            error(500, "Internal Server Error", "File not found: " + file.getName());
            // cannot happen, because error() always throws an exception
            return null;
        }
    }

    private void error(int code, String status, String msg)
            throws RestError
    {
        LOG.error(code + ": " + status + ": " + msg);
        throw new RestError(code, status, msg);
    }

    private void error(int code, String status, String msg, Throwable ex)
            throws RestError
    {
        LOG.error(code + ": " + status + ": " + msg, ex);
        throw new RestError(code, status, msg, ex);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(DeployWebapp.class);
    /**
     * The regex for webapp context roots.
     * 
     * TODO: The current draft of the spec says it is an NCName.
     */
    private static final String CTXT_ROOT_RE = "[-a-zA-Z0-9]+";
    /** The patter for the regex for webapp context roots. */
    private static final Pattern CTXT_ROOT_PATTERN = Pattern.compile(CTXT_ROOT_RE);

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
