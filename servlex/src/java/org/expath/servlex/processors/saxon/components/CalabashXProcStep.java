/****************************************************************************/
/*  File:       CalabashXProcStep.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-09-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.components;

import java.io.StringReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Logger;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.Component;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.processors.XProcProcessor;
import org.expath.servlex.processors.saxon.CalabashPipeline;
import org.expath.servlex.processors.saxon.CalabashXProc;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.processors.saxon.SaxonHelper;

/**
 * TODO: ...
 *
 * @author Florent Georges
 * @date   2010-09-06
 */
public class CalabashXProcStep
        implements Component
{
    public CalabashXProcStep(CalabashXProc calabash, String import_uri, String ns, String localname)
    {
        myCalabash = calabash;
        myImportUri = import_uri;
        myNS = ns;
        myLocal = localname;
    }

    @Override
    public void logApplication(Logger log)
    {
        log.debug("      XProc Step");
        log.debug("         uri  : " + myImportUri);
        log.debug("         ns   : " + myNS);
        log.debug("         local: " + myLocal);
    }

    @Override
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
        throws ServlexException
             , ComponentError
    {
        auditor.run("step");
        try {
            CalabashPipeline pipeline = myCalabash.prepare(auditor);
            XdmNode pipe = makeCallPipe();
            pipeline.compile(pipe);
            return pipeline.evaluate(connector);
        }
        catch ( SaxonApiException ex ) {
            LOG.error("User error in pipeline", ex);
            throw SaxonHelper.makeError(ex);
        }
    }

    /**
     * TODO: Use a tree builder instead of string concatenation!
     */
    private XdmNode makeCallPipe()
            throws SaxonApiException
    {
        StringBuilder b = new StringBuilder();
        b.append("<p:declare-step xmlns:p='http://www.w3.org/ns/xproc'\n");
        b.append("                xmlns:my='").append(myNS).append("'\n");
        b.append("                name='servlex-call-pipe'\n");
        b.append("                version='1.0'>\n");
        b.append("   <p:import href='").append(myImportUri).append("'/>\n");
        b.append("   <p:input port='" + XProcProcessor.INPUT_PORT_NAME +"' sequence='true'/>\n");
        b.append("   <p:output port='" + XProcProcessor.OUTPUT_PORT_NAME + "' sequence='true'>\n");
        b.append("      <p:pipe step='implem' port='" + XProcProcessor.OUTPUT_PORT_NAME + "'/>\n");
        b.append("   </p:output>\n");
        b.append("   <my:").append(myLocal).append(" name='implem'>\n");
        b.append("      <p:input port='" + XProcProcessor.INPUT_PORT_NAME + "'>\n");
        b.append("         <p:pipe step='servlex-call-pipe' port='" + XProcProcessor.INPUT_PORT_NAME + "'/>\n");
        b.append("      </p:input>\n");
        b.append("   </my:").append(myLocal).append(">\n");
        b.append("</p:declare-step>\n");
        String pipe = b.toString();
        LOG.debug("The generated pipeline");
        LOG.debug(pipe);
        Source src = new StreamSource(new StringReader(pipe));
        src.setSystemId(ServlexConstants.PRIVATE_NS + "?generated-for=" + myImportUri);
        DocumentBuilder builder = myCalabash.getSaxon().newDocumentBuilder();
        return builder.build(src);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(CalabashXProcStep.class);

    private CalabashXProc myCalabash;
    private String myImportUri;
    private String myNS;
    private String myLocal;
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
