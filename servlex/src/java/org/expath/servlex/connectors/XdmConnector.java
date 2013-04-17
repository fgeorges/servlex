/****************************************************************************/
/*  File:       XdmConnector.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import com.xmlcalabash.runtime.XPipeline;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;
import org.expath.servlex.Result;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.processors.XProcProcessor;
import org.expath.servlex.tools.CalabashHelper;

/**
 * Connector to an XDM sequence, represented by the Saxon {@link XdmValue}.
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
 * @date   2011-02-06
 */
public class XdmConnector
        implements Connector
{
    public XdmConnector(XdmValue sequence)
    {
        mySequence = sequence;
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToXQueryFunction(XQueryEvaluator eval, ServerConfig config)
            throws ServlexException
    {
        final QName param_name = new QName("input");
        eval.setExternalVariable(param_name, mySequence);
    }

    /**
     * TODO: No context node...?
     */
    @Override
    public void connectToQuery(XQueryEvaluator eval, ServerConfig config)
            throws ServlexException
    {
        final QName input_name = new QName(ServlexConstants.WEBAPP_NS, "input");
        eval.setExternalVariable(input_name, mySequence);
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToXSLTComponent(XsltTransformer trans, ServerConfig config)
            throws ServlexException
    {
        final QName param_name = new QName(ServlexConstants.PRIVATE_NS, "input");
        trans.setParameter(param_name, mySequence);
    }

    /**
     * Connect the sequence to $web:input and $web:input[1] as the input tree.
     * 
     * Connecting the first item in the input sequence will succeed only if it
     * is a document node or an element node (and if there is exactly one).
     * 
     * TODO: That imposes the user to declare the parameter in the stylesheet,
     * isn't it?  Even if there is only one node in the sequence (this is a
     * common case: we want to apply a stylesheet and we know we have a single
     * document, then the intuitive way is just to apply the stylesheet to the
     * node, no need to declare a parameter...)
     */
    @Override
    public void connectToStylesheet(XsltTransformer trans, ServerConfig config)
            throws ServlexException
    {
        if ( mySequence.size() == 0 ) {
            throw new ServlexException(500, "The input to the stylesheet is empty");
        }
        XdmItem item = mySequence.itemAt(0);
        if ( item.isAtomicValue() ) {
            String msg = "An atomic value cannot be set as the input to stylesheet: ";
            throw new ServlexException(500, msg + item);
        }
        XdmNode node = (XdmNode) item;
        XdmNodeKind kind = node.getNodeKind();
        if ( kind != XdmNodeKind.DOCUMENT && kind != XdmNodeKind.ELEMENT ) {
            String msg = "The input to stylesheet is neither a document nor an element node: ";
            throw new ServlexException(500, msg + kind);
        }
        // TODO: Is it possible to set it only if it is declared?  Is this
        // actually an error if it is not declared?
        final QName param_name = new QName(ServlexConstants.WEBAPP_NS, "input");
        trans.setInitialContextNode(node);
        trans.setParameter(param_name, mySequence);
    }

    /**
     * TODO: Mapping to define, then implement.
     */
    @Override
    public void connectToPipeline(XPipeline pipeline, ServerConfig config)
            throws ServlexException
    {
        CalabashHelper.writeTo(pipeline, XProcProcessor.INPUT_PORT_NAME, mySequence, config);
    }

    /**
     * TODO: ...
     */
    @Override
    public void connectToResponse(HttpServletResponse resp, ServerConfig config)
            throws ServlexException
                 , IOException
    {
        // TODO: FIXME: The artificial, old class Result should be removed, and
        // its content moved to this class, which is really the one responsible
        // to write an XDM sequence to the HTTP servlet response object.
        Result result = new Result(mySequence, config.getProcessors());
        result.respond(resp);
    }

    private XdmValue mySequence;
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
