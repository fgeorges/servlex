/****************************************************************************/
/*  File:       ExecuteCall.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.TreeBuilder;
import net.servlex.saxon.SaxonHelper;
import org.expath.servlex.tools.Log;

/**
 * See {@link ExecuteFunction}.
 * 
 * This function was introduced for the EXPath website, to update the local Git
 * repository with the latest pages.  Two reasons why it has been disabled
 * since:
 * 
 *   - this is not how we should update the repository, it has to be done on
 *     a regular basis., outside of Servlex, e.g. via cron;
 * 
 *   - this function does not have to be provided by Servlex itself anyway,
 *     but can be written as a standalone extension library (that could be
 *     reused outside of Servlex then); the only difference would be that it
 *     would become more difficult to have a white list of webapps allowed to
 *     use it.
 *
 * @author Florent Georges
 */
public class ExecuteCall
        extends ExtensionFunctionCall
{
    public ExecuteCall(Processors procs)
    {
        myProcs = procs;
    }

    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        if ( true ) {
            throw new XPathException("web:execute disabled");
        }
        // the params
        FunParams params = new FunParams(orig_params, 1, 1);
        NodeInfo program = params.asElement(0, false, "exec-program");
        // log it
        LOG.debug(params.format(ExecuteFunction.LOCAL_NAME).param(program).value());
        // parse the input into myCwd, myProgram and myOptions
        parseExecProgram(program);
        // TODO: FIXME: Using ProcessBuilder.start() must be conditioned to a
        // webapp white list!
        ProcessBuilder builder = new ProcessBuilder(myCommand);
        Process proc;
        try {
            proc = builder.start();
        }
        catch ( IOException ex ) {
            throw new XPathException("Error when executing: " + myCommand.get(0), ex);
        }
        // the result values
        String code   = Integer.toString(proc.exitValue());
        String stdout = asString(proc.getInputStream());
        String stderr = asString(proc.getInputStream());
        // build the resulting element
        return buildResult(code, stdout, stderr);
    }

    private void parseExecProgram(NodeInfo program)
            throws XPathException
    {
        AxisIterator it = program.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
        NodeInfo child;
        while ( (child = it.next()) != null ) {
            String ns   = child.getURI();
            String name = child.getLocalPart();
            if ( ! ServlexConstants.WEBAPP_NS.equals(ns) ) {
                String msg = "Namespace of web:exec-program child is not the webapp namespace: ";
                throw new XPathException(msg + SaxonHelper.clarkName(child));
            }
            switch ( name ) {
                case "cwd":
                    String cwd = child.getStringValue();
                    File dir = new File(cwd);
                    if ( ! dir.isDirectory() ) {
                        String msg = "web:exec-program/web:cwd is not an existing dir: ";
                        throw new XPathException(msg + cwd + " (" + dir + ")");
                    }   myCwd = dir;
                    break;
                case "program":
                    String pgr = child.getStringValue();
                    if ( ! myCommand.isEmpty() ) {
                        throw new XPathException("Program must come before the options: " + pgr);
                    }   myCommand.add(pgr);
                    break;
                case "option":
                    String opt = child.getStringValue();
                    myCommand.add(opt);
                    break;
                default:
                    String msg = "Unknown element as child of web:exec-program: ";
                    throw new XPathException(msg + SaxonHelper.clarkName(child));
            }
        }
    }

    private Sequence buildResult(String code, String stdout, String stderr)
            throws XPathException
    {
        try {
            // build the resulting element
            TreeBuilder b = myProcs.makeTreeBuilder(NS, PREFIX);
            b.startElem("exec-result");
            b.attribute("code", code);
            b.startContent();
            b.textElem("stdout", stdout);
            b.textElem("stderr", stderr);
            b.endElem();
            // return the basic-auth element, inside the document node
            Document doc = b.getRoot();
            XdmNode root = SaxonHelper.getDocumentRootElement(doc);
            return root.getUnderlyingNode();
        }
        catch ( TechnicalException ex ) {
            String msg = "Technical exception occured in Saxon extension function";
            throw new XPathException(msg, ex);
        }
    }

    private String asString(InputStream in)
            throws XPathException
    {
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader buffered = new BufferedReader(reader);
        String line;
        StringBuilder result = new StringBuilder();
        try {
            while ( (line = buffered.readLine()) != null ) {
                result.append(line);
                result.append("\n");
            }
        }
        catch ( IOException ex ) {
            throw new XPathException("Error when reading the output of: " + myCommand.get(0), ex);
        }
        return result.toString();
    }

    /** The logger. */
    private static final Log LOG = new Log(ExecuteCall.class);
    /** Shortcuts. */
    private static final String NS     = ServlexConstants.WEBAPP_NS;
    private static final String PREFIX = ServlexConstants.WEBAPP_PREFIX;
    /** The processors. */
    private final Processors myProcs;
    /** The current directory, from element(exec-program). */
    private File myCwd;
    /** The command, first one must be the program name. */
    private final List<String> myCommand = new ArrayList<>();
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
