/****************************************************************************/
/*  File:       CalabashProcessor.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import java.io.File;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.saxon.SaxonRepository;

/**
 * Abstract an XProc processor.
 *
 * @author Florent Georges
 * @date   2013-02-12
 */
public class CalabashProcessor
{
    /**
     * Construct a new Calabash processor, from its underlying Saxon processor and repository.
     * 
     * It uses the value of the property {@code ServerConfig.PROFILE_DIR_PROPERTY},
     * if it exists, to enable Calabash profiling data generation, the value of
     * the property being a directory where to put the corresponding files.  If
     * the directory does not exist, profiling is disabled and a message is logged
     * (but this is not an error).  If the property does not exist, profiling is
     * not enabled.
     */
    public CalabashProcessor(SaxonRepository repo, Processor saxon, File profile_dir)
            throws PackageException
    {
        myRepo = repo;
        mySaxon = saxon;
        myProfileDir = profile_dir;
    }

    /**
     * Compile a pipeline from a URI.
     */
    public CalabashPipeline compile(String pipe)
    {
        return new CalabashPipeline(this, pipe);
    }

    /**
     * Compile a pipeline from an in-memory XML tree.
     */
    public CalabashPipeline compile(XdmNode pipe)
    {
        return new CalabashPipeline(this, pipe);
    }

    /**
     * Return the underlying Saxon processor.
     */
    public Processor getSaxon()
    {
        return mySaxon;
    }

    /**
     * Return the underlying repository.
     */
    public SaxonRepository getRepo()
    {
        return myRepo;
    }

    /**
     * Return the directory to save profiling data, when enabled.  Null if disabled.
     */
    public File getProfileDir()
    {
        return myProfileDir;
    }

    /** The specific logger. */
    private static final Logger LOG = Logger.getLogger(CalabashProcessor.class);

    /** The Saxon processor. */
    private Processor mySaxon;
    /** The repository. */
    private SaxonRepository myRepo;
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
