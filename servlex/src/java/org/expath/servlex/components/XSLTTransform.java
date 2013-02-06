/****************************************************************************/
/*  File:       XSLTTransform.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.components;

import com.xmlcalabash.core.XProcRuntime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
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
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.XdmConnector;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.SaxonHelper;

/**
 * ...
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public class XSLTTransform
        implements Component
{
    public XSLTTransform(String stylesheet)
    {
        myStyle = stylesheet;
    }

    @Override
    public Connector run(Processor saxon, XProcRuntime calabash, Connector connector)
        throws ServlexException
             , ComponentError
    {
        try {
            XsltExecutable exec = getCompiled(saxon);
            XsltTransformer trans = exec.load();
            connector.connectToStylesheet(trans, saxon);
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
        catch ( PackageException ex ) {
            LOG.error("Internal error", ex);
            throw new ServlexException(500, "Internal error", ex);
        }
        catch ( SaxonApiException ex ) {
            LOG.error("User error in pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
        catch ( TransformerException ex ) {
            LOG.error("Internal error", ex);
            throw new ServlexException(500, "Internal error", ex);
        }
    }
 
    private synchronized XsltExecutable getCompiled(Processor proc)
            throws PackageException
                 , SaxonApiException
                 , TransformerException
    {
        if ( myCompiled == null ) {
            XsltCompiler c = proc.newXsltCompiler();
            // saxon's xslt compiler does not use its uri resolver on the param
            // passed directly to the stream source ctor; the resolver is used
            // only for xsl:import and xsl:include, so we have to call it first
            // explicitely
            URIResolver resolver = c.getURIResolver();
            Source src = ( resolver == null )
                    ? null
                    : resolver.resolve(myStyle, null);
            if ( src == null ) {
                src = new StreamSource(myStyle);
            }
            myCompiled = c.compile(src);
        }
        return myCompiled;
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(XSLTTransform.class);

    private String myStyle;
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
