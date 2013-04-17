/****************************************************************************/
/*  File:       SaxonXSLTTemplate.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.PackageException;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.Component;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.XdmConnector;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.SaxonHelper;

/**
 * ...
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
class SaxonXSLTTemplate
        implements Component
{
    public SaxonXSLTTemplate(Processor saxon, String import_uri, String ns, String localname)
    {
        mySaxon = saxon;
        myImportUri = import_uri;
        myNS = ns;
        myLocal = localname;
    }

    @Override
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
        throws ServlexException
             , ComponentError
    {
        try {
            XsltExecutable exec = getCompiled();
            XsltTransformer trans = exec.load();
            trans.setInitialTemplate(new QName(ServlexConstants.PRIVATE_NS, "main"));
            connector.connectToXSLTComponent(trans, config);
            XdmDestination dest = new XdmDestination();
            trans.setDestination(dest);
            trans.transform();
            // TODO: As per XSLT, this is always a doc node.  Check that.  But for
            // now, I take the doc's children as the result sequence...
            // TODO: BTW, check this is a document node...
            XdmNode doc = dest.getXdmNode();
            List<XdmItem> children = new ArrayList<XdmItem>();
            XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
            while ( it.hasNext() ) {
                children.add(it.next());
            }
            return new XdmConnector(new XdmValue(children));
        }
        catch ( SaxonApiException ex ) {
            LOG.error("User error in pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
        catch ( PackageException ex ) {
            LOG.error("Internal error", ex);
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    private synchronized XsltExecutable getCompiled()
            throws PackageException
                 , SaxonApiException
    {
        if ( myCompiled == null ) {
            XsltCompiler c = mySaxon.newXsltCompiler();
            String style = makeCallSheet(myImportUri, myNS, myLocal);
            Source src = new StreamSource(new StringReader(style));
            src.setSystemId(ServlexConstants.PRIVATE_NS + "?generated-for=" + myImportUri);
            myCompiled = c.compile(src);
        }
        return myCompiled;
    }

    private static String makeCallSheet(String import_uri, String ns, String local)
    {
        return SaxonXSLTFunction.makeCallSheet(false, import_uri, ns, local);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(SaxonXSLTTemplate.class);

    private Processor mySaxon;
    private String myImportUri;
    private String myNS;
    private String myLocal;
    private XsltExecutable myCompiled = null;
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
