/****************************************************************************/
/*  File:       SaxonXQuery.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import org.expath.servlex.processors.saxon.components.SaxonXQueryFunction;
import org.expath.servlex.processors.saxon.components.SaxonXQueryModule;
import net.sf.saxon.s9api.Processor;
import org.expath.pkg.repo.Repository;
import org.expath.servlex.components.Component;
import org.expath.servlex.processors.XQueryProcessor;

/**
 * The Saxon implementation of the XQuery processor.
 *
 * @author Florent Georges
 */
class SaxonXQuery
        implements XQueryProcessor
{
    // FIXME: Repo shoud not be needed here...
    public SaxonXQuery(Processor saxon, Repository repo)
    {
        mySaxon = saxon;
        myRepo = repo;
    }

    public Component makeQuery(String uri)
    {
        return new SaxonXQueryModule(mySaxon, myRepo, uri);
    }

    public Component makeFunction(String ns, String localname)
    {
        return new SaxonXQueryFunction(mySaxon, ns, localname);
    }

    /** FIXME: Should not be needed here. */
    private Repository myRepo;
    /** The Saxon instance. */
    private Processor mySaxon;
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
