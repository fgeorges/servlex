/****************************************************************************/
/*  File:       ParseHeaderValueCall.java                                   */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.log4j.Logger;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 * @date   2010-11-26
 */
public class ParseHeaderValueCall
        extends ExtensionFunctionCall
{
    public ParseHeaderValueCall(Processors procs)
    {
        myProcs = procs;
    }

    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 1, 1);
        String value = params.asString(0, false);
        // log it
        LOG.debug(params.format(ParseHeaderValueFunction.LOCAL_NAME).param(value).value());
        try {
            Document doc = doit(value);
            // return the header element, inside the document node
            XdmNode elem = SaxonHelper.getDocumentRootElement(doc);
            return FunReturn.value(elem);
        }
        catch ( TechnicalException ex ) {
            String msg = "Technical exception occured in Saxon extension function";
            throw new XPathException(msg, ex);
        }
    }

    private Document doit(String value)
            throws TechnicalException
    {
        TreeBuilder b = myProcs.makeTreeBuilder(NS, PREFIX);
        b.startElem("header");
        b.startContent();
        HeaderValueParser parser = new BasicHeaderValueParser();
        HeaderElement[] elems = BasicHeaderValueParser.parseElements(value, parser);
        for ( HeaderElement e : elems ) {
            b.startElem("element");
            b.attribute("name", e.getName());
            if ( e.getValue() != null ) {
                b.attribute("value", e.getValue());
            }
            b.startContent();
            for ( NameValuePair p : e.getParameters() ) {
                b.startElem("param");
                b.attribute("name", p.getName());
                if ( p.getValue() != null ) {
                    b.attribute("value", p.getValue());
                }
                b.startContent(); // necessary for an empty element?
                b.endElem();
            }
            b.endElem();
        }
        b.endElem();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Result of parsing header value: " + b.getRoot());
        }
        return b.getRoot();
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ParseHeaderValueCall.class);
    /** Shortcuts. */
    private static final String NS     = ServlexConstants.WEBAPP_NS;
    private static final String PREFIX = ServlexConstants.WEBAPP_PREFIX;
    /** The processors. */
    private final Processors myProcs;
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
