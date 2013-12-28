/****************************************************************************/
/*  File:       ResourceInvocation.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-08-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.runtime;

import org.expath.servlex.model.Resource;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Storage;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.connectors.ResourceConnector;
import org.expath.servlex.model.Application;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.RegexPattern;

/**
 * Represent a specific invocation of an application's resource, at a specific URI.
 *
 * @author Florent Georges
 */
public class ResourceInvocation
        extends Invocation
{
    public ResourceInvocation(Resource rsrc, String path, RequestConnector request, RegexPattern regex, String rewrite)
    {
        super(null, path, request);
        myRsrc = rsrc;
        myRegex = regex;
        myRewrite = rewrite;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("resource invocation: " + myRegex);
        myRsrc.cleanup(auditor);
    }

    @Override
    public Connector invoke(Connector connector, Application app, ServerConfig config, Auditor auditor)
            throws ServlexException
    {
        auditor.invoke(
                "resource", getName(), getPath(),
                myRegex == null ? "" : myRegex.toString(),
                myRewrite);
        String orig_path = getPath();
        try {
            String path = myRegex.replace(orig_path, myRewrite);
            Package pkg = myRsrc.getApplication().getPackage();
            Source src = pkg.getResolver().resolveComponent(path);
            // return a 404 if the resource does not exist
            if ( src == null ) {
                throw new ServlexException(404, "Page not found");
            }
            StreamSource stream = null;
            if ( src instanceof StreamSource ) {
                stream = (StreamSource) src;
            }
            else {
                throw new ServlexException(500, "The resource is not a StreamSource: " + src.getClass());
            }
            InputStream in = stream.getInputStream();
            String type = myRsrc.getType();
            return new ResourceConnector(in, 200, type, app.getProcessors(), auditor);
        }
        catch ( Storage.NotExistException ex ) {
            LOG.error("Page not found: " + orig_path, ex);
            throw new ServlexException(404, "Page not found");
        }
        catch ( PackageException ex ) {
            LOG.error("Internal server error serving: " + orig_path, ex);
            throw new ServlexException(500, "Internal server error");
        }
        catch ( TechnicalException ex ) {
            LOG.error("Internal server error serving: " + orig_path, ex);
            throw new ServlexException(500, "Internal server error");
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ResourceInvocation.class);

    private final Resource     myRsrc;
    private final RegexPattern myRegex;
    private final String       myRewrite;
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
