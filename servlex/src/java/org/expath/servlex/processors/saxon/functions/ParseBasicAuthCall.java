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
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.processors.saxon.SaxonHelper;
import org.expath.servlex.tools.Log;

/**
 * See {@link ParseBasicAuthFunction}.
 *
 * @author Florent Georges
 */
public class ParseBasicAuthCall
        extends ExtensionFunctionCall
{
    public ParseBasicAuthCall(Processors procs)
    {
        myProcs = procs;
    }

    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 1, 1);
        String header = params.asString(0, false);
        // log it
        // TODO: FIXME: Don't log it in prod, it contains password unencrypted!
        LOG.debug(params.format(ParseBasicAuthFunction.LOCAL_NAME).param(header).value());
        // parse the header
        Credentials creds = parse(header.trim());
        try {
            Document doc = buildResult(creds);
            // return the element, inside the document node
            XdmNode elem = SaxonHelper.getDocumentRootElement(doc);
            return FunReturn.value(elem);
        }
        catch ( TechnicalException ex ) {
            String msg = "Technical exception occured extracting the root element";
            throw new XPathException(msg, ex);
        }
    }

    private Credentials parse(String header)
            throws XPathException
    {
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
        String user = decoded.substring(0, colon);
        String pwd  = decoded.substring(colon + 1);
        return new Credentials(user, pwd);
    }

    private Document buildResult(Credentials creds)
            throws XPathException
    {
        try {
            // build the resulting element
            TreeBuilder b = myProcs.makeTreeBuilder(NS, PREFIX);
            b.startElem("basic-auth");
            b.attribute("username", creds.myUser);
            b.attribute("password", creds.myPwd);
            b.startContent();
            b.endElem();
            // return the basic-auth element, inside the document node
            return b.getRoot();
        }
        catch ( TechnicalException ex ) {
            String msg = "Technical exception occured in Saxon extension function";
            throw new XPathException(msg, ex);
        }
    }

    /** The logger. */
    private static final Log LOG = new Log(ParseBasicAuthCall.class);
    /** Shortcuts. */
    private static final String NS     = ServlexConstants.WEBAPP_NS;
    private static final String PREFIX = ServlexConstants.WEBAPP_PREFIX;
    /** The processors. */
    private final Processors myProcs;

    private static class Credentials
    {
        public Credentials(String user, String pwd)
        {
            myUser = user;
            myPwd  = pwd;
        }
        public final String myUser;
        public final String myPwd;
    }
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
