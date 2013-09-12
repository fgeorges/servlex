/****************************************************************************/
/*  File:       List.java                                                   */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.manager;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;

/**
 * List the installed webapps.
 *
 * @author Florent Georges
 * @date   2013-02-03
 */
public class List
        extends HttpServlet
{
    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "The Servlex manager page listing the installed webapps";
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
            throw new ServletException("Error initializing the server configuration...", ex);
        }
    }

    /** 
     * Display the homepage of the manager, listing all installed applications.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        View view = new View(resp, "list", "Installed webapps");
        // display the names in alphabetical order
        SortedSet<String> names = new TreeSet<String>(myConfig.getApplicationNames());
        if ( names.isEmpty() ) {
            view.println("<p>No webapp has been installed yet.</p>");
        }
        else {
            view.println("<p>Click on the webapp name to access it (its homepage).");
            view.println(" Click on the (x) to delete it.</p>");
            view.println("<ul>");
            for ( String app : names ) {
                view.startln();
                view.print("   <li>");
                WebRepository repo = myConfig.getRepository();
                if ( repo.canInstall() ) {
                    view.print("(<a href='remove?webapp=");
                    view.print(app);
                    view.print("'>x</a>) ");
                }
                view.print("<a href='../");
                view.print(app);
                view.print("/'>");
                view.print(app);
                view.print("</a></li>\n");
            }
            view.println("</ul>");
        }
        view.close();
    } 

    /** 
     * POST is not supported.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException
    {
        resp.addHeader("Allow", "GET");
        resp.sendError(405, "Method Not Allowed");
    }

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
