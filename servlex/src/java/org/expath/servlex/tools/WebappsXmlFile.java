/****************************************************************************/
/*  File:       WebappsXmlFile.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.xml.transform.Transformer;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.tools.UpdatableXmlFile;

/**
 * Represent the file [repo]/.expath-pkg/packages.xml.
 *
 * @author Florent Georges
 * @date   2013-09-17
 */
public class WebappsXmlFile
        extends UpdatableXmlFile
{
    /**
     * Create a new instance.
     * 
     * @param file The actual file, for [repo]/.expath-web/webapps.xml.
     */
    public WebappsXmlFile(File file)
            throws PackageException
    {
        super(file);
    }

    /**
     * Add a webapp to webapps.xml.
     * 
     * @param root   The context root of the webapp.
     * @param pkg    The webapp package name (the name URI).
     * @param config The config parameters.
     */
    public void addWebapp(String root, String pkg, Map<String, String> config)
            throws PackageException
    {
        Transformer trans = compile(ADD_WEBAPP_XSL);
        trans.setParameter("root", root);
        trans.setParameter("pkg",  pkg);
        int i = 1;
        for ( Map.Entry<String, String> cfg : config.entrySet() ) {
            String name  = cfg.getKey();
            String value = cfg.getValue();
            trans.setParameter("config-name-" + i,   name);
            trans.setParameter("config-value-" + i,  value);
            ++i;
        }
        transform(trans);
    }

    /**
     * Remove a webapp from webapps.xml.
     * 
     * The webapp is identified by its context root.
     */
    public void removeWebapp(String root)
            throws PackageException
    {
        Transformer trans = compile(REMOVE_WEBAPP_XSL);
        trans.setParameter("root", root);
        transform(trans);
    }

    /**
     * Create an empty file.
     * 
     * By empty, means with the root element, with no package element.
     */
    @Override
    protected void createEmpty(Writer out)
            throws IOException
    {
        out.write("<webapps xmlns=\"http://expath.org/ns/webapp\">\n");
        out.write("\n");
        out.write("   <!--\n");
        out.write("       Contains elements like:\n");
        out.write("\n");
        out.write("       <webapp root=\"myapp\" enabled=\"true\">\n");
        out.write("          <package name=\"http://example.org/my/webapp\"/>\n");
        out.write("       </webapp>\n");
        out.write("   -->\n");
        out.write("\n");
        out.write("</webapps>\n");
    }

    /** The stylesheet to add a new webapp to .expath-web/webapps.xml. */
    private static final String ADD_WEBAPP_XSL    = "org/expath/servlex/rsrc/webapps-add.xsl";
    /** The stylesheet to remove a webapp from .expath-web/webapps.xml. */
    private static final String REMOVE_WEBAPP_XSL = "org/expath/servlex/rsrc/webapps-remove.xsl";
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
