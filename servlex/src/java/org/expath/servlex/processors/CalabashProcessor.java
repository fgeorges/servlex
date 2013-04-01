/****************************************************************************/
/*  File:       CalabashProcessor.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import net.sf.saxon.s9api.XdmNode;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.tools.Auditor;

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
    public CalabashProcessor(ServerConfig config)
            throws PackageException
    {
        myConfig = config;
    }

    /**
     * Compile a pipeline from a URI.
     */
    public CalabashPipeline compile(String pipe)
    {
        return new CalabashPipeline(this, pipe, myConfig);
    }

    /**
     * Compile a pipeline from an in-memory XML tree.
     */
    public CalabashPipeline compile(XdmNode pipe)
    {
        return new CalabashPipeline(this, pipe, myConfig);
    }

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
