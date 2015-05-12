/****************************************************************************/
/*  File:       SaxonXQueryFunction.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.components;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.Component;
import org.expath.servlex.components.ComponentInstance;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.XdmConnector;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.saxon.model.SaxonSequence;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.processors.saxon.SaxonHelper;
import org.expath.servlex.tools.Log;

/**
 * A component that is an XQuery function.
 *
 * @author Florent Georges
 */
public class SaxonXQueryFunction
        implements Component
{
    public SaxonXQueryFunction(Processor saxon, String ns, String localname)
    {
        mySaxon = saxon;
        myNS = ns;
        myLocal = localname;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("saxon xquery function");
    }

    @Override
    public void logApplication(Log log)
    {
        log.debug("      XQuery Function");
        log.debug("         ns   : " + myNS);
        log.debug("         local: " + myLocal);
    }

    @Override
    public Connector run(Connector connector, ServerConfig config, Auditor auditor)
        throws ServlexException
             , ComponentError
    {
        auditor.run("xquery function");
        XQueryExecutable exec = getCompiled();
        XQueryEvaluator eval = exec.load();
        ComponentInstance instance = new MyInstance(eval);
        connector.connectToXQueryFunction(instance, config);
        XdmValue result;
        try {
            result = eval.evaluate();
        }
        catch ( SaxonApiException ex ) {
            LOG.error(formatMsg("User error in XQuery"), ex);
            throw SaxonHelper.makeError(ex);
        }
        Sequence seq = new SaxonSequence(result);
        return new XdmConnector(seq, auditor);
    }

    /**
     * Return a compiled query calling the function.
     * 
     * The query takes a global parameter $input, and passes it to the function
     * call.  The compiled object is cached (it is compiled only once).
     */
    private synchronized XQueryExecutable getCompiled()
            throws ServlexException
    {
        if ( myCompiled == null ) {
            LOG.debug(formatMsg("Going to generate query for"));
            XQueryCompiler c = mySaxon.newXQueryCompiler();
            try {
                myCompiled = c.compile(
                    "import module namespace my = \"" + myNS + "\";\n"
                    + "declare variable $input external;\n"
                    + "my:" + myLocal + "($input)\n");
            }
            catch ( SaxonApiException ex ) {
                String msg = formatMsg("Error compiling the generated query for calling");
                LOG.error(msg, ex);
                throw new ServlexException(500, msg, ex);
            }
        }
        return myCompiled;
    }

    /**
     * Format a message with the function name (using Clark notation).
     */
    private String formatMsg(String msg)
    {
        return msg + " function: {" + myNS + "}" + myLocal;
    }

    /** The logger. */
    private static final Log LOG = new Log(SaxonXQueryFunction.class);

    /** The Saxon instance. */
    private Processor mySaxon;
    /** The namespace URI of the function. */
    private String myNS;
    /** The local name of the function. */
    private String myLocal;
    /** The cached generated query calling the function. */
    private XQueryExecutable myCompiled = null;

    /**
     * An instance of this component.
     */
    private static class MyInstance
            implements ComponentInstance
    {
        public MyInstance(XQueryEvaluator eval)
        {
            myEval = eval;
        }

        public void connect(Sequence input)
        {
            if ( ! (input instanceof SaxonSequence) ) {
                throw new IllegalStateException("Not a Saxon sequence: " + input);
            }
            SaxonSequence seq = (SaxonSequence) input;
            myEval.setExternalVariable(NAME, seq.makeSaxonValue());
        }

        public void error(ComponentError error, Document request)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private static QName NAME = new QName("input");
        private XQueryEvaluator myEval;
    }
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
