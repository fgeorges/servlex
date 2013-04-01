/****************************************************************************/
/*  File:       View.java                                                   */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.manager;

import java.io.PrintWriter;
import javax.servlet.ServletException;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.tools.Properties;

/**
 * Apply a unified view to the console pages.
 *
 * @author Florent Georges
 * @date   2013-02-03
 */
public class View
{
    public View(PrintWriter out)
    {
        myOut = out;
    }

    public void println(String line)
    {
        myOut.println("                  " + line);
    }

    public void print(String content)
    {
        myOut.println(content);
    }

    public void startln()
    {
        myOut.println("                  ");
    }

    public void open(String id, String title)
    {
        myOut.println("<html>");
        myOut.println("   <head>");
        myOut.println("      <title>Manager</title>");
        myOut.println("      <link rel=\"stylesheet\" type=\"text/css\" href=\"style/default.css\"/>");
        myOut.println("      <link rel=\"shortcut icon\" type=\"image/png\" href=\"images/expath-icon.png\"/>");
        myOut.println("   </head>");
        myOut.println("   <body>");
        myOut.println("      <div id='upbg'> </div>");
        myOut.println("      <div id='outer'>");
        myOut.println("         <div id='header'>");
        myOut.println("            <div id='headercontent'>");
        myOut.println("               <h1>Servlex manager</h1>");
        myOut.println("            </div>");
        myOut.println("         </div>");
        myOut.println("         <div id='menu'>");
        myOut.println("            <ul>");
        printMenuEntry(id, "home",   "Servlex manager home",   "Home");
        printMenuEntry(id, "list",   "Installed webapps list", "Webapps");
        printMenuEntry(id, "deploy", "Deploy a webapp",        "Deploy");
        printMenuEntry(id, "reset",  "Reset the webapp cache", "Reset");
        myOut.println("            </ul>");
        myOut.println("         </div>");
        myOut.println("         <div id='menubottom'> </div>");
        myOut.println("         <div id='content'>");
        myOut.println("            <div class='normalcontent'>");
        myOut.print("               <h3><strong>");
        myOut.print(title);
        myOut.println("</strong></h3>");
        myOut.println("               <div class='contentarea'>");
    }

    private void printMenuEntry(String id, String href, String title, String label)
    {
        myOut.print("               <li><a href='");
        myOut.print(href);
        myOut.print("' title='");
        myOut.print(title);
        myOut.print("'");
        if ( href.equals(id) ) {
            myOut.print(" class='active'");
        }
        myOut.print(">");
        myOut.print(label);
        myOut.println("</a></li>");
    }

    public void close()
            throws ServletException
    {
        String vendor = getVendor();
        myOut.println("               </div>");
        myOut.println("            </div>");
        myOut.println("         </div>");
        myOut.println("         <div id='footer'>");
        if ( vendor == null ) {
            myOut.println("            <div class='right'>[ version not loaded yet ]</div>");
        }
        else {
            myOut.println("            <div class='right'>" + vendor + "</div>");
        }
        myOut.println("         </div>");
        myOut.println("      </div>");
        myOut.println("   </body>");
        myOut.println("</html>");
        myOut.close();
    }

    private String getVendor()
            throws ServletException
    {
        try {
            Properties props = Servlex.getServerMap();
            if ( props == null ) {
                // Servlex has not been initialized yet
                return null;
            }
            String product = props.getPrivate("web:product-html");
            String vendor  = props.getPrivate("web:vendor-html");
            return product + ", by " + vendor;
        }
        catch ( TechnicalException ex ) {
            throw new ServletException("Error getting the system property web:vendor", ex);
        }
    }

    private PrintWriter myOut;
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
