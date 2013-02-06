/****************************************************************************/
/*  File:       RemoveWebapp.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-20                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.manager;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.parser.ParseException;

/**
 * Remove a webapp (or a library) from the repository.
 *
 * TODO: This servlet must be protected by Digest Authentication!
 * 
 * @author Florent Georges
 * @date   2010-02-20
 */
public class RemoveWebapp
        extends HttpServlet
{
    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "Remove a webapp.";
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
     * The last path item is the name of the package to remove.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException
                 , IOException
    {
        String name = req.getParameter("webapp");
        if ( name == null ) {
            throw new ServletException("Parameter 'webapp' mandatory");
        }
        try {
            myConfig.remove(name);
        }
        catch ( PackageException ex ) {
            throw new ServletException("Error removing the webapp", ex);
        }
        catch ( ParseException ex ) {
            throw new ServletException("Error removing the webapp", ex);
        }

        resp.setContentType("text/html;charset=UTF-8");
        View view = new View(resp.getWriter());
        view.open("remove", "Remove webapp");
        view.println("<p>Webapp removed: " + name + ".</p>");
        view.close();
    } 

    /** 
     * POST is not supported.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException
                 , IOException
    {
        throw new ServletException("POST is not supported.");
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(Manager.class);

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
