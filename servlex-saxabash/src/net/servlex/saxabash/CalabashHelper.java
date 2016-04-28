/****************************************************************************/
/*  File:       CalabashHelper.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-04-25                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxabash;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.runtime.XPipeline;
import javax.xml.namespace.QName;
import net.servlex.saxon.Saxon;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.TreeBuilder;
import net.servlex.saxon.model.SaxonDocument;
import net.servlex.saxon.model.SaxonSequence;
import org.expath.servlex.runtime.ComponentError;
import org.expath.servlex.tools.Log;

/**
 * Helper methods for Calabash.
 *
 * @author Florent Georges
 */
public class CalabashHelper
{
    public static ComponentError makeError(XProcException ex)
            throws ServlexException
                 , TechnicalException
    {
        // the error name
        net.sf.saxon.s9api.QName code = ex.getErrorCode();
        QName name;
        if ( code == null ) {
            name = new QName(ServlexConstants.WEBAPP_NS, "ERRUNKNOWN", ServlexConstants.WEBAPP_PREFIX);
        }
        else {
            name = new QName(code.getNamespaceURI(), code.getLocalName(), code.getPrefix());
        }
        // the error message
        String msg = ex.getMessage();
        // the error object
        XdmValue sequence = ex.getNode();
        LOG.error("TODO: Cannot get the p:error input out of an XProcException");
        Sequence seq = sequence == null
                ? Saxon.makeEmptySequence()
                : new SaxonSequence(sequence);
        return new ComponentError(ex, name, msg, seq);
    }

    /**
     * Write a generic XDM sequence to an XProc port.
     * 
     * Each item is {@code sequence} is written, in that order, to the port
     * with name {@code port} on the pipeline {@code pipe}.  A node is written
     * directly, an item is written within a c:data element.
     */
    public static void writeTo(XPipeline pipe, String port, XdmValue sequence, Processors procs)
            throws TechnicalException
    {
        // TODO: Generate an error if not?
        if ( pipe.getInputs().contains(port) ) {
            // TODO: Can really sequence be null? (for now, this is because I don't
            // know how to get the user sequence from an XProcException, see the
            // method makeError() here above).
            if ( sequence == null ) {
                throw new TechnicalException("Not implemented yet, if the input is empty");
            }
            else {
                for ( XdmItem body : sequence ) {
                    // TODO: Is it enough to test whether this is a node?  Shouldn't
                    // I test if it is a document node?  Or at least an element?
                    // And what about the web:request? (the element is registered
                    // in the input sequence, not the document node...)
                    if ( body instanceof XdmNode ) {
                        pipe.writeTo(port, (XdmNode) body);
                    }
                    else {
                        String c_ns = "http://www.w3.org/ns/xproc-step";
                        TreeBuilder b = procs.makeTreeBuilder(c_ns, "c");
                        b.startElem("data");
                        b.attribute("encoding", "base64");
                        b.startContent();
                        b.characters(body.getStringValue());
                        b.endElem();
                        SaxonDocument doc = (SaxonDocument) b.getRoot();
                        pipe.writeTo(port, doc.getSaxonNode());
                    }
                }
            }
        }
    }

    /** The logger. */
    private static final Log LOG = new Log(CalabashHelper.class);
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
