/****************************************************************************/
/*  File:       WebRepository.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.FileSystemStorage;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.Packages;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.repo.UserInteractionStrategy;
import org.expath.servlex.model.Application;
import org.expath.servlex.parser.EXPathWebParser;
import org.expath.servlex.parser.WebappsParser;
import org.expath.servlex.tools.ProcessorsMap;

/**
 * The package repository, with support for EXPath Webapp.
 *
 * @author Florent Georges
 * @date   2013-09-11
 */
public class WebRepository
{
    /**
     * Create a new repository with EXPath Webapp support.
     */
    public WebRepository(Repository underlying, ProcessorsMap procs)
            throws TechnicalException
    {
        myUnderlying = underlying;
        myProcs = procs;
        myApps = initApplications();
    }

    /**
     * Return the underlying package repository.
     */
    public Repository getUnderlying()
    {
        return myUnderlying;
    }

    /**
     * Return the application bound to the given context root.
     */
    public Application getApplication(String root)
    {
        return myApps.get(root);
    }

    /**
     * Return all the context roots enabled in this repository.
     */
    public Set<String> getContextRoots()
    {
        return myApps.keySet();
    }

    /**
     * Some repositories do not support installing new webapps.
     * 
     * For instance if their storage is read-only.  This method returns true if
     * it is possible to install new webapps in this repository.
     */
    public boolean canInstall()
    {
        return ! myUnderlying.getStorage().isReadOnly();
    }

    /**
     * Install a webapp (or a library) in the repository.
     *
     * Return the name of the newly installed webapp, or null if the package
     * is not a webapp.
     * 
     * @param ctxt_root The context where to make the webapp available.
     */
    public synchronized String install(File archive, String ctxt_root, boolean force)
            throws TechnicalException
                 , PackageException
    {
        installPreconditions(ctxt_root);
        Package pkg = myUnderlying.installPackage(archive, force, new LoggingUserInteraction());
        return doInstall(pkg, ctxt_root);
    }

    /**
     * Install a webapp (or a library) in the repository.
     *
     * Return the name of the newly installed webapp, or null if the package
     * is not a webapp.
     * 
     * @param ctxt_root The context where to make the webapp available.
     */
    public synchronized String install(URI uri, String ctxt_root, boolean force)
            throws TechnicalException
                 , PackageException
    {
        installPreconditions(ctxt_root);
        Package pkg = myUnderlying.installPackage(uri, force, new LoggingUserInteraction());
        return doInstall(pkg, ctxt_root);
    }

    /**
     * Remove a webapp (or a library) in the repository.
     */
    public synchronized void remove(String appname)
            throws PackageException
                 , TechnicalException
    {
        Application app = myApps.get(appname);
        if ( app == null ) {
            throw new PackageException("The application is not installed: " + appname);
        }
        Package pkg = app.getPackage();
        myUnderlying.removePackage(pkg.getName(), true, new LoggingUserInteraction());
        myApps.remove(appname);
        // Update [repo]/.expath-web/webapps.xml.
        removeFromWebappsXml(appname);
    }

    /**
     * Return a map from context roots to applications.
     * 
     * The applications are all the enabled web applications in this repository.
     */
    private Map<String, Application> initApplications()
            throws TechnicalException
    {
        // the webapps.xml parser
        WebappsParser webapps_parser = new WebappsParser(myUnderlying);
        // the application URI names mapping to context roots
        Map<URI, String> roots = webapps_parser.parse();
        // the .expath-web.xml's parser
        EXPathWebParser expath_parser = new EXPathWebParser(myProcs);
        // the application map
        Map<String, Application> applications = new HashMap<String, Application>();
        // parse and save the result in the map
        for ( URI app_name : roots.keySet() ) {
            String root = roots.get(app_name);
            Packages packages = myUnderlying.getPackages(app_name.toString());
            if ( packages == null ) {
                // TODO: Maybe log it as an error instead, but not fatal (webapps.xml
                // is corrupted, but that does not prevent to continue with other
                // applications).
                throw new TechnicalException("Package " + app_name + " not installed (but in .expath-web/webapps.xml).");
            }
            org.expath.pkg.repo.Package pkg = packages.latest();
            Application app = expath_parser.loadPackage(pkg);
            if ( app == null ) {
                throw new TechnicalException("Not an application: " + app_name + " / " + pkg);
            }
            LOG.info("Add the application to the store: " + root + " / " + app.getName());
            applications.put(root, app);
        }
        return applications;
    }

    /**
     * Check preconditions before installing.
     * 
     * Checks if this repository supports installing new webapps, and if the
     * context root provided is syntactically valid (if not null).  If anything
     * is wrong, an exception is raised.
     */
    private void installPreconditions(String ctxt_root)
            throws TechnicalException
    {
        if ( ! canInstall() ) {
            throw new TechnicalException("Storage read-only, cannot install in this web repository.");
        }
        if ( ctxt_root != null && ! WebappsParser.isContextRootValid(ctxt_root) ) {
            throw new TechnicalException("Syntactically invalid context root: " + ctxt_root);
        }
    }

