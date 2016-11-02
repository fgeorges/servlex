/****************************************************************************/
/*  File:       Saxon.java                                                  */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.servlex.saxon.model.SaxonDocument;
import net.servlex.saxon.model.SaxonItem;
import net.servlex.saxon.model.SaxonSequence;
import net.servlex.saxon.model.SaxonEmptySequence;
import javax.xml.transform.Source;
import net.sf.saxon.Version;
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

import static org.expath.servlex.ServlexConstants.SAXON_CONFIG_FILE_PROPERTY;
import static org.expath.servlex.ServlexConstants.SAXON_XSLT_VER_DEFAULT;
import static org.expath.servlex.ServlexConstants.SAXON_XSLT_VER_PROPERTY;
import org.expath.servlex.processors.LanguageSupportException;

/**
 * XSLT, XQuery and XProc processors based on Saxon and Calabash.
 * 
 * The "XSLT version" used here is the XSLT version number (a string, either
 * "2.0" or "3.0") to use for the "wrapper stylesheets" used for function and
 * names template components.
 *
 * @author Florent Georges
 */
public class Saxon
    implements Processors
{
    @SuppressWarnings("LeakingThisInConstructor")
    public Saxon(Repository repo, ServerConfig config)
            throws TechnicalException
    {
        List<String> info = new ArrayList<>();
        // the config file property
        String file = System.getProperty(SAXON_CONFIG_FILE_PROPERTY);
        info.add("config file property name: " + SAXON_CONFIG_FILE_PROPERTY);
        info.add("config file property value: " + file);
        try {
            // actually instantiate Saxon
            myRepo = new SaxonRepository(repo);
            mySaxon = SaxonHelper.makeSaxon(myRepo, this, config, file, info);
            myXslt = new SaxonXSLT(this);
            myXQuery = new SaxonXQuery(mySaxon, repo);
        }
        catch ( PackageException ex ) {
            throw new TechnicalException("Error initializing the saxon processors", ex);
        }
        // the XSLT version to use
        String ver = System.getProperty(SAXON_XSLT_VER_PROPERTY, SAXON_XSLT_VER_DEFAULT);
        info.add("xslt version property name: " + SAXON_XSLT_VER_PROPERTY);
        info.add("xslt version property default: " + SAXON_XSLT_VER_DEFAULT);
        info.add("xslt version property value: " + ver);
        if ( ! XSLT_VERSION_RE.matcher(ver).matches() ) {
            throw new TechnicalException("Default XSLT version for wrappers is not valid: '" + ver + "'");
        }
        myXsltVersion = ver;
        // gathering more info on the instantiated Saxon
        info.add("is schema aware: " + mySaxon.isSchemaAware());
        info.add("edition: " + mySaxon.getSaxonEdition());
        info.add("product version: " + mySaxon.getSaxonProductVersion());
        mySaxon.getUnderlyingConfiguration().displayLicenseMessage();
        info.add("software edition: " + Version.softwareEdition);
        info.add("platform: " + Version.platform.getPlatformVersion());
        info.add("config class: " + Version.configurationClass);
        {
            int[] sver = Version.getStructuredVersionNumber();
            info.add("structured version: " + sver[0] + "." + sver[1] + "." + sver[2] + "." + sver[3]);
        }
        info.add("major release date: " + Version.getMajorReleaseDate());
        info.add("release date: " + Version.getReleaseDate());
        info.add("product name: " + Version.getProductName());
        info.add("product title: " + Version.getProductTitle());
        info.add("product vendor: " + Version.getProductVendor());
        info.add("product version: " + Version.getProductVersion());
        info.add("website address: " + Version.getWebSiteAddress());
        myInfo = info.toArray(new String[]{});
    }

    public SaxonRepository getRepository()
    {
        return myRepo;
    }

    public Processor getSaxon()
    {
        return mySaxon;
    }

    public String getWrapperXsltVersion()
    {
        return myXsltVersion;
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
            throws LanguageSupportException
    {
        throw new LanguageSupportException("XProc not supported by this processor");
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

    @Override
    public String[] info()
    {
        return myInfo;
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

    protected String[] myInfo;
    private final Processor mySaxon;
    private final SaxonRepository myRepo;
    private final SaxonXSLT myXslt;
    private final SaxonXQuery myXQuery;
    private final String myXsltVersion;
    private final Pattern XSLT_VERSION_RE = Pattern.compile("^[0-9]\\.[0-9]$");
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
