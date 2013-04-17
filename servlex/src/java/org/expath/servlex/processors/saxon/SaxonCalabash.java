/****************************************************************************/
/*  File:       SaxonCalabash.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import javax.xml.transform.Source;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Serializer;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.processors.XProcProcessor;
import org.expath.servlex.processors.XQueryProcessor;
import org.expath.servlex.processors.XSLTProcessor;
import org.expath.servlex.tools.SaxonHelper;

/**
 * XSLT, XQuery and XProc processors based on Saxon and Calabash.
 *
 * @author Florent Georges
 * @date   2013-04-15
 */
public class SaxonCalabash
    implements Processors
{
    public SaxonCalabash(Repository repo, ServerConfig config)
            throws PackageException
    {
        myRepo = new SaxonRepository(repo);
        mySaxon = SaxonHelper.makeSaxon(myRepo, this);
        myXslt = new SaxonXSLT(mySaxon);
        myXQuery = new SaxonXQuery(mySaxon, repo);
        myXProc = new CalabashXProc(mySaxon, myRepo, config);
    }

    public SaxonRepository getRepository()
    {
        return myRepo;
    }

    public XSLTProcessor getXSLT()
    {
        return myXslt;
    }

    public XQueryProcessor getXQuery()
    {
        return myXQuery;
    }

    public XProcProcessor getXProc()
    {
        return myXProc;
    }

    public Serializer makeSerializer()
            throws TechnicalException
    {
        return new SaxonSerializer(mySaxon);
    }

    public TreeBuilder makeTreeBuilder(String uri, String prefix)
            throws TechnicalException
    {
        return new SaxonTreeBuilder(mySaxon, uri, prefix);
    }

    public XdmNode buildDocument(Source src)
            throws TechnicalException
    {
        try {
            DocumentBuilder builder = mySaxon.newDocumentBuilder();
            return builder.build(src);
        }
        catch ( SaxonApiException ex ) {
            throw new TechnicalException("Error building a document from Source " + src, ex);
        }
    }

    private Processor mySaxon;
    private SaxonRepository myRepo;
    private SaxonXSLT myXslt;
    private SaxonXQuery myXQuery;
    private CalabashXProc myXProc;
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
