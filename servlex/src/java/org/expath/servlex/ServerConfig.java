/****************************************************************************/
/*  File:       ServerConfig.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
import org.expath.servlex.model.Application;
import org.expath.servlex.parser.EXPathWebParser;
import org.expath.servlex.parser.ParseException;
import org.expath.servlex.processors.Processors;

import static org.expath.servlex.ServlexConstants.DEFAULT_CHARSET_PROPERTY;
import static org.expath.servlex.ServlexConstants.DEFAULT_PROCESSORS;
import static org.expath.servlex.ServlexConstants.PROCESSORS_PROPERTY;
import static org.expath.servlex.ServlexConstants.PROFILE_DIR_PROPERTY;
import static org.expath.servlex.ServlexConstants.REPO_CP_PROPERTY;
import static org.expath.servlex.ServlexConstants.REPO_DIR_PROPERTY;
import static org.expath.servlex.ServlexConstants.TRACE_CONTENT_PROPERTY;


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
    /**
     * Initialize the webapp list from the repository got from system properties.
     */
    protected ServerConfig()
            throws TechnicalException
    {
        this(System.getProperty(REPO_DIR_PROPERTY), System.getProperty(REPO_CP_PROPERTY));
    }

    /**
     * Initialize the webapp list from the repository got from either parameter.
     */
    protected ServerConfig(String repo_dir, String repo_classpath)
            throws TechnicalException
    {
        this(getStorage(repo_dir, repo_classpath));
    }

    /**
     * Initialize the webapp list from the repository constructed over the storage.
     */
    protected ServerConfig(Storage storage)
            throws TechnicalException
    {
        this(storage, initRepo(storage));
    }

    /**
     * Initialize the webapp list from the repository.
     * 
     * TODO: This constructor should only take a repository, not both a repository
     * and a storage.  Once I update the pkg-repo.jar, I can use the new getStorage()
     * method on Repository...
     */
    protected ServerConfig(Storage storage, Repository repo)
            throws TechnicalException
    {
        LOG.info("ServerConfig with storage: " + storage + ", and repository: " + repo);
        myStorage = storage;
        myRepo = repo;
        String class_name = System.getProperty(PROCESSORS_PROPERTY);
        if ( class_name == null ) {
            class_name = DEFAULT_PROCESSORS;
        }
        init(getProcessors(class_name));
    }

    /**
     * Initialize the webapp list from the repository and the processors implementation.
     * 
     * TODO: This constructor should only take a repository, not both a repository
     * and a storage.  Once I update the pkg-repo.jar, I can use the new getStorage()
     * method on Repository...
     */
    protected ServerConfig(Storage storage, Repository repo, Processors procs)
            throws TechnicalException
    {
        LOG.info("ServerConfig with storage: " + storage + ", and repository: " + repo + ", and processors: " + procs);
        myStorage = storage;
        myRepo = repo;
        init(procs);
    }

    private void init(Processors procs)
            throws TechnicalException
    {
        myProcessors = procs;
        myProfileDir = initProfiling();
        myTraceContent = initTracing();
        myDefaultCharset = initCharset();
        myApps = initApplications(myRepo, this);
    }

    private static Repository initRepo(Storage storage)
            throws TechnicalException
    {
        try {
            return new Repository(storage);
        }
        catch ( PackageException ex ) {
            throw new TechnicalException("Error initializing the repository", ex);
        }
    }

    public synchronized Processors getProcessors(String class_name)
            throws TechnicalException
    {
        // if in the map, return it
        Processors procs = myProcessorsMap.get(class_name);
        if ( procs != null ) {
            return procs;
        }
        // if not, instantiate it
        try {
            // get the raw class object
            ClassLoader loader = ServerConfig.class.getClassLoader();
            Class<?> class_raw = loader.loadClass(class_name);
            // check it implements Processors
            if ( ! Processors.class.isAssignableFrom(class_raw) ) {
                String msg = "The processors implementation must implement Processors: ";
                throw new TechnicalException(msg + class_name);
            }
            // get the ctor
            Class<Processors> clazz = (Class<Processors>) class_raw;
            Constructor<Processors> ctor = clazz.getConstructor(Repository.class, ServerConfig.class);
            // instantiate
            procs = ctor.newInstance(myRepo, this);
            myProcessorsMap.put(class_name, procs);
            return procs;
        }
        catch ( ClassNotFoundException ex ) {
            String msg = "The processors implementation class not found: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( NoSuchMethodException ex ) {
            String msg = "The processors implementation must have a constructor(Repository,ServerConfig): ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( SecurityException ex ) {
            String msg = "Servlex must have access to the processors implementation: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( InstantiationException ex ) {
            String msg = "The processors implementation must be instantiable: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( IllegalAccessException ex ) {
            String msg = "Servlex must have access to the processors implementation: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( IllegalArgumentException ex ) {
            String msg = "The processors implementation constructor must accept the Repository and ServerConfig: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( InvocationTargetException ex ) {
            String msg = "The processors implementation constructor threw an exception: ";
            throw new TechnicalException(msg + class_name, ex);
        }
    }

    private static File initProfiling()
            throws TechnicalException
    {
        String value = System.getProperty(PROFILE_DIR_PROPERTY);
        File dir = null;
        if ( value != null ) {
            dir = new File(value);
            if ( ! dir.exists() ) {
                LOG.error("Calabash profile dir does not exist, disabling profiling (" + dir + ")");
                dir = null;
            }
        }
        return dir;
    }

    private static boolean initTracing()
            throws TechnicalException
    {
        String name = TRACE_CONTENT_PROPERTY;
        String value = System.getProperty(name);
        if ( value == null ) {
            return false;
        }
        else if ( "true".equals(value) ) {
            return true;
        }
        else if ( "false".equals(value) ) {
            return false;
        }
        else {
            throw new TechnicalException("Invalid value for the property " + name + ": " + value);
        }
    }

    private static String initCharset()
            throws TechnicalException
    {
        return System.getProperty(DEFAULT_CHARSET_PROPERTY);
    }

    private static Map<String, Application> initApplications(Repository repo, ServerConfig config)
            throws TechnicalException
    {
        // the parser
        EXPathWebParser parser = new EXPathWebParser(config);
        // the application map
        Map<String, Application> applications = new HashMap<String, Application>();
        // parse and save the result in the map
        for ( Application app : parser.parseDescriptors(repo.listPackages()) ) {
            applications.put(app.getName(), app);
            LOG.info("Add the application to the store: " + app.getName());
        }
        return applications;
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

    private static Storage getStorage(String repo_dir, String repo_classpath)
            throws TechnicalException
    {
        try {
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
        catch ( PackageException ex ) {
            throw new TechnicalException("Error initializing the storage", ex);
        }
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
    public Processors getDefaultProcessors()
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
        EXPathWebParser parser = new EXPathWebParser(this);
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

    /** The singleton instance. */
    private static ServerConfig INSTANCE;

    /** The repository for webapps. */
    private Repository myRepo;
    /** The storage used by the repository. */
    private Storage myStorage;
    /** The map with all processors implementations. */
    private Map<String, Processors> myProcessorsMap = new HashMap<String, Processors>();
    /** The processors. */
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
