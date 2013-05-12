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
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;
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
    public SequenceIterator call(SequenceIterator[] params, XPathContext ctxt)
            throws XPathException
    {
        // num of params
        if ( params.length != 1 ) {
            throw new XPathException("There is not exactly 1 param: " + params.length);
        }
        // the first param
        Item first = params[0].next();
        if ( first == null ) {
            throw new XPathException("The 1st param is an empty sequence");
        }
        if ( params[0].next() != null ) {
            throw new XPathException("The 1st param sequence has more than one item");
        }
        if ( ! ( first instanceof StringValue ) ) {
            throw new XPathException("The 1st param is not a string");
        }
        String value = first.getStringValue();
        try {
            TreeBuilder b = myProcs.makeTreeBuilder(NS, PREFIX);
            // parsing the header
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("Parse header value: '" + value + "'");
            }
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
            // return the header element, inside the document node
            Document doc = b.getRoot();
            XdmNode root = SaxonHelper.getDocumentRootElement(doc);
            return SingletonIterator.makeIterator(root.getUnderlyingNode());
        }
        catch ( TechnicalException ex ) {
            String msg = "Technical exception occured in Saxon extension function";
            throw new XPathException(msg, ex);
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ParseHeaderValueCall.class);
    /** Shortcuts. */
    private static final String NS     = ServlexConstants.WEBAPP_NS;
    private static final String PREFIX = ServlexConstants.WEBAPP_PREFIX;
    /** The processors. */
    private Processors myProcs;
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
