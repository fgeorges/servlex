/****************************************************************************/
/*  File:       RemoveWebappCall.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;
import org.expath.servlex.tools.Log;

/**
 * Implements web:remove-webapp().
 * 
 * The XPath signature:
 *
 *     web:remove-webapp($repo as item(),
 *                       $root as xs:string) as xs:boolean
 *
 * The parameter $repo must be a {@link RepositoryItem}.
 * 
 * The function returns true if the webapp deployed at the context root
 * {@code root} has been properly un-installed.
 * 
 * Possible XPath errors:
 * 
 * - web:cannot-install: if installation is disabled on the repository (if it
 *   is read-only).
 * 
 * - web:invalid-context-root: if there is no application deployed at the
 *   provided context root.
 * 
 * - web:unexpected: for any other error.
 * 
 * @author Florent Georges
 */
public class RemoveWebappCall
        extends ExtensionFunctionCall
{
    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 2);
        WebRepository repo = params.asRepository(0, false);
        String        root = params.asString(1, false);
        // log it
        LOG.debug(params.format(RemoveWebappFunction.LOCAL_NAME).param(repo).param(root).value());
        // do it
        boolean value = doit(repo, root);
        return FunReturn.value(value);
    }

    private boolean doit(WebRepository repo, String root)
            throws XPathException
    {
        try {
            repo.remove(root);
            return true;
        }
        catch ( WebRepository.CannotInstall ex ) {
            throw FunErrors.cannotInstall(ex);
        }
        catch ( WebRepository.InvalidContextRoot ex ) {
            throw FunErrors.invalidContextRoot(ex);
        }
        catch ( TechnicalException | PackageException ex ) {
            throw FunErrors.unexpected(ex);
        }
    }

    /** The logger. */
    private static final Log LOG = new Log(RemoveWebappCall.class);
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