    /**
     * Implements the installation methods.
     */
    private String doInstall(Package pkg, String ctxt_root)
            throws TechnicalException
    {
        EXPathWebParser parser = new EXPathWebParser(myProcs);
        Application app = parser.loadPackage(pkg);
        if ( app == null ) {
            // not a webapp
            return null;
        }
        else {
            // by default use the webapp's own abbrev
            String root = ctxt_root == null ? app.getName() : ctxt_root;
            // package is a webapp
            myApps.put(root, app);
            // update [repo]/.expath-web/webapps.xml
            addToWebappsXml(root, pkg.getName());
            return root;
        }
    }

    /**
     * Use a stylesheet to add a webapp to .expath-web/webapps.xml.
     */
    private void addToWebappsXml(String root, String pkg)
            throws TechnicalException
    {
        // the stylesheet and the parameters
        Transformer trans = compileStylesheet(WEBAPPS_ADD_XSL);
        trans.setParameter("root", root);
        trans.setParameter("pkg", pkg);
        // transform it
        transformWebappsXml(trans);
    }

    /**
     * Use a stylesheet to remove a webapp from .expath-web/webapps.xml.
     */
    private void removeFromWebappsXml(String root)
            throws TechnicalException
    {
        // the stylesheet and the parameters
        Transformer trans = compileStylesheet(WEBAPPS_REMOVE_XSL);
        trans.setParameter("root", root);
        // transform it
        transformWebappsXml(trans);
    }

    private File getWebappsXml()
            throws TechnicalException
    {
        Storage storage = myUnderlying.getStorage();
        if ( ! (storage instanceof FileSystemStorage) ) {
            throw new TechnicalException("Installing and removing webapps only supported on File System Storage: " + storage.getClass());
        }
        FileSystemStorage fs_storage = (FileSystemStorage) storage;
        File dir = fs_storage.getRootDirectory();
        return new File(dir, ".expath-web/webapps.xml");
    }

    /**
     * Transform a file with a stylesheet and replace it with the result of the transform.
     * 
     * Because we want to write the result back to the same file as the input,
     * we want to be sure we won't delete it first to read it.  So we buffer
     * the output.  In order to avoid creating a temporary file, we buffer the
     * result in memory, by serializing it to a string.  This is ok for such a
     * small file.
     */
    private void transformWebappsXml(Transformer trans)
            throws TechnicalException
    {
        File file = getWebappsXml();
        try {
            Source src = new StreamSource(file);
            StringWriter res_out = new StringWriter();
            StreamResult res = new StreamResult(res_out);
            trans.transform(src, res);
            OutputStream out = new FileOutputStream(file);
            out.write(res_out.getBuffer().toString().getBytes());
            out.close();
        }
        catch ( TransformerException ex ) {
            throw new TechnicalException("Error transforming packages.xml", ex);
        }
        catch ( FileNotFoundException ex ) {
            throw new TechnicalException("File not found (wtf? - I just transformed it): " + file, ex);
        }
        catch ( IOException ex ) {
            throw new TechnicalException("Error writing the file: " + file, ex);
        }
    }

    private Transformer compileStylesheet(String rsrc_name)
            throws TechnicalException
    {
        try {
            // cache the compiled stylesheet?
            ClassLoader loader = FileSystemStorage.class.getClassLoader();
            InputStream style_in = loader.getResourceAsStream(rsrc_name);
            if ( style_in == null ) {
                throw new TechnicalException("Resource not found: " + rsrc_name);
            }
            Source style_src = new StreamSource(style_in);
            style_src.setSystemId(rsrc_name);
            Templates style = TransformerFactory.newInstance().newTemplates(style_src);
            return style.newTransformer();
        }
        catch ( TransformerConfigurationException ex ) {
            throw new TechnicalException("Impossible to compile the stylesheet: " + rsrc_name, ex);
        }
        catch ( TransformerException ex ) {
            throw new TechnicalException("Error transforming packages.xml", ex);
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ServerConfig.class);
    /** The stylesheet to add a new webapp to .expath-web/webapps.xml. */
    private static final String WEBAPPS_ADD_XSL = "org/expath/servlex/rsrc/webapps-add.xsl";
    /** The stylesheet to remove a webapp from .expath-web/webapps.xml. */
    private static final String WEBAPPS_REMOVE_XSL = "org/expath/servlex/rsrc/webapps-remove.xsl";

    /** The underlying package repository. */
    private Repository myUnderlying;
    /** The map of Processors objects. */
    private ProcessorsMap myProcs;
    /** The application map. */
    private Map<String, Application> myApps;

    /**
     * User interaction implementation always returning default value, and logging messages.
     */
    private class LoggingUserInteraction
            implements UserInteractionStrategy
    {
        @Override
        public void messageInfo(String msg)
                throws PackageException
        {
            LOG.info(msg);
        }

        @Override
        public void messageError(String msg)
                throws PackageException
        {
            LOG.error(msg);
        }

        @Override
        public void logInfo(String msg)
                throws PackageException
        {
            LOG.info(msg);
        }

        @Override
        public boolean ask(String prompt, boolean dflt)
                throws PackageException
        {
            return dflt;
        }

        @Override
        public String ask(String prompt, String dflt)
                throws PackageException
        {
            return dflt;
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
