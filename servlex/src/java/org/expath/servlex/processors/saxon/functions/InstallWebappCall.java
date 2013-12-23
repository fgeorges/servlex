/****************************************************************************/
/*  File:       InstallWebappCall.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;

/**
 * Implements web:install-webapp().
 * 
 * The XPath signatures:
 *
 *     web:install-webapp($repo as item(),
 *                        $pkg  as xs:base64Binary) as xs:string?
 *
 *     web:install-webapp($repo as item(),
 *                        $pkg  as xs:base64Binary,
 *                        $root as xs:string) as xs:string?
 *
 * The parameter $repo must be a {@link RepositoryItem}.
 * 
 * If the function returns no string, then it installed a regular library
 * package (not a webapp).
 * 
 * TODO: Maybe return more information about the webapp, as XML elements.
 * 
 * Possible XPath errors:
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
 * @date   2013-09-11
 */
public class InstallWebappCall
        extends ExtensionFunctionCall
{
    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 2, 3);
        WebRepository repo = params.asRepository(0, false);
        byte[]        pkg  = params.asBinary(1, false);
        String        root = null;
        if ( params.number() == 3 ) {
            root = params.asString(2, false);
        }
        // log it
        LOG.debug(params.format(InstallWebappFunction.LOCAL_NAME).param(repo).param(pkg).param(root).value());
        // do it
        String value = doit(repo, pkg, root);
        return FunReturn.value(value);
    }

    private String doit(WebRepository repo, byte[] pkg, String root)
            throws XPathException
    {
        File file = save(pkg);
        try {
            // TODO: Set whether to override an existing package (instead of false),
            // from an extra param...?
            return repo.install(file, root, false);
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
        catch ( TechnicalException ex ) {
            throw FunErrors.unexpected(ex);
        }
        catch ( PackageException ex ) {
            throw FunErrors.unexpected(ex);
        }
    }

    private File save(byte[] pkg)
            throws XPathException
    {
        String id;
        try {
            id = Servlex.getRequestMap().getPrivate("web:request-id");
        }
        catch ( TechnicalException ex ) {
            throw FunErrors.unexpected("Error accessing the request ID", ex);
        }
        File dir = null;
        try {
            dir = File.createTempFile("servlex-", id);
        }
        catch ( IOException ex ) {
            throw FunErrors.unexpected("Error creating a temporary dir", ex);
        }
        dir.delete();
        dir.mkdirs();
        // find a better name for the file?
        File file = new File(dir, "webapp-to-install.xar");
        try {
            OutputStream out = new FileOutputStream(file);
            IOUtils.write(pkg, out);
        }
        catch ( IOException ex ) {
            throw FunErrors.unexpected("Error writing the package to a temporary file: " + file, ex);
        }
        return file;
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(InstallWebappCall.class);
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
