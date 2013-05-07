/****************************************************************************/
/*  File:       Reset.java                                                  */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-04-22                                                  */
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
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;

/**
 * TODO: ...
 *
 * @author Florent Georges
 * @date   2010-04-22
 */
public class Reset
        extends HttpServlet
{
    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "Reset the cached data.";
    }

    /**
     * Initialize the servlet config object.
     */
    @Override
    public void init(ServletConfig config)
            throws ServletException
    {
        ourServletConfig = config;
    }

    /**
     * TODO: ...
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException
                 , IOException
    {
        View view = new View(resp, "reset", "Reset cache");
        view.println("<form action='reset' method='post'>");
        view.println("   Clear the application cache:");
        view.println("   <input type='submit' value='Reset'>");
        view.println("</form>");
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
        View view = new View(resp, "reset", "Reset cache");
        try {
            ServerConfig.reload(ourServletConfig);
            view.println("<p>The application cache has been reloaded.</p>");
        }
        catch ( TechnicalException ex ) {
            view.print("<b>Error</b> reloading the config: " + ex.getMessage());
            resp.setStatus(500);
        }
        finally {
            view.close();
        }
    }

    /** The config of this servlet. */
    private static ServletConfig ourServletConfig;
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
