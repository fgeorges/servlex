/****************************************************************************/
/*  File:       XQueryCall.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2016-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2016 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import org.expath.servlex.tools.Log;

/**
 * Implements web:xquery().
 * 
 *     web:xquery($query as xs:string,
 *                $input as document-node()) as item()*
 *
 * @author Florent Georges
 */
public class XQueryCall
        extends ExtensionFunctionCall
{
    public XQueryCall(Processor saxon)
    {
        mySaxon = saxon;
    }

    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 2);
        String   query = params.asString(0, false);
        NodeInfo input = params.asDocument(1, false);
        // log it
        LOG.debug("About to execute: " + XQueryFunction.LOCAL_NAME);
        // do it
        XQueryEvaluator expr = compile(query);
        expr.setContextItem(new XdmNode(input));
        return run(expr);
    }

    private XQueryEvaluator compile(String query)
            throws XPathException
    {
        XQueryExecutable prog;
        try {
            XQueryCompiler compiler = mySaxon.newXQueryCompiler();
            prog = compiler.compile(query);
        }
        catch ( SaxonApiException ex ) {
            throw new XPathException("Error compiling the query: " + ex, ex);
        }
        return prog.load();
    }

    private Sequence run(XQueryEvaluator expr)
            throws XPathException
    {
        try {
            return expr.evaluate().getUnderlyingValue();
        }
        catch ( SaxonApiException ex ) {
            throw new XPathException("Error running the query: " + ex, ex);
        }
    }

    /** The logger. */
    private static final Log LOG = new Log(XQueryCall.class);
    /** The Saxon processor. */
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
