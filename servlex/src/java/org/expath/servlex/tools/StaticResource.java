/****************************************************************************/
/*  File:       StaticResource.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 * Serve static resources.
 *
 * @author Florent Georges
 * @date   2013-02-03
 */
public class StaticResource
        extends HttpServlet
{
    /** 
     * Serve the resource straight from the classpath.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException
                 , IOException
    {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        ClassLoader loader = StaticResource.class.getClassLoader();
        InputStream in = null;
        if ( path.startsWith("/manager/images/") ) {
            in = loader.getResourceAsStream("/org/expath/servlex" + path);
        }
        else if ( path.startsWith("/manager/style/") ) {
            in = loader.getResourceAsStream("/org/expath/servlex" + path);
        }
        if ( in == null ) {
            resp.sendError(404, "Resource not found - " + path);
            return;
        }
        if ( path.endsWith(".gif") ) {
            resp.setContentType("image/gif");
        }
        else if ( path.endsWith(".png") ) {
            resp.setContentType("image/png");
        }
        else if ( path.endsWith(".css") ) {
            resp.setContentType("text/css");
        }
        else {
            resp.sendError(500, "Unknown resource content type - " + path);
            return;
        }
        IOUtils.copy(in, resp.getOutputStream());
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
