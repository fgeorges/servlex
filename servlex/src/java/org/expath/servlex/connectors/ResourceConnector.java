/****************************************************************************/
/*  File:       ResourceConnector.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XPipeline;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XsltTransformer;
import org.expath.servlex.ServlexException;

/**
 * Connector for a resource, can be connected only to the http servlet response.
 *
 * @author Florent Georges
 * @date   2011-02-06
 */
public class ResourceConnector
        implements Connector
{
    /**
     * Constructor.
     * 
     * @param in The input stream to get the resource content from.
     * @param status The HTTP status code to set on the response.
     * @param type The MIME content type to set on the response.
     */
    public ResourceConnector(InputStream in, int status, String type)
    {
        myIn = in;
        myStatus = status;
        myType = type;
    }

    @Override
    public void connectToXQueryFunction(XQueryEvaluator eval, Processor saxon)
            throws ServlexException
    {
        throw new ServlexException(500, "Cannot connect a resource to an XQuery function.");
    }

    @Override
    public void connectToQuery(XQueryEvaluator eval, Processor saxon)
            throws ServlexException
    {
        throw new ServlexException(500, "Cannot connect a resource to an XQuery main module.");
    }

    @Override
    public void connectToXSLTComponent(XsltTransformer trans, Processor saxon)
            throws ServlexException
    {
        throw new ServlexException(500, "Cannot connect a resource to an XSLT function or template.");
    }

    @Override
    public void connectToStylesheet(XsltTransformer trans, Processor saxon)
            throws ServlexException
    {
        throw new ServlexException(500, "Cannot connect a resource to a stylesheet.");
    }

    @Override
    public void connectToPipeline(XPipeline pipeline, Processor saxon, XProcRuntime calabash)
            throws ServlexException
    {
        throw new ServlexException(500, "Cannot connect a resource to a pipeline.");
    }

    @Override
    public void connectToResponse(HttpServletResponse resp, Processor saxon, XProcRuntime calabash)
            throws ServlexException
                 , IOException
    {
        OutputStream out = null;
        try {
            out = resp.getOutputStream();
            byte[] buf = new byte[4096];
            int len;
            while ( (len = myIn.read(buf)) > 0 ) {
                out.write(buf, 0, len);
            }
            resp.setStatus(myStatus);
            resp.setContentType(myType);
        }
        finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    private InputStream myIn;
    private int myStatus;
    private String myType;
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
