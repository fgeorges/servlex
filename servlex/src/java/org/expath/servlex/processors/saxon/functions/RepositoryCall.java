/****************************************************************************/
/*  File:       RepositoryCall.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.WebRepository;
import org.expath.servlex.tools.Log;

/**
 * Implements web:installed-webapps().
 * 
 * The XPath signatures:
 *
 *     web:installed-webapps() as xs:string*
 * 
 * TODO: Maybe return more information about each webapp, as XML elements.
 * 
 * @author Florent Georges
 */
public class RepositoryCall
        extends ExtensionFunctionCall
{
    public RepositoryCall(ServerConfig config)
    {
        myConfig = config;
    }

    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 0, 0);
        // log it
        LOG.debug(params.format(RepositoryFunction.LOCAL_NAME).value());
        // do it
        WebRepository repo = myConfig.getRepository();
        return FunReturn.value(repo);
    }

    /** The logger. */
    private static final Log LOG = new Log(RepositoryCall.class);

    /** The repository. */
    private final ServerConfig myConfig;
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
