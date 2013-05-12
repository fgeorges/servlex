/****************************************************************************/
/*  File:       ServlexPipeline.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcProcessor;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XPipeline;
import java.io.File;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Logger;
import org.expath.pkg.calabash.PkgConfigurer;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.saxon.ConfigHelper;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;

/**
 * Abstract an XProc pipeline.
 *
 * @author Florent Georges
 * @date   2013-02-12
 */
public class CalabashPipeline
{
    public CalabashPipeline(CalabashXProc calabash, String pipe, ServerConfig config)
    {
        myCalabash = calabash;
        myPipe = pipe;
        myConfig = config;
    }

    public CalabashPipeline(CalabashXProc calabash, XdmNode pipe, ServerConfig config)
    {
        myCalabash = calabash;
        myPipeNode = pipe;
        myConfig = config;
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
    public XPipeline prepare(Auditor auditor)
            throws ComponentError
                 , ServlexException
    {
        auditor.compilationStarts("xproc");
        XPipeline result;
        try {
            // instantiate the runtime
            XProcRuntime runtime = getRuntime();
            // compile the pipeline
            if ( myPipeNode == null ) {
                LOG.debug("About to compile the pipeline: " + myPipe);
                result = runtime.load(myPipe);
            }
            else {
                LOG.debug("About to compile the pipeline document: " + myPipeNode.getBaseURI());
                result = runtime.use(myPipeNode);
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
        catch ( TechnicalException ex ) {
            LOG.error("Error compiling pipeline", ex);
            throw new ServlexException(500, "Error compiling pipeline", ex);
        }
        auditor.compilationStops();
        return result;
    }

    /**
     * Prepare a new runtime, not compiling any pipeline, not handling errors.
     */
    private XProcRuntime getRuntime()
            throws PackageException
                 , ServlexException
                 , TechnicalException
    {
        Processor saxon = myCalabash.getSaxon();
        XProcConfiguration xconf = new XProcConfiguration(saxon);
        XProcProcessor proc = new XProcProcessor(xconf);
        SaxonRepository repo = myCalabash.getRepository();
        PkgConfigurer configurer = new PkgConfigurer(repo.getUnderlyingRepo());
        proc.setConfigurer(configurer);
        XProcRuntime runtime = new XProcRuntime(proc);
        // runtime.setMessageListener(new MsgListener());
        File profiling = myConfig.getProfileFile("xproc-profile");
        if ( profiling != null ) {
            runtime.setProfileOutput(profiling);
        }
        // FIXME: Have to reconfigure the Saxon processor, because Calabash
        // install its own resolvers.  Should be ok though, but double-check!
        ConfigHelper helper = new ConfigHelper(repo);
        helper.config(saxon.getUnderlyingConfiguration());
        return runtime;
    }

    /** The specific logger. */
    private static final Logger LOG = Logger.getLogger(CalabashPipeline.class);

    /** The pipeline URI. Mutually exclusive with myPipeNode. */
    private String myPipe;
    /** The pipeline XML representation in memory. Mutually exclusive with myPipe. */
    private XdmNode myPipeNode;
    /** The Calabash processor. */
    private CalabashXProc myCalabash;
    /** The configuration object. */
    private ServerConfig myConfig;
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
