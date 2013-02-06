/****************************************************************************/
/*  File:       Connector.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XPipeline;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XsltTransformer;
import org.expath.servlex.ServlexException;

/**
 * Encapsulate a connection between two components.
 *
 * @author Florent Georges
 * @date   2011-02-06
 */
public interface Connector
{
    /** Connect to an XQuery function. */
    public void connectToXQueryFunction(XQueryEvaluator eval, Processor saxon)
            throws ServlexException;
    /** Connect to an XQuery main module. */
    public void connectToQuery(XQueryEvaluator eval, Processor saxon)
            throws ServlexException;
    /** Connect to an XSLT component, either a function or a named template. */
    public void connectToXSLTComponent(XsltTransformer trans, Processor saxon)
            throws ServlexException;
    /** Connect to an XSLT stylesheet. */
    public void connectToStylesheet(XsltTransformer trans, Processor saxon)
            throws ServlexException;
    /** Connect to an XProc pipeline. */
    public void connectToPipeline(XPipeline pipeline, Processor saxon, XProcRuntime calabash)
            throws ServlexException;
    /** Connect to the final HTTP Servlet response. */
    public void connectToResponse(HttpServletResponse resp, Processor saxon, XProcRuntime calabash)
            throws ServlexException, IOException;
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
