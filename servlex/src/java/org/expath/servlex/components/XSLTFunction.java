/****************************************************************************/
/*  File:       XSLTFunction.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.components;

import com.xmlcalabash.core.XProcRuntime;
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
import org.expath.servlex.ServlexConstants;
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
public class XSLTFunction
        implements Component
{
    public XSLTFunction(String import_uri, String ns, String localname)
    {
        myImportUri = import_uri;
        myNS = ns;
        myLocal = localname;
    }

    @Override
    public Connector run(Processor saxon, XProcRuntime calabash, Connector connector)
        throws ServlexException
             , ComponentError
    {
        try {
            XsltExecutable exec = getCompiled(saxon);
            XsltTransformer trans = exec.load();
            trans.setInitialTemplate(new QName(ServlexConstants.PRIVATE_NS, "main"));
            connector.connectToXSLTComponent(trans, saxon);
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

    private synchronized XsltExecutable getCompiled(Processor proc)
            throws PackageException
                 , SaxonApiException
    {
        if ( myCompiled == null ) {
            XsltCompiler c = proc.newXsltCompiler();
            String style = makeCallSheet(true, myImportUri, myNS, myLocal);
            Source src = new StreamSource(new StringReader(style));
            src.setSystemId(ServlexConstants.PRIVATE_NS + "?generated-for=" + myImportUri);
            myCompiled = c.compile(src);
        }
        return myCompiled;
    }

    // TODO: Actually, all the servlet functions and templates within the same
    // stylesheet (the same import URI) can share the same "calling sheet"
    // (usefull when the compiled object will be cached, because that will
    // reduce the number of those) ==> the "calling sheets" should be generated
    // at the deployment...
    //
    // also used by XSLTTemplateEntryPoint (so package-level)
    static String makeCallSheet(boolean is_function, String import_uri, String ns, String local)
    {
        StringBuilder b = new StringBuilder();
        b.append("<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'\n");
        b.append("                xmlns:web='" + ServlexConstants.WEBAPP_NS + "'\n");
        b.append("                xmlns:local='" + ServlexConstants.PRIVATE_NS + "'\n");
        b.append("                xmlns:my='").append(ns).append("'\n");
        b.append("                version='2.0'>\n");
        b.append("   <xsl:import href='").append(import_uri).append("'/>\n");
        b.append("   <xsl:param name='local:input' as='item()*'/>\n");
        b.append("   <xsl:template name='local:main'>\n");
        if ( LOG.isDebugEnabled() ) {
            b.append("      <xsl:message>\n");
            b.append("         THE INPUT: <xsl:copy-of select='$local:input'/>\n");
            b.append("      </xsl:message>\n");
        }
        if ( is_function ) {
            b.append("      <xsl:variable name='res' select='my:").append(local).append("($local:input)'/>\n");
        }
        else {
            b.append("      <xsl:variable name='res' as='item()*'>\n");
            b.append("         <xsl:call-template name='my:").append(local).append("'>\n");
            b.append("            <xsl:with-param name='web:input' select='$local:input'/>\n");
            b.append("         </xsl:call-template>\n");
            b.append("      </xsl:variable>\n");
        }
        if ( LOG.isDebugEnabled() ) {
            b.append("      <xsl:message>\n");
            b.append("         THE OUTPUT: <xsl:copy-of select='$res'/>\n");
            b.append("      </xsl:message>\n");
        }
        b.append("      <xsl:sequence select='$res'/>\n");
        b.append("   </xsl:template>\n");
        b.append("</xsl:stylesheet>\n");
        String sheet = b.toString();
        LOG.debug("The generated stylesheet");
        LOG.debug(sheet);
        return sheet;
    }

    /** The logger. */
    private static final Logger LOG     = Logger.getLogger(XSLTFunction.class);

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
