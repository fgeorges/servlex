/****************************************************************************/
/*  File:       ServerConfig.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import org.expath.servlex.model.Application;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.ClasspathStorage;
import org.expath.pkg.repo.FileSystemStorage;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.repo.UserInteractionStrategy;
import org.expath.servlex.parser.EXPathWebParser;
import org.expath.servlex.parser.ParseException;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.saxon.SaxonCalabash;


/**
 * Singleton class with the config of the server.
 *
 * TODO: Probably should NOT be a singleton.  What if a (Java) webapp wants to
 * have different instances of Servlex, using different server configuration,
 * with several repositories?
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public class ServerConfig
{
    /** The system property name for the repo directory. */
    private static final String REPO_DIR_PROPERTY        = "org.expath.servlex.repo.dir";
    /** The system property name for the repo classpath prefix. */
    private static final String REPO_CP_PROPERTY         = "org.expath.servlex.repo.classpath";
    /** The system property name for the log directory. */
    private static final String PROFILE_DIR_PROPERTY     = "org.expath.servlex.profile.dir";
    /** The system property name for whether logging HTTP entity content. */
    private static final String TRACE_CONTENT_PROPERTY   = "org.expath.servlex.trace.content";
    /** The system property name for whether logging HTTP entity content. */
    private static final String DEFAULT_CHARSET_PROPERTY = "org.expath.servlex.default.charset";

    /**
     * Initialize the webapp list from the repository got from system properties.
     */
    protected ServerConfig()
            throws TechnicalException
                 , PackageException
    {
        this(System.getProperty(REPO_DIR_PROPERTY), System.getProperty(REPO_CP_PROPERTY));
    }

    /**
     * Initialize the webapp list from the repository got from either parameter.
     */
    protected ServerConfig(String repo_dir, String repo_classpath)
            throws TechnicalException
                 , PackageException
    {
        this(getStorage(repo_dir, repo_classpath));
    }

    /**
     * Initialize the webapp list from the repository got from the parameter.
     */
    protected ServerConfig(Storage repo_storage)
            throws TechnicalException
                 , PackageException
    {
        LOG.info("ServerConfig with storage: " + repo_storage);
        myStorage = repo_storage;
        // the repository object
        try {
            myRepo = new Repository(myStorage);
        }
        catch ( PackageException ex ) {
            throw new PackageException("Error inityializing the repository", ex);
        }
        // the processors
        // TODO: Must use a kind of registry, but must not instantiate explicitly Saxon
        // and Calabash from here, it should be injected somehow...
        myProcessors = new SaxonCalabash(myRepo, this);
        // Calabash profiling
        String prof_prop = System.getProperty(ServerConfig.PROFILE_DIR_PROPERTY);
        if ( prof_prop != null ) {
            myProfileDir = new File(prof_prop);
            if ( ! myProfileDir.exists() ) {
                LOG.error("Calabash profile dir does not exist, disabling profiling (" + myProfileDir + ")");
                myProfileDir = null;
            }
        }
        // set version and revision numbers
        setVersion();
        // the trace content property
        String trace_prop = System.getProperty(ServerConfig.TRACE_CONTENT_PROPERTY);
        if ( trace_prop == null ) {
            myTraceContent = false;
        }
        else if ( "true".equals(trace_prop) ) {
            myTraceContent = true;
        }
        else if ( "false".equals(trace_prop) ) {
            myTraceContent = false;
        }
        else {
            // TODO: Don't use a package exception, use another exception type.
            throw new PackageException("Invalid value for the property " + TRACE_CONTENT_PROPERTY + ": " + trace_prop);
        }
        // the default charset property
        myDefaultCharset = System.getProperty(ServerConfig.DEFAULT_CHARSET_PROPERTY);
        // the parser
        EXPathWebParser parser = new EXPathWebParser(myProcessors);
        // the application map
        myApps = new HashMap<String, Application>();
        for ( Application app : parser.parseDescriptors(myRepo.listPackages()) ) {
            myApps.put(app.getName(), app);
            LOG.info("Add the application to the store: " + app.getName());
        }
    }

    /**
     * Return true if needs to include request and response content in the logs.
     */
    public boolean isTraceContentEnabled()
    {
        return myTraceContent;
    }

    /**
     * Return the default charset to use in case none is set on the request. Can be null.
     */
    public String getDefaultCharset()
    {
        return myDefaultCharset;
    }

    /**
     * Return the directory to save profiling data, when enabled.  Null if disabled.
     */
    public File getProfileDir()
    {
        return myProfileDir;
    }

    /**
     * Return a file in the profile dir, with a name based on current time.
     */
    public File getProfileFile(String prefix)
            throws TechnicalException
    {
        if ( myProfileDir != null ) {
            String id = Servlex.getRequestMap().getPrivate("web:request-id");
            File file = new File(myProfileDir, prefix + "-" + id + ".xml");
            if ( file.exists() ) {
                // TODO: What if the file already exists?
                LOG.error("Profiling file already exists! (" + file + ")");
            }
            return file;
        }
        else {
            return null;
        }
    }

    /**
     * Set the version and revision number by reading the properties file.
     */
    private void setVersion()
            throws ParseException
    {
        Properties props = new Properties();
        InputStream rsrc = ServerConfig.class.getResourceAsStream(VERSION_RSRC);
        if ( rsrc == null ) {
            throw new ParseException("Version properties file does not exist: " + VERSION_RSRC);
        }
        try {
            props.load(rsrc);
            rsrc.close();
        }
        catch ( IOException ex ) {
            throw new ParseException("Error reading the version properties: " + VERSION_RSRC, ex);
        }
        myVersion  = props.getProperty(VERSION_PROP);
        myRevision = props.getProperty(REVISION_PROP);
    }

    private static Storage getStorage(String repo_dir, String repo_classpath)
            throws PackageException
    {
        if ( repo_dir == null && repo_classpath == null ) {
            // TODO: DEBUG: Must be set within web.xml...
            // repo_classpath = "appengine.repo";
            throw new PackageException("Neither " + REPO_DIR_PROPERTY + " nor " + REPO_CP_PROPERTY + " is set");
        }
        if ( repo_dir != null && repo_classpath != null ) {
            throw new PackageException("Both " + REPO_DIR_PROPERTY + " and " + REPO_CP_PROPERTY + " are set");
        }
        // the storage object
        Storage store;
        if ( repo_dir != null ) {
            File f = new File(repo_dir);
            if ( ! f.exists() ) {
                String msg = "The EXPath repository does not exist (" + REPO_DIR_PROPERTY + "=" + repo_dir + "): " + f;
                throw new PackageException(msg);
            }
            store = new FileSystemStorage(f);
        }
        else {
            store = new ClasspathStorage(repo_classpath);
        }
        return store;
    }

    /**
     * Return the Servlex version number.
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Return the Servlex revision number.
     */
    public String getRevision()
    {
        return myRevision;
    }

    public boolean canInstall()
    {
        return ! myStorage.isReadOnly();
    }

    /**
     * Reload the configuration.
     *
     * TODO: Maybe we should instead really reparse the map in the same instance,
     * so we do not invalidate all the reference to the existing instance, so
     * this is really a singleton (and other classes can keep a reference to
     * the singleton instance if they want).
     */
    public static synchronized ServerConfig reload(ServletConfig config)
            throws TechnicalException
                 , PackageException
    {
        // Just get rid of the previous one and instantiate a new one.
        INSTANCE = null;
        return getInstance(config);
    }

    /**
     * Return the singleton instance.
     *
     * Return the instance if it exists, without taking the params into account.
     * That's correct, but can be confusing.  TODO: Document it, or maybe find
     * a more flexible mechanism.
     */
    public static synchronized ServerConfig getInstance(ServletConfig config)
            throws TechnicalException
                 , PackageException
    {
        if ( INSTANCE == null ) {
            ServletContext ctxt = config.getServletContext();
            if ( LOG.isInfoEnabled() ) {
                Enumeration<String> names = ctxt.getInitParameterNames();
                while ( names.hasMoreElements() ) {
                    String n = names.nextElement();
                    LOG.info("Init Servlex - param " + n + ": " + ctxt.getInitParameter(n));
                }
            }
            String dir = ctxt.getInitParameter(REPO_DIR_PROPERTY);
            String cp  = ctxt.getInitParameter(REPO_CP_PROPERTY);
            LOG.info("Init Servlex - dir=" + dir + ", cp=" + cp);
            if ( dir == null && cp == null ) {
                INSTANCE = new ServerConfig();
            }
            else {
                INSTANCE = new ServerConfig(dir, cp);
            }
        }
        return INSTANCE;
    }

    /**
     * Return the singleton instance.
     *
     * TODO: Return the instance if it exists, without taking the params into
     * account.  That's correct, but can be confusing.  Document it, or maybe
     * find a more flexible mechanism.
     */
    public static synchronized ServerConfig getInstance(String repo_dir, String repo_classpath)
            throws TechnicalException
                 , PackageException
    {
        if ( INSTANCE == null ) {
            INSTANCE = new ServerConfig(repo_dir, repo_classpath);
        }
        return INSTANCE;
    }

    /**
     * Return the singleton instance.
     *
     * TODO: Return the instance if it exists, without taking the param into
     * account.  That's correct, but can be confusing.  Document it, or maybe
     * find a more flexible mechanism.
     */
    public static synchronized ServerConfig getInstance(Storage repo_storage)
            throws TechnicalException
                 , PackageException
    {
        if ( INSTANCE == null ) {
            INSTANCE = new ServerConfig(repo_storage);
        }
        return INSTANCE;
    }

    /**
     * Return an application given its name.
     * 
     * Throw an exception if there is no application with that name.
     */
    public Application getApplication(String appname)
            throws ServlexException
    {
        Application app = myApps.get(appname);
        if ( app == null ) {
            LOG.error("404: Application not found: " + appname);
            throw new ServlexException(404, "Page not found");
        }
        return app;
    }

    /**
     * Return the list of application names.
     */
    public Set<String> getApplicationNames()
    {
        return myApps.keySet();
    }

    /**
     * Return the processors.
     */
    public Processors getProcessors()
    {
        return myProcessors;
    }

    /**
     * Install a webapp (or a library) in the repository.
     *
     * Return the name of the newly installed webapp, or null if the package
     * is not a webapp.
     * 
     * TODO: The param 'force' is set to 'true', make it configurable.
     */
    public synchronized String install(File archive)
            throws TechnicalException
                 , PackageException
    {
        Package pkg = myRepo.installPackage(archive, true, new LoggingUserInteraction());
        return doInstall(pkg);
    }

    /**
     * Install a webapp (or a library) in the repository.
     *
     * Return the name of the newly installed webapp, or null if the package
     * is not a webapp.
     * 
     * TODO: The param 'force' is set to 'true', make it configurable.
     */
    public synchronized String install(URI uri)
            throws TechnicalException
                 , PackageException
    {
        Package pkg = myRepo.installPackage(uri, true, new LoggingUserInteraction());
        return doInstall(pkg);
    }

    private String doInstall(Package pkg)
            throws TechnicalException
    {
        EXPathWebParser parser = new EXPathWebParser(myProcessors);
        Application app = parser.loadPackage(pkg);
        if ( app == null ) {
            // not a webapp
            return null;
        }
        else {
            // package is a webapp
            myApps.put(app.getName(), app);
            return app.getName();
        }
    }

    /**
     * Remove a webapp (or a library) in the repository.
     */
    public synchronized void remove(String appname)
            throws PackageException
                 , ParseException
    {
        Application app = myApps.get(appname);
        if ( app == null ) {
            throw new PackageException("The application is not installed: " + appname);
        }
        Package pkg = app.getPackage();
        myRepo.removePackage(pkg.getName(), true, new LoggingUserInteraction());
        myApps.remove(appname);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ServerConfig.class);
    /** The resource name of the version properties file. */
    private static final String VERSION_RSRC = "/org/expath/servlex/tools/version.properties";
    /** The property for the version number. */
    private static final String VERSION_PROP = "org.expath.servlex.version";
    /** The property for the revision number. */
    private static final String REVISION_PROP = "org.expath.servlex.revision";

    /** The singleton instance. */
    private static ServerConfig INSTANCE;

    /** The Servlex implementation version. */
    private String myVersion;
    /** The Servlex implementation revision number. */
    private String myRevision;
    /** The repository for webapps. */
    private Repository myRepo;
    /** The storage used by the repository. */
    private Storage myStorage;
    /** The XSLT processor. */
    private Processors myProcessors;
    /** The application map. */
    private Map<String, Application> myApps;
    /** Include request and response content in the logs? */
    private boolean myTraceContent = false;
    /** Default charset to use when none is set on the request. */
    private String myDefaultCharset = null;
    /** The profile directory, if profiling is enabled. */
    private File myProfileDir;

    /**
     * Interaction always return default, and log messages.
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
