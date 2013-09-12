/****************************************************************************/
/*  File:       InstallWebappCall.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.Logger;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * Implements web:install-webapp().
 * 
 * The XPath signature:
 *
 *     web:install-webapp($pkg  as xs:base64Binary,
 *                        $root as xs:string) as xs:boolean
 *
 * @author Florent Georges
 * @date   2013-09-11
 */
public class InstallWebappCall
        extends ExtensionFunctionCall
{
    public InstallWebappCall(WebRepository repo)
    {
        myRepo = repo;
    }

    @Override
    public SequenceIterator call(SequenceIterator[] orig_params, XPathContext ctxt)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 2, 2);
        byte[] pkg  = params.asBinary(0, false);
        String root = params.asString(1, false);
        // log it
        LOG.debug(params.format(InstallWebappFunction.LOCAL_NAME).param(pkg).param(root).value());
        // do it
        boolean value = doit(pkg, root);
        try {
            return SaxonHelper.toSequenceIterator(value);
        }
        catch ( TechnicalException ex ) {
            throw new XPathException("Error in the data model", ex);
        }
    }

    private boolean doit(byte[] pkg, String root)
            throws XPathException
    {
        if ( ! myRepo.canInstall() ) {
            throw new XPathException("Installation not supported on repo");
        }
        throw new XPathException("Not implemented yet, installing webapp at: " + root);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(InstallWebappCall.class);

    /** The repository. */
    private WebRepository myRepo;
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
