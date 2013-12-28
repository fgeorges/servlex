/****************************************************************************/
/*  File:       InstallFromCxanCall.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;

/**
 * Implements web:install-from-cxan().
 * 
 * The XPath signatures:
 *
 *     web:install-from-cxan($repo    as item(),
 *                           $domain  as xs:string,
 *                           $id      as xs:string?,
 *                           $name    as xs:string?,
 *                           $version as xs:string?) as xs:string?
 *
 *     web:install-from-cxan($repo    as item(),
 *                           $domain  as xs:string,
 *                           $id      as xs:string?,
 *                           $name    as xs:string?,
 *                           $version as xs:string?,
 *                           $root    as xs:string) as xs:string?
 *
 * The parameter $repo must be a {@link RepositoryItem}.
 * 
 * If the function returns no string, then it installed a regular library
 * package (not a webapp).  Parameters $id and $name are mutually exclusive,
 * and exactly one has to be set.
 * 
 * TODO: Maybe return more information about the webapp, as XML elements.
 * 
 * Possible XPath errors:
 * 
 * - web:not-found: if the URL generated from the parameters returns 404.
 * 
 * - web:online-error: if there is any error whilst downloading the package at
 *   the URL generated from the parameters.
 * 
 * - web:invalid-param: if none of $id and $name are provided at all, or if
 *   both are provided at the same time.
 * 
 * - web:cannot-install: if installation is disabled on the repository (if it
 *   is read-only).
 * 
 * - web:already-installed: if the package is already installed.
 * 
 * - web:invalid-context-root: if the provided context root is not syntactically
 *   valid.
 * 
 * @author Florent Georges
 * @date   2013-09-16
 */
public class InstallFromCxanCall
        extends ExtensionFunctionCall
{
    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 5, 6);
        WebRepository repo    = params.asRepository(0, false);
        String        domain  = params.asString(1, false);
        String        id      = params.asString(2, true);
        String        name    = params.asString(3, true);
        String        version = params.asString(4, true);
        String        root    = null;
        if ( params.number() == 6 ) {
            root = params.asString(5, false);
        }
        // log it
        LOG.debug(params.format(InstallFromCxanFunction.LOCAL_NAME)
                .param(repo).param(domain).param(id)
                .param(name).param(version).param(root)
                .value());
        // do it
        String value = doit(repo, domain, id, name, version, root);
        return FunReturn.value(value);
    }

    private String doit(WebRepository repo, String domain, String id, String name, String version, String root)
            throws XPathException
    {
        // validate $id and $name (mutually exclusive but one required)
        if ( id == null && name == null ) {
            throw FunErrors.invalidParam("Neither CXAN ID or package name provided, at least one is required.");
        }
        else if ( id != null && name != null ) {
            throw FunErrors.invalidParam("Both CXAN ID and package name provided: resp. '" + id + "' and '" + name + "'.");
        }
        // the URL
        String url = "http://" + domain + "/file?";
        if ( name == null ) {
            url += "id=" + id;
        }
        else {
            url += "name=" + name;
        }
        if ( version != null ) {
            url += "&version=" + version;
        }
        // do it
        try {
            // no config parameter are set when installing from CXAN
            Map<String, String> config = new HashMap<>();
            // TODO: Set whether to override an existing package (instead of false),
            // from an extra param...?
            return repo.install(new URI(url), root, false, config);
        }
        catch ( URISyntaxException ex ) {
            throw FunErrors.invalidParam("Parameters resulted in an invalid URL: " + url, ex);
        }
        catch ( WebRepository.CannotInstall ex ) {
            throw FunErrors.cannotInstall(ex);
        }
        catch ( WebRepository.InvalidContextRoot ex ) {
            throw FunErrors.invalidContextRoot(ex);
        }
        catch ( Repository.AlreadyInstalledException ex ) {
            throw FunErrors.alreadyInstalled(ex);
        }
        catch ( Repository.NotFoundException ex ) {
            throw FunErrors.notFound(ex);
        }
        catch ( Repository.HttpException ex ) {
            throw FunErrors.onlineError(ex);
        }
        catch ( Repository.OnlineException ex ) {
            throw FunErrors.onlineError(ex);
        }
        catch ( TechnicalException | PackageException ex ) {
            throw FunErrors.unexpected(ex);
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(InstallFromCxanCall.class);
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
