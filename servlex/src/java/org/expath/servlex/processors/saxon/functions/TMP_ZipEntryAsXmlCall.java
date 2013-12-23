/****************************************************************************/
/*  File:       ConfigParamCall.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.Logger;

/**
 * Implements web:tmp--zip-entry-as-xml().
 * 
 *     web:tmp--zip-entry-as-xml($zip   as xs:base64Binary,
 *                               $entry as xs:string) as document-node?
 *
 * @author Florent Georges
 * @date   2013-12-19
 */
public class TMP_ZipEntryAsXmlCall
        extends ExtensionFunctionCall
{
    public TMP_ZipEntryAsXmlCall(Processor saxon)
    {
        mySaxon = saxon;
    }

    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 2);
        byte[] zip   = params.asBinary(0, false);
        String entry = params.asString(1, false);
        // log it
        FunParams.Formatter fmt = params.format(TMP_ZipEntryAsXmlFunction.LOCAL_NAME);
        LOG.debug(fmt.param(zip).param(entry).value());
        // do it
        XdmNode doc = doit(zip, entry);
        return FunReturn.value(doc);
    }

    private XdmNode doit(byte[] zip, String entry_name)
            throws XPathException
    {
        InputStream entry = getEntry(zip, entry_name);
        if ( entry == null ) {
            return null;
        }
        Source src = new StreamSource(entry);
        DocumentBuilder builder = mySaxon.newDocumentBuilder();
        try {
            return builder.build(src);
        }
        catch ( SaxonApiException ex ) {
            throw new XPathException("Error building a document from Source " + src, ex);
        }
    }

    private InputStream getEntry(byte[] zip_bin, String entry_name)
            throws XPathException
    {
        InputStream in = new ByteArrayInputStream(zip_bin);
        ZipInputStream zip = new ZipInputStream(in);
        ZipEntry entry;
        try {
            while ( (entry = zip.getNextEntry()) != null ) {
                if ( entry.getName().equals(entry_name) ) {
                    break;
                }
            }
        }
        catch ( IOException ex ) {
            throw new XPathException("Error reading ZIP binary", ex);
        }
        if ( entry == null ) {
            return null;
        }
        return zip;
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(TMP_ZipEntryAsXmlCall.class);
    /** The Saxon processor object. */
    private final Processor mySaxon;
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
