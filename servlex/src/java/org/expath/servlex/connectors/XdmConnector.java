/****************************************************************************/
/*  File:       XdmConnector.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.expath.servlex.Result;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.components.ComponentInstance;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.tools.Auditor;

/**
 * Connector to an XDM sequence.
 *
 * TODO: Define the connection between an XDM sequence and... XQuery function,
 * XSLT component, stylesheet, pipeline...
 *
 * TODO: Probably, the most generic mechanism is to say that XDM sequences are
 * flowing between components (servlets, filters, error handlers, etc.) and to
 * remove the special case of a request element + a bodies sequence (which is
 * the general case now for components).  That's hell of a change, think about
 * it carefully!
 *
 * @author Florent Georges
 */
public class XdmConnector
        implements Connector
{
    public XdmConnector(Sequence sequence, Auditor auditor)
    {
        mySequence = sequence;
        myAuditor = auditor;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("sequence");
    }

    @Override
    public Auditor getAuditor()
    {
        return myAuditor;
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToXQueryFunction(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("sequence", "xquery function");
        try {
            comp.connect(mySequence);
        }
        catch ( TechnicalException ex ) {
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    /**
     * TODO: No context node...?
     */
    @Override
    public void connectToQuery(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("sequence", "query");
        try {
            comp.connect(mySequence);
        }
        catch ( TechnicalException ex ) {
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToXSLTComponent(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("sequence", "xslt component");
        try {
            comp.connect(mySequence);
        }
        catch ( TechnicalException ex ) {
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    @Override
    public void connectToStylesheet(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("sequence", "style");
        try {
            comp.connect(mySequence);
        }
        catch ( TechnicalException ex ) {
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToPipeline(ComponentInstance comp, ServerConfig config)
            throws ServlexException
    {
        myAuditor.connect("sequence", "pipeline");
        try {
            comp.connect(mySequence);
        }
        catch ( TechnicalException ex ) {
            throw new ServlexException(500, "Internal error", ex);
        }
    }

    /**
     * TODO: ...
     */
    @Override
    public void connectToResponse(HttpServletResponse resp, ServerConfig config, Processors procs)
            throws ServlexException
                 , IOException
    {
        myAuditor.connect("sequence", "response");
        // TODO: FIXME: The artificial, old class Result should be removed, and
        // its content moved to this class, which is really the one responsible
        // to write an XDM sequence to the HTTP servlet response object.
        Result result = new Result(mySequence, procs);
        result.respond(resp);
    }

    private Sequence mySequence;
    /** The auditor object. */
    private Auditor myAuditor;
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
