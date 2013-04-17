/****************************************************************************/
/*  File:       SaxonXQueryModule.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import java.io.IOException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.URISpace;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.Component;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.XdmConnector;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.SaxonHelper;

/**
 * A component that is an XQuery Main Module, AKA "a query".
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
class SaxonXQueryModule
        implements Component
{
    // FIXME: We should not need to pass the repo, using getModuleURIResolver should be enough!
    // (see SaxonXSLTTransform...)
    public SaxonXQueryModule(Processor saxon, Repository repo, String uri)
    {
        mySaxon = saxon;
        myRepo = repo;
        myUri  = uri;
    }

    @Override
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
            throws ServlexException
                 , ComponentError
    {
        XQueryExecutable exec = getCompiled(config);
        XQueryEvaluator eval = exec.load();
        connector.connectToQuery(eval, config);
        XdmValue result;
        try {
            result = eval.evaluate();
        }
        catch ( SaxonApiException ex ) {
            LOG.error("User error in XQuery main module at URI: '" + myUri + "'", ex);
            throw SaxonHelper.makeError(ex);
        }
        return new XdmConnector(result);
    }

    /**
     * Return the compiled query.
     * 
     * The compiled object is cached (it is compiled only once).
     */
    private synchronized XQueryExecutable getCompiled(ServerConfig config)
            throws ServlexException
    {
        if ( myCompiled == null ) {
            LOG.debug("Going to compile query: " + myUri);
            StreamSource src = resolve();
            XQueryCompiler compiler = mySaxon.newXQueryCompiler();
            try {
                // TODO: Pass the system ID instead, to compiler.compile()?  If not,
                // how to give Saxon the system ID?
                if ( src.getReader() != null ) {
                    myCompiled = compiler.compile(src.getReader());
                }
                else if ( src.getInputStream() != null ) {
                    myCompiled = compiler.compile(src.getInputStream());
                }
                else {
                    error("Query URI resolve in repo but both reader and stream are null");
                }
            }
            catch ( SaxonApiException ex ) {
                error("Error compiling the query module for URI", ex);
            }
            catch ( IOException ex ) {
                error("Error reading the query module for URI", ex);
            }
        }
        return myCompiled;
    }

    /**
     * Resolve the URI in the repository, in the XQuery URI space.
     */
    private StreamSource resolve()
            throws ServlexException
    {
        StreamSource src = null;
        try {
            src = myRepo.resolve(myUri, URISpace.XQUERY);
        }
        catch ( PackageException ex ) {
            error("Error resolving the query module URI", ex);
        }
        if ( src == null ) {
            error("Query URI does not resolve in repo");
        }
        return src;
    }

    private void error(String msg)
            throws ServlexException
    {
        msg = msg + ": '" + myUri + "'";
        LOG.error(msg);
        throw new ServlexException(500, msg);
    }

    private void error(String msg, Throwable ex)
            throws ServlexException
    {
        msg = msg + ": '" + myUri + "'";
        LOG.error(msg, ex);
        throw new ServlexException(500, msg, ex);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(SaxonXQueryModule.class);

    /** The Saxon instance. */
    private Processor mySaxon;
    /** The package repository where to resolve the query URI. FIXME: Should not be needed here. */
    private Repository myRepo;
    /** The query URI. */
    private String myUri;
    /** The cached compiled query. */
    private XQueryExecutable myCompiled = null;
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
