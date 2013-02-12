/****************************************************************************/
/*  File:       ErrorConnector.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XPipeline;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import net.sf.saxon.s9api.*;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.XProcPipeline;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.CalabashHelper;

/**
 * Connect an XPath error to component implementations.
 *
 * @author Florent Georges
 * @date   2011-02-09
 */
public class ErrorConnector
        implements Connector
{
    /**
     * Build a new object, based on the XPath error (name, message and sequence).
     */
    public ErrorConnector(ComponentError error, RequestConnector request)
    {
        myError   = error;
        myRequest = request;
    }

    /**
     * TODO: Mapping to define, then implement.
     *
     * FIXME: And BOOM!  The mapping here is pretty straightforward: the function
     * accepts 3 parameters: {@code f($errname, $errmsg, $errobject)}.  But the
     * signature really is defined by the component itself (in this case, this is
     * in XQueryFunction), which says: {@code f($request, $bodies)} for all cases.
     *
     * The solution: move the "call sheet" from the components to the connectors
     * (the "call sheets" are the stylesheet generated to get global parameters
     * and pass them to a function or template, importing the original stylesheet
     * by using its public import URI).  Because clearly, the call sheet is part
     * of the mapping.  For pipelines that's a bit different as the ports can be
     * access through the API of XPipeline, passed to the connectors.
     */
    @Override
    public void connectToXQueryFunction(XQueryEvaluator eval, ServerConfig config)
            throws ServlexException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectToQuery(XQueryEvaluator eval, ServerConfig config)
            throws ServlexException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToXSLTComponent(XsltTransformer trans, ServerConfig config)
            throws ServlexException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToStylesheet(XsltTransformer trans, ServerConfig config)
            throws ServlexException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToPipeline(XPipeline pipe, ServerConfig config)
            throws ServlexException
    {
        // the code-name
        String prefix = myError.getName().getPrefix();
        String local  = myError.getName().getLocalPart();
        String name = local;
        if ( prefix != null && ! prefix.equals("") ) {
            name = prefix + ":" + local;
        }
        // the code-namespace
        String ns     = myError.getName().getNamespaceURI();
        // the message
        String msg    = myError.getMsg();
        // set them as options
        pipe.passOption(CODE_NAME_ATTRIBUTE, new RuntimeValue(name));
        pipe.passOption(CODE_NAMESPACE_ATTRIBUTE, new RuntimeValue(ns));
        pipe.passOption(MESSAGE_ATTRIBUTE, new RuntimeValue(msg));
        // connect the web request to the source port
        final String src_port = XProcPipeline.INPUT_PORT_NAME;
        XdmNode web_request = myRequest.getWebRequest(config);
        CalabashHelper.writeTo(pipe, src_port, web_request, config);
        // connect the user sequence to the user-data port
        final String err_port = XProcPipeline.ERROR_PORT_NAME;
        XdmValue userdata = myError.getSequence();
        if ( userdata != null ) {
            CalabashHelper.writeTo(pipe, err_port, userdata, config);
        }
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToResponse(HttpServletResponse resp, ServerConfig config)
            throws ServlexException
                 , IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static final QName CODE_NAME_ATTRIBUTE      =
            new QName(ServlexConstants.WEBAPP_PREFIX, ServlexConstants.WEBAPP_NS, "code-name");
    public static final QName CODE_NAMESPACE_ATTRIBUTE =
            new QName(ServlexConstants.WEBAPP_PREFIX, ServlexConstants.WEBAPP_NS, "code-namespace");
    public static final QName MESSAGE_ATTRIBUTE        =
            new QName(ServlexConstants.WEBAPP_PREFIX, ServlexConstants.WEBAPP_NS, "message");

    private ComponentError   myError;
    private RequestConnector myRequest;
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
