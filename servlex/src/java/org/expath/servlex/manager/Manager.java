/****************************************************************************/
/*  File:       Manager.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-20                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.manager;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The main page of the manager.
 *
 * TODO: This servlet must be protected by Digest Authentication!
 * 
 * @author Florent Georges
 * @date   2010-02-20
 */
public class Manager
        extends HttpServlet
{
    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "The Servlex manager";
    }

    /** 
     * Display the homepage of the manager, listing all installed applications.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        View view = new View(resp, "home", "Manager");
        view.println("<p>Choose one of the pages in the menu above.</p>");
        view.println("<p>You can find documentation on the");
        view.println("   <a href='http://servlex.net/'>Servlex website</a>.</p>");
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
