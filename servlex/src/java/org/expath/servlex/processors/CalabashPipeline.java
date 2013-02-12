/****************************************************************************/
/*  File:       ServlexPipeline.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRunnable;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XPipeline;
import java.io.File;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Logger;
import org.expath.pkg.calabash.PkgConfigurer;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.saxon.ConfigHelper;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.SaxonHelper;

/**
 * Abstract an XProc pipeline.
 *
 * @author Florent Georges
 * @date   2013-02-12
 */
public class CalabashPipeline
{
    public CalabashPipeline(CalabashProcessor calabash, String pipe)
    {
        myCalabash = calabash;
        myPipe = pipe;
    }

    public CalabashPipeline(CalabashProcessor calabash, XdmNode pipe)
    {
        myCalabash = calabash;
        myPipeNode = pipe;
    }

    /**
     * Prepare the compiled pipeline for evaluation.
     * 
     * TODO: After discussions with Norm, it seems this is the only correct
     * way to go currently.  XProcRuntime is NOT the Calabash equivalent of
     * Saxon's Processor.  It is, well, the runtime for one pipeline.  So
     * nothing is cacheable with Calabash, because there is no processor,
     * and compiled pipelines cannot be evaluated in a concurrent way (they
     * can be reused in a sequence way, by resetting the pipeline, but not
     * at the same time so not in a web server).  I still hope to have a
     * better design in a later version of Calabash.
     */
    public XPipeline prepare()
            throws ComponentError
                 , ServlexException
    {
        Processor saxon = myCalabash.getSaxon();
        XProcConfiguration xconf = new XProcConfiguration(saxon);
        XProcRuntime runtime = new XProcRuntime(xconf);
        runtime.setMessageListener(new MsgListener());
        SaxonRepository repo = myCalabash.getRepo();
        PkgConfigurer configurer = new PkgConfigurer(runtime, repo.getUnderlyingRepo());
        runtime.setConfigurer(configurer);
        String logdir_prop = System.getProperty(ServerConfig.LOG_DIR_PROPERTY);
        if ( logdir_prop != null ) {
            File logdir = new File(logdir_prop);
            if ( ! logdir.exists() ) {
                throw new ServlexException(500, "Calabash log dir does not exist!");
            }
            File logfile = new File(logdir, System.nanoTime() + "-profile.xml");
            // TODO: What if the file already exists?
            runtime.setProfileFile(logfile.getAbsolutePath());
        }
        try {
            // FIXME: Have to reconfigure the Saxon processor, because Calabash
            // install its own resolvers.  Should be ok though, but double-check!
            ConfigHelper helper = new ConfigHelper(repo);
            helper.config(saxon.getUnderlyingConfiguration());
            // compile the pipeline
            if ( myPipeNode == null ) {
                LOG.debug("About to compile the pipeline: " + myPipe);
                return runtime.load(myPipe);
            }
            else {
                LOG.debug("About to compile the pipeline document: " + myPipeNode.getBaseURI());
                return runtime.use(myPipeNode);
            }
        }
        catch ( SaxonApiException ex ) {
            LOG.error("Error compiling pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
        catch ( PackageException ex ) {
            LOG.error("Error compiling pipeline", ex);
            throw new ServlexException(500, "Error compiling pipeline", ex);
        }
    }

    private String myPipe;
    private XdmNode myPipeNode;
    private CalabashProcessor myCalabash;
    /** The specific logger. */
    private static final Logger LOG = Logger.getLogger(CalabashPipeline.class);

    /**
     * TODO: We should keep the default implementation from Calabash (that is,
     * not defining this class and not calling setMessageListener() on
     * myCalabash).  The default implem uses java.util.logging, just find how
     * to configure it properly (also used by EXPath Repo...)
     */
    private static class MsgListener
            implements XProcMessageListener
    {
        public void error(XProcRunnable step, XdmNode node, String message, QName code) {
            LOG.error("[calabash] ERROR: " + message + ", " + code + " in " + step + ": " + node);
        }

        public void error(Throwable exception) {
            LOG.error("[calabash] ERROR: " + exception, exception);
        }

        public void warning(XProcRunnable step, XdmNode node, String message) {
            LOG.warn("[calabash] WARNING: " + message + " in " + step + ": " + node);
        }

        public void warning(Throwable exception) {
            LOG.warn("[calabash] WARNING: " + exception, exception);
        }

        public void info(XProcRunnable step, XdmNode node, String message) {
            LOG.info("[calabash] INFO: " + message + " in " + step + ": " + node);
        }

        public void fine(XProcRunnable step, XdmNode node, String message) {
            LOG.debug("[calabash] FINE: " + message + " in " + step + ": " + node);
        }

        public void finer(XProcRunnable step, XdmNode node, String message) {
            LOG.debug("[calabash] FINER: " + message + " in " + step + ": " + node);
        }

        public void finest(XProcRunnable step, XdmNode node, String message) {
            LOG.trace("[calabash] FINEST: " + message + " in " + step + ": " + node);
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
