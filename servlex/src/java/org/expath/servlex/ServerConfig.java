/****************************************************************************/
/*  File:       ServerConfig.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import java.io.File;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.expath.pkg.repo.ClasspathStorage;
import org.expath.pkg.repo.FileSystemStorage;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.Storage;
import org.expath.servlex.model.Application;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.tools.ProcessorsMap;

import static org.expath.servlex.ServlexConstants.DEFAULT_CHARSET_PROPERTY;
import static org.expath.servlex.ServlexConstants.DEFAULT_PROCESSORS;
import static org.expath.servlex.ServlexConstants.PROCESSORS_PROPERTY;
import static org.expath.servlex.ServlexConstants.PROFILE_DIR_PROPERTY;
import static org.expath.servlex.ServlexConstants.REPO_CP_PROPERTY;
import static org.expath.servlex.ServlexConstants.REPO_DIR_PROPERTY;
import static org.expath.servlex.ServlexConstants.TRACE_CONTENT_PROPERTY;
import org.expath.servlex.tools.Log;


/**
 * Singleton class with the config of the server.
 *
 * TODO: Probably should NOT be a singleton.  What if a (Java) webapp wants to
 * have different instances of Servlex, using different server configuration,
 * with several repositories?
 *
 * @author Florent Georges
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
        LOG.info("ServerConfig by default");
    }

    /**
     * Initialize the webapp list from the repository got from either parameter.
     */
    protected ServerConfig(String repo_dir, String repo_classpath)
            throws TechnicalException
    {
        this(getStorage(repo_dir, repo_classpath));
        LOG.info("ServerConfig with dir: " + repo_dir + ", and classpath: " + repo_classpath);
    }

    /**
     * Initialize the webapp list from the repository constructed over the storage.
     */
    protected ServerConfig(Storage storage)
            throws TechnicalException
    {
        this(initRepo(storage));
        LOG.info("ServerConfig with storage: " + storage);
    }

    /**
     * Initialize the webapp list from the repository.
     */
    protected ServerConfig(Repository repo)
            throws TechnicalException
    {
        LOG.info("ServerConfig with repository: " + repo);
        String clazz = System.getProperty(PROCESSORS_PROPERTY, DEFAULT_PROCESSORS);
        init(new ProcessorsMap(clazz, repo, this), repo);
    }

    /**
     * Initialize the webapp list from the repository and the processors implementation.
     */
    protected ServerConfig(Repository repo, Processors procs)
            throws TechnicalException
    {
        LOG.info("ServerConfig with repository: " + repo + ", and processors: " + procs);
        init(new ProcessorsMap(procs, repo, this), repo);
    }

    /**
     * Return the web repository.
     */
    public WebRepository getRepository()
    {
        return myRepo;
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
            if ( LOG.info() ) {
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
     * Return true if there is one single application, and it has no context root.
     */
    public boolean isSingleApp()
    {
        return myRepo.isSingleApp();
    }

    /**
     * Return the one application.
     * 
     * Throw an exception if not in single application mode.
     */
    public Application getApplication()
            throws ServlexException
    {
        if ( ! isSingleApp() ) {
            throw new ServlexException(500, "Asking for the single app whilst in single app mode");
        }
        return myRepo.getApplication();
    }

    /**
     * Return an application given its name.
     * 
     * Throw an exception if there is no application with that name.
     * Throw an exception if in single application mode.
     */
    public Application getApplication(String appname)
            throws ServlexException
    {
        if ( isSingleApp() ) {
            throw new ServlexException(500, "Asking for app '" + appname + "' in single app mode");
        }
        Application app = myRepo.getApplication(appname);
        if ( app == null ) {
            LOG.error("404: Application not found: " + appname);
            throw new ServlexException(404, "Page not found");
        }
        return app;
    }

    /**
     * Return the processors.
     */
    public Processors getDefaultProcessors()
    {
        return myProcessors.getDefault();
    }

    private void init(ProcessorsMap procs, Repository repo)
            throws TechnicalException
    {
        myProcessors = procs;
        myRepo = new WebRepository(repo, myProcessors);
        myProfileDir = initProfiling();
        myTraceContent = initTracing();
        myDefaultCharset = initCharset();
        // log a summary of the config...
        LOG.info("*** Servlex initialization report ***");
        // simple vars
        LOG.info("[**] profile dir: " + myProfileDir);
        LOG.info("[**] trace content: " + myTraceContent);
        LOG.info("[**] default charset: " + myDefaultCharset);
        // the repo
        LOG.info("[**] the repo: " + myRepo);
        LOG.info("      - can install: " + myRepo.canInstall());
        LOG.info("      - single app: " + myRepo.isSingleApp());
        if ( myRepo.isSingleApp() ) {
            Application app = myRepo.getApplication();
            LOG.info("      - appplication: " + app.getName());
            LOG.info("          o title: " + app.getTitle());
            for ( String p : app.getConfigParamNames() ) {
                LOG.info("          o config param: " + p + " = " + app.getConfigParam(p).getValue());
            }
        }
        else {
            for ( String root : myRepo.getContextRoots() ) {
                Application app = myRepo.getApplication(root);
                LOG.info("      - context root: " + root);
                LOG.info("          o app name: " + app.getName());
                LOG.info("          o app title: " + app.getTitle());
                for ( String p : app.getConfigParamNames() ) {
                    LOG.info("          o app config param: " + p + " = " + app.getConfigParam(p).getValue());
                }
            }
        }
        // processors
        Processors dflt = myProcessors.getDefault();
        String dflt_class = dflt.getClass().getCanonicalName();
        LOG.info("[**] default processor: " + dflt_class);
        for ( String line : dflt.info() ) {
            LOG.info("      - " + line);
        }
        for ( String extra_class : myProcessors.inCache() ) {
            if ( ! extra_class.equals(dflt_class) ) {
                LOG.info("[**] extra available processor: " + dflt_class);
                for ( String line : dflt.info() ) {
                    LOG.info("      - " + line);
                }
            }
        }
        LOG.info("End of Servlex report ***");
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

    /** The logger. */
    private static final Log LOG = new Log(ServerConfig.class);

    /** The singleton instance. */
    private static ServerConfig INSTANCE;

    /** The repository for webapps. */
    private WebRepository myRepo;
    /** The processors implementations. */
    private ProcessorsMap myProcessors;
    /** Include request and response content in the logs? */
    private boolean myTraceContent = false;
    /** Default charset to use when none is set on the request. */
    private String myDefaultCharset = null;
    /** The profile directory, if profiling is enabled. */
    private File myProfileDir;
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
