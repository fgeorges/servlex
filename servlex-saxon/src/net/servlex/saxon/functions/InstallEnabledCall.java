/****************************************************************************/
/*  File:       InstallEnabledCall.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import org.expath.servlex.WebRepository;
import org.expath.servlex.tools.Log;

/**
 * Implements web:install-enabled().
 * 
 * The XPath signature:
 *
 *     web:install-enabled($repo as item()) as xs:boolean
 * 
 * The parameter $repo must be a {@link RepositoryItem}.
 * 
 * @author Florent Georges
 */
public class InstallEnabledCall
        extends ExtensionFunctionCall
{
    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 1, 1);
        WebRepository repo = params.asRepository(0, false);
        // log it
        LOG.debug(params.format(InstallEnabledFunction.LOCAL_NAME).param(repo).value());
        // do it
        boolean value = repo.canInstall();
        return FunReturn.value(value);
    }

    /** The logger. */
    private static final Log LOG = new Log(InstallEnabledCall.class);
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
