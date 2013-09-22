/****************************************************************************/
/*  File:       SaxonCalabash.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import org.expath.servlex.processors.saxon.model.SaxonDocument;
import org.expath.servlex.processors.saxon.model.SaxonItem;
import org.expath.servlex.processors.saxon.model.SaxonSequence;
import org.expath.servlex.processors.saxon.model.SaxonEmptySequence;
import javax.xml.transform.Source;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.value.Base64BinaryValue;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.Serializer;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.processors.XProcProcessor;
import org.expath.servlex.processors.XQueryProcessor;
import org.expath.servlex.processors.XSLTProcessor;

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
            throws TechnicalException
    {
        try {
            myRepo = new SaxonRepository(repo);
            mySaxon = SaxonHelper.makeSaxon(myRepo, this, config);
            myXslt = new SaxonXSLT(mySaxon);
            myXQuery = new SaxonXQuery(mySaxon, repo);
            myXProc = new CalabashXProc(mySaxon, myRepo, config, this);
        }
        catch ( PackageException ex ) {
            throw new TechnicalException("Error initializing the saxon and calabash processors", ex);
        }
    }

    public SaxonRepository getRepository()
    {
        return myRepo;
    }

    @Override
    public XSLTProcessor getXSLT()
    {
        return myXslt;
    }

    @Override
    public XQueryProcessor getXQuery()
    {
        return myXQuery;
    }

    @Override
    public XProcProcessor getXProc()
    {
        return myXProc;
    }

    @Override
    public Serializer makeSerializer()
            throws TechnicalException
    {
        return new SaxonSerializer(mySaxon);
    }

    @Override
    public TreeBuilder makeTreeBuilder(String uri, String prefix)
            throws TechnicalException
    {
        return new SaxonTreeBuilder(mySaxon, uri, prefix);
    }

    @Override
    public Sequence emptySequence()
            throws TechnicalException
    {
        return makeEmptySequence();
    }

    @Override
    public Sequence buildSequence(Iterable<Item> items)
            throws TechnicalException
    {
        return new SaxonSequence(items);
    }

    @Override
    public Document buildDocument(Source src)
            throws TechnicalException
    {
        try {
            DocumentBuilder builder = mySaxon.newDocumentBuilder();
            XdmNode doc = builder.build(src);
            return new SaxonDocument(doc);
        }
        catch ( SaxonApiException ex ) {
            throw new TechnicalException("Error building a document from Source " + src, ex);
        }
    }

    @Override
    public Item buildString(String value)
            throws TechnicalException
    {
        XdmItem item = new XdmAtomicValue(value);
        return new SaxonItem(item);
    }

    @Override
    public Item buildBinary(byte[] value)
            throws TechnicalException
    {
        XdmItem item = TodoBinaryItem.makeBinaryItem(value);
        return new SaxonItem(item);
    }

    public static Sequence makeEmptySequence()
            throws TechnicalException
    {
        return SaxonEmptySequence.getInstance();
    }

    /**
     * TODO: Work around, see http://saxon.markmail.org/thread/sufwctvikfphdh2m
     */
    private static class TodoBinaryItem
            extends XdmItem
    {
        public static XdmItem makeBinaryItem(byte[] bytes)
        {
            net.sf.saxon.om.Item value = new Base64BinaryValue(bytes);
            return wrapItem(value);
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
