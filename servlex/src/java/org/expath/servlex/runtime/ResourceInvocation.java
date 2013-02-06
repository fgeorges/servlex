/****************************************************************************/
/*  File:       ResourceInvocation.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-08-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.runtime;

import org.expath.servlex.model.Resource;
import com.xmlcalabash.core.XProcRuntime;
import java.io.InputStream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.functions.regex.JRegularExpression;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Storage;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.connectors.ResourceConnector;

/**
 * Represent a specific invocation of an application's resource, at a specific URI.
 *
 * @author Florent Georges
 * @date   2010-08-17
 */
public class ResourceInvocation
        extends Invocation
{
    public ResourceInvocation(Resource rsrc, String path, RequestConnector request, String java_regex, String rewrite)
    {
        super(path, request);
        myRsrc = rsrc;
        myJavaRegex = java_regex;
        myRewrite = rewrite;
    }

    @Override
    public Connector invoke(Connector connector, Processor saxon, XProcRuntime calabash)
            throws ServlexException
    {
        String path = replaceMatches(getPath());
        try {
            Package pkg = myRsrc.getApplication().getPackage();
            StreamSource rsrc = pkg.getResolver().resolveComponent(path);
            // return a 404 if the resource does not exist
            if ( rsrc == null ) {
                throw new ServlexException(404, "Page not found");
            }
            InputStream in = rsrc.getInputStream();
            return new ResourceConnector(in, 200, myRsrc.getType());
        }
        catch ( Storage.NotExistException ex ) {
            LOG.error("Page not found: " + getPath(), ex);
            throw new ServlexException(404, "Page not found");
        }
        catch ( PackageException ex ) {
            LOG.error("Internal server error serving: " + getPath(), ex);
            throw new ServlexException(500, "Internal server error");
        }
    }

    /**
     * Replace the matches in the actual path if there is a rewrite attribute.
     */
    private String replaceMatches(String path)
            throws ServlexException
    {
        if ( myRewrite == null ) {
            return path;
        }
        else {
            try {
                JRegularExpression re = new JRegularExpression(myJavaRegex, 0);
                return re.replace(path, myRewrite).toString();
            }
            catch ( XPathException ex ) {
                throw new ServlexException(500, "Error replacing matches in pattern", ex);
            }
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ResourceInvocation.class);

    private Resource myRsrc;
    private String   myJavaRegex;
    private String   myRewrite;
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
