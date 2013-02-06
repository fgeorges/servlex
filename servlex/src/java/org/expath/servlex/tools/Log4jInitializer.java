/****************************************************************************/
/*  File:       Log4jInitializer.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-§5                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import javax.servlet.http.HttpServlet;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Servlet dedicated to initializing log4j within the servlet container.
 *
 * There is no URL binding, this is only used to be launched at the container
 * startup phase, via the init() and load-on-startup mechanism.
 *
 * @author Florent Georges
 * @date   2010-02-15
 */
public class Log4jInitializer
        extends HttpServlet
{
    @Override
    public void init()
    {
        String path = getServletContext().getRealPath("/");
        String file = getInitParameter("log4j-init-file");
        // If the log4j-init-file is not set, then no point in trying.
        // TODO: Log it properly.
        if ( file != null ) {
            // TODO: Use a proper file resolution mechanism.
            DOMConfigurator.configure(path + file);
        }
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
