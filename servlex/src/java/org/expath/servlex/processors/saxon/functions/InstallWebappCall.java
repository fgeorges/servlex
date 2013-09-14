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
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebRepository;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * Implements web:install-webapp().
 * 
 * The XPath signatures:
 *
 *     web:install-webapp($pkg as xs:base64Binary) as xs:string
 *
 *     web:install-webapp($pkg  as xs:base64Binary,
 *                        $root as xs:string) as xs:string
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
        FunParams params = new FunParams(orig_params, 1, 2);
        byte[] pkg  = params.asBinary(0, false);
        String root = null;
        if ( params.number() == 2 ) {
            root = params.asString(1, false);
        }
        // log it
        LOG.debug(params.format(InstallWebappFunction.LOCAL_NAME).param(pkg).param(root).value());
        // do it
        try {
            String value = doit(pkg, root);
            return SaxonHelper.toSequenceIterator(value);
        }
        catch ( TechnicalException ex ) {
            throw new XPathException("Error installing the webapp", ex);
        }
    }

    private String doit(byte[] pkg, String root)
            throws XPathException
                 , TechnicalException
    {
        if ( ! myRepo.canInstall() ) {
            throw new XPathException("Installation not supported on repo");
        }
        File file = save(pkg);
        try {
            // TODO: Set whether to override an existing package (instead of false),
            // from an extra param...?
            return myRepo.install(file, root, false);
        }
        catch ( PackageException ex ) {
            throw new TechnicalException("Error creating a temporary dir", ex);
        }
    }

    private File save(byte[] pkg)
            throws XPathException
                 , TechnicalException
    {
        String id = Servlex.getRequestMap().getPrivate("web:request-id");
        File dir = null;
        try {
            dir = File.createTempFile("servlex-", id);
        }
        catch ( IOException ex ) {
            throw new TechnicalException("Error creating a temporary dir", ex);
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
            throw new TechnicalException("Error writing the package to a temporary file: " + file, ex);
        }
        return file;
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
