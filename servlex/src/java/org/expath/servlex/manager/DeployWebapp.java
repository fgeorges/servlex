/****************************************************************************/
/*  File:       DeployWebapp.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;

/**
 * Servlet used to deploy a XAW file.
 *
 * It must receive a POST request of type "multipart/form-data", with the part
 * "xawfile" that contains the XAW file to deploy (or the XAR file to install).
 *
 * TODO: This servlet must be protected by Digest Authentication!
 *
 * TODO: When deploying, we should be able to use a URI of the form "{@code
 * .../deploy/my-webapp}" instead of just "{@code .../deploy}", in order to
 * deploy the webapp with a specific context root.  By default it is got from
 * the webapp descriptor, and that's the only way available for now.
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
    }

    /** 
     * GET is not supported.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException
                 , ServletException
    {
        View view = new View(resp, "deploy", "Deploy");
        if ( myConfig.canInstall() ) {
            view.println("<p>Deploy a webapp, either from a local XAW file, or directly from CXAN.");
            view.println("If you deploy directly from CXAN, you can use either a CXAN ID <b>or</b>");
            view.println("a full package name.  The version number is optional (if not set, the");
            view.println("latest one is picked).</p>");
            view.println("<p><b>Local file:</b></p>");
            view.println("<form action='deploy' method='post' enctype='multipart/form-data'>");
            view.println("   <input type='file' name='xawfile' size='40'>");
            view.println("   <input type='submit' value='Deploy file'>");
            view.println("</form>");
            view.println("<p><b>From CXAN:</b></p>");
            view.println("<form action='deploy-cxan' method='post' enctype='application/x-www-form-urlencoded'>");
            view.println("   <table>");
            view.println("      <tr>");
            view.println("         <td class='right'>ID:</td>");
            view.println("         <td><input name='id' size='50' type='text' /></td>");
            view.println("      </tr>");
            view.println("      <tr>");
            view.println("         <td class='right'>Name:</td>");
            view.println("         <td><input name='name' size='50' type='text' /></td>");
            view.println("      </tr>");
            view.println("      <tr>");
            view.println("         <td class='right'>Version:</td>");
            view.println("         <td><input name='version' size='50' type='text' /></td>");
            view.println("      </tr>");
            view.println("      <tr>");
            view.println("         <td class='right'>From:</td>");
            view.println("         <td>");
            view.println("            <select name='server'>");
            view.println("               <option selected='selected' value='prod'>Production - http://cxan.org/</option>");
            view.println("               <option value='sandbox'>Sandbox - http://test.cxan.org/</option>");
            view.println("            </select>");
            view.println("         </td>");
            view.println("      </tr>");
            view.println("   </table>");
            view.println("   <p><input value='Deploy' type='submit'></p>");
            view.println("</form>");
        }
        else {
            view.println("<p><em>(installation disabled, read-only storage)</em></p>");
        }
        view.close();
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
            if ( ! myConfig.canInstall() ) {
                error(501, "Install not supported, storage is read-only");
            }
            // name will be null if the package is not a webapp
            String name = doInstall(req);
            if ( name == null ) {
                view.print("The package");
            }
            else {
                view.print("<a href='../");
                view.print(name);
                view.print("/'>");
                view.print(name);
                view.print("</a>");
            }
            view.print(" has been successfully installed.");
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

    /**
     * TODO: Remove dependency on Apache 'fileupload'. First of all, the
     * algorithm here is broken (looping over parts, uploading every file and
     * using the last one silently).  Then it should check everything is ok,
     * then pass the input stream to myConfig (in order to do so, I'd have to
     * add the ability to install from an input stream on the repository).
     */
    private String doInstall(HttpServletRequest req)
            throws ServlexException
    {
        if ( ! ServletFileUpload.isMultipartContent(req) ) {
            error(400, "Request is not Multipart Content: " + req.getContentType());
        }
        File archive = null;
        try {
            FileItemFactory factory = new DiskFileItemFactory(0, null);
            ServletFileUpload upload = new ServletFileUpload(factory);
            for ( FileItem item : (List<FileItem>) upload.parseRequest(req) ) {
                // plain form field
                if ( item.isFormField() ) {
                    error(400, "Unknown parameter: " + item.getFieldName());
                }
                // file upload part
                else {
                    String id = req.getSession(true).getId();
                    archive = uploadFile(item, id);
                }
            }
        }
        catch ( FileUploadException ex ) {
            error(500, "Error uploading the file", ex);
        }
        if ( archive == null ) {
            error(500, "File not provided");
        }

        try {
            return myConfig.install(archive);
        }
        catch ( PackageException ex ) {
            error(500, "Error installing the webapp", ex);
        }
        catch ( TechnicalException ex ) {
            error(500, "Error installing the webapp", ex);
        }
        // cannot happen, because error() always throws an exception
        throw new ServlexException(500, "Cannot happen (doInstall)");
    }

    /**
     * TODO: ...
     */
    private File uploadFile(FileItem item, String id)
            throws ServlexException
    {
        String field = item.getFieldName();
        if ( ! "xawfile".equals(field) ) {
            error(400, "Unknown parameter: " + field);
        }
        String file = item.getName();
        // some clients, as Opera and IE, include the full path name instead of
        // just the basename of the file; this trick works around this problem
        file = new File(file).getName();
        if ( LOG.isInfoEnabled() ) {
            String type = item.getContentType();
            boolean memory = item.isInMemory();
            long size = item.getSize();
            LOG.info("Deployer: upload file: " + field + ", " + file + ", "
                    + type + ", " + memory + ", " + size);
        }
        if ( file == null ) {
            error(400, "The file has no name (null).");
        }
        if ( "".equals(file) ) {
            error(400, "The file has no name (empty).");
        }
        if ( item.getSize() == 0 ) {
            error(400, "The file is empty (size = 0).");
        }
        File work_dir = null;
        try {
            work_dir = File.createTempFile("servlex-", id);
        }
        catch ( IOException ex ) {
            error(500, "Error creating a temporary dir", ex);
        }
        work_dir.delete();
        work_dir.mkdirs();
        File xaw_file = new File(work_dir, file);
        if ( xaw_file.exists() ) {
            xaw_file.delete();
        }
        try {
            item.write(xaw_file);
        }
        catch ( Exception ex ) {
            error(500, "Error while writing the uploaded archive", ex);
        }
        return xaw_file;
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
    private static final Logger LOG = Logger.getLogger(DeployWebapp.class);

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
