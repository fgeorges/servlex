/****************************************************************************/
/*  File:       SaxonHelper.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-12-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.saxon.ConfigHelper;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.WebappFunctions;
import org.expath.servlex.runtime.ComponentError;

/**
 * Helper methods for Saxon.
 *
 * @author Florent Georges
 * @date   2010-12-21
 */
public class SaxonHelper
{
    public static Processor makeSaxon(SaxonRepository repo)
            throws PackageException
    {
        Processor saxon = new Processor(true);
        ConfigHelper helper = new ConfigHelper(repo);
        helper.config(saxon.getUnderlyingConfiguration());
        WebappFunctions.setup(saxon);
        return saxon;
    }

    /**
     * Return the root element of the document node passed in param.
     *
     * Throw an error is the param is null, is not a document node, or if it
     * not exactly one child which is an element node.
     */
    public static XdmNode getDocumentRootElement(XdmNode doc)
            throws TechnicalException
    {
        if ( doc == null ) {
            throw new TechnicalException("doc is null");
        }
        if ( doc.getNodeKind() != XdmNodeKind.DOCUMENT ) {
            throw new TechnicalException("doc is not a document node: " + doc.getNodeKind());
        }
        XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
        XdmNode root = ignoreWhitespaceTextNodes(it);
        if ( root == null ) {
            throw new TechnicalException("doc has no child (except whitespace-only text nodes)");
        }
        XdmNode second = ignoreWhitespaceTextNodes(it);
        if ( second != null ) {
            String name_1 = root.getNodeName().getClarkName();
            String name_2 = second.getNodeName().getClarkName();
            String msg    = "doc has several children: " + name_1 + ", " + name_2;
            throw new TechnicalException(msg);
        }
        return root;
    }

    /**
     * Return next node, ignoring all whitespace-only text nodes.
     * 
     * Return null if there is no such next node.
     */
    public static XdmNode ignoreWhitespaceTextNodes(XdmSequenceIterator it)
    {
        final Pattern pattern = Pattern.compile("\\s+");
        for ( ; /* ever */ ; ) {
            if ( ! it.hasNext() ) {
                return null;
            }
            XdmNode node = (XdmNode) it.next();
            if ( node.getNodeKind() != XdmNodeKind.TEXT ) {
                return node;
            }
            String value = node.getStringValue();
            if ( ! pattern.matcher(value).matches() ) {
                return node;
            }
        }
    }

    public static ComponentError makeError(SaxonApiException ex)
            throws ServlexException
    {
        if ( ! (ex.getCause() instanceof XPathException) ) {
            throw new ServlexException(500, "Internal error", ex);
        }
        XPathException cause = (XPathException) ex.getCause();
        QName name = cause.getErrorCodeQName().toJaxpQName();
        String msg = cause.getMessage();
        XdmValue sequence = MyValue.wrap(cause.getErrorObject());
        return new ComponentError(cause, name, msg, sequence);
    }

    /**
     * This class is a trick to make the protected wrap() available.
     */
    private static class MyValue
            extends XdmValue
    {
        public static XdmValue wrap(ValueRepresentation v)
        {
            return XdmValue.wrap(v);
        }
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
