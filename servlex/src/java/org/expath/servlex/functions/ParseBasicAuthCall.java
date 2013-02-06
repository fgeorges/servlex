/****************************************************************************/
/*  File:       ParseBasicAuthCall.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-04                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.StringValue;
import org.apache.log4j.Logger;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.tools.SaxonHelper;
import org.expath.servlex.tools.TreeBuilderHelper;

/**
 * See {@link ParseBasicAuthFunction}.
 *
 * @author Florent Georges
 * @date   2012-05-04
 */
public class ParseBasicAuthCall
        extends ExtensionFunctionCall
{
    public ParseBasicAuthCall(Processor saxon)
    {
        mySaxon = saxon;
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
        String header = first.getStringValue();
        // parsing the header
        // TODO: FIXME: Don't log it in prod, it contains password unencrypted!
        LOG.debug("Parse basic auth value: '" + header + "'");
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
        TreeBuilderHelper b = new TreeBuilderHelper(mySaxon, ServlexConstants.WEBAPP_NS, ServlexConstants.WEBAPP_PREFIX);
        b.startElem("basic-auth");
        b.attribute("username", username);
        b.attribute("password", password);
        b.startContent();
        b.endElem();
        // return the basic-auth element, inside the document node
        XdmNode root = null;
        try {
            root = SaxonHelper.getDocumentRootElement(b.getRoot());
        }
        catch ( TechnicalException ex ) {
            String msg = "Error accessing the basic-auth element I just built, cannot happen";
            throw new XPathException(msg, ex);
        }
        return SingletonIterator.makeIterator(root.getUnderlyingNode());
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ParseBasicAuthCall.class);
    /** The Saxon processor. */
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
