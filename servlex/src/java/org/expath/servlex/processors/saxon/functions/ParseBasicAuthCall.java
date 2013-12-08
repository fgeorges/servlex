/****************************************************************************/
/*  File:       ParseBasicAuthCall.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-04                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.Base64BinaryValue;
import org.apache.log4j.Logger;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * See {@link ParseBasicAuthFunction}.
 *
 * @author Florent Georges
 * @date   2012-05-04
 */
public class ParseBasicAuthCall
        extends ExtensionFunctionCall
{
    public ParseBasicAuthCall(Processors procs)
    {
        myProcs = procs;
    }

    @Override
    public SequenceIterator call(SequenceIterator[] orig_params, XPathContext ctxt)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 1, 1);
        String header = params.asString(0, false);
        // log it
        // TODO: FIXME: Don't log it in prod, it contains password unencrypted!
        LOG.debug(params.format(ParseBasicAuthFunction.LOCAL_NAME).param(header).value());
        // parsing the header
        header = header.trim();
        if ( ! header.startsWith("Basic ") ) {
            throw new XPathException("Basic auth string wrong format, does not start with 'Basic '");
        }
        header = header.substring(6);
        Base64BinaryValue value = new Base64BinaryValue(header);
        byte[] bytes = value.getBinaryValue();
        String decoded = new String(bytes);
        int colon = decoded.indexOf(':');
        if ( colon < 0 ) {
            throw new XPathException("Basic auth string wrong format, does not contain ':'");
        }
        String username = decoded.substring(0, colon);
        String password = decoded.substring(colon + 1);
        // build the resulting element
        return buildResult(username, password);
    }

    private SequenceIterator buildResult(String username, String password)
            throws XPathException
    {
        try {
            // build the resulting element
            TreeBuilder b = myProcs.makeTreeBuilder(NS, PREFIX);
            b.startElem("basic-auth");
            b.attribute("username", username);
            b.attribute("password", password);
            b.startContent();
            b.endElem();
            // return the basic-auth element, inside the document node
            Document doc = b.getRoot();
            XdmNode root = SaxonHelper.getDocumentRootElement(doc);
            NodeInfo node = root.getUnderlyingNode();
            return SingletonIterator.makeIterator(node);
        }
        catch ( TechnicalException ex ) {
            String msg = "Technical exception occured in Saxon extension function";
            throw new XPathException(msg, ex);
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ParseBasicAuthCall.class);
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
