/****************************************************************************/
/*  File:       CalabashProcessor.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import org.expath.pkg.saxon.SaxonRepository;

/**
 * Abstract an XProc processor.
 *
 * @author Florent Georges
 * @date   2013-02-12
 */
public class CalabashProcessor
{
    public CalabashProcessor(Processor saxon, SaxonRepository repo)
    {
        mySaxon = saxon;
        myRepo = repo;
    }

    public CalabashPipeline compile(String pipe)
    {
        return new CalabashPipeline(this, pipe);
    }

    public CalabashPipeline compile(XdmNode pipe)
    {
        return new CalabashPipeline(this, pipe);
    }

    public Processor getSaxon()
    {
        return mySaxon;
    }

    public SaxonRepository getRepo()
    {
        return myRepo;
    }

    private Processor mySaxon;
    private SaxonRepository myRepo;
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
