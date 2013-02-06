/****************************************************************************/
/*  File:       Result.java                                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import org.apache.log4j.Logger;
import org.expath.servlex.tools.SaxonHelper;

/**
 * TODO: ...
 *
 * TODO: ServletException is obviously not an adapted exception type here...
 * Nor SaxonApiException (and maybe even IOException).
 *
 * TODO: Refactor this code and integrate it all to XdmConnector.  Then remove
 * this class.
 *
 * TODO: This class analyzes the sequence in the constructor and builds an
 * intermediary representation in memory, then write to the response object.
 * This two-phases process is not needed in the original design, where the
 * second always follows directly the first.  But in the context of connectors,
 * that can be interesting to decouple the mapping a the sequence to an
 * abstract Body or Multipart object, then rely on common code to actually push
 * it to the response object (even if a more streamable architecture could
 * probably be found).
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
@Deprecated
public class Result
{
    /**
     * TODO: ...
     * @param sequence
     * @throws ServletException
     */
    public Result(XdmValue sequence)
            throws ServlexException
    {
        myStatus = -1;
        myMsg = null;
        myHeaders = new ArrayList<Header>();
        // 1.) the web:response element
        // TODO: Make something more elaborate than that (like ignoring text
        // nodes if any, if they are withspace only, etc.)
        LOG.debug("Result: sequence size: " + sequence.size());
        if ( sequence.size() < 1 ) {
            error(500, "Empty sequence");
        }
        XdmItem item = sequence.itemAt(0);
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Result: sequence[1]: " + item);
        }
        // if an atomic value, error
        if ( item.isAtomicValue() ) {
            error(500, "first item must be a web:response element");
        }
        XdmNode resp = (XdmNode) item;
        // if a document, get its child instead
        if ( resp.getNodeKind() == XdmNodeKind.DOCUMENT ) {
            try {
                resp = SaxonHelper.getDocumentRootElement(resp);
            }
            catch ( TechnicalException ex ) {
                error(500, "first item is a document not with exactly one element child", ex);
            }
        }
        // if not an element, error
        if ( resp.getNodeKind() != XdmNodeKind.ELEMENT ) {
            error(500, "first item must be a web:response element (" + resp.getNodeKind() + ")");
        }
        // if not a web:response, error
        if ( ! resp.getNodeName().equals(RESP_NAME) ) {
            error(500, "first item must be a web:response element (" + resp.getNodeName() + ")");
        }
        // 2.) the response bodies
        XdmNode[] bodies = null;
        if ( sequence.size() > 1 ) {
            bodies = new XdmNode[sequence.size() - 1];
            for ( int i = 1; i < sequence.size(); ++i ) {
                XdmItem body = sequence.itemAt(i);
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug("Result: sequence[" + (i+1) + "]: " + body);
                }
                if ( body.isAtomicValue() ) {
                    // TODO: Really?
                    error(500, "response body must be a node");
                }
                bodies[i - 1] = (XdmNode) body;
            }
        }
        // 3.) decode the response and bodies
        decodeResponse(resp, bodies);
    }

    /**
     * TODO: ...
     * @param resp
     */
    public void respond(Processor proc, HttpServletResponse resp)
            throws ServlexException
                 , IOException
    {
        resp.setStatus(myStatus, myMsg);
        for ( Result.Header h : myHeaders ) {
            resp.addHeader(h.name, h.value);
        }
        if ( myMultipart != null ) {
            respondMultipart(proc, resp);
        }
        else if ( myBody == null ) {
            // nothing
        }
        else {
            respondBody(proc, resp);
        }
    }

    // =======================================================================
    //     Decode the response
    //     -------------------
    // -----------------------------------------------------------------------

    private void decodeResponse(XdmNode resp, XdmNode[] bodies)
            throws ServlexException
    {
        XdmSequenceIterator it = resp.axisIterator(Axis.ATTRIBUTE);
        while ( it.hasNext() ) {
            XdmNode attr = (XdmNode) it.next();
            QName name = attr.getNodeName();
            if ( name.equals(STATUS_NAME) ) {
                myStatus = Integer.valueOf(attr.getStringValue());
            }
            else if ( name.equals(MSG_NAME) ) {
                myMsg = attr.getStringValue();
            }
            else if ( name.getNamespaceURI().equals("") ) {
                error(500, "Unknown attribute on web:response: " + name);
            }
            else {
                LOG.debug("Ignore attribute in another namespace on web:response: " + name);
            }
        }
        // FIXME: Can have only ONE web:body (or web:multipart) at this level.
        // Enforce that!
        int body_count = 0;
        it = resp.axisIterator(Axis.CHILD);
        while ( it.hasNext() ) {
            XdmNode node = (XdmNode) it.next();
            if ( node.getNodeKind() == XdmNodeKind.TEXT ) {
                // TODO: Check it is only whitespaces...  If not, error!
            }
            else if ( node.getNodeKind() == XdmNodeKind.ELEMENT ) {
                QName name = node.getNodeName();
                if ( name.equals(HEADER_NAME) ) {
                    handleHeader(node);
                }
                else if ( name.equals(MULTI_NAME) ) {
                    handleMultipart(node, bodies);
                }
                else if ( name.equals(BODY_NAME) ) {
                    myBody = handleBody(node);
                    if ( myBody.value == null && myBody.src == null ) {
                        if ( bodies == null ) {
                            error(500, "No body at all");
                        }
                        if ( bodies.length < body_count ) {
                            error(500, "Not enough bodies: " + bodies.length + " - " + body_count);
                        }
                        myBody.value = bodies[body_count++];
                    }
                }
                else {
                    error(500, "Unknown web:response child: " + name);
                }
            }
        }
    }

    private void handleHeader(XdmNode node)
            throws ServlexException
    {
        Header h = new Header();
        XdmSequenceIterator it = node.axisIterator(Axis.ATTRIBUTE);
        while ( it.hasNext() ) {
            XdmNode attr = (XdmNode) it.next();
            QName name = attr.getNodeName();
            if ( name.equals(NAME_NAME) ) {
                h.name = attr.getStringValue();
            }
            else if ( name.equals(VALUE_NAME) ) {
                h.value = attr.getStringValue();
            }
            else {
                error(500, "Unknown attribute on web:header: " + name);
            }
        }
        myHeaders.add(h);
    }

    private void handleMultipart(XdmNode node, XdmNode[] bodies)
            throws ServlexException
    {
        myMultipart = new Multipart();
        XdmSequenceIterator it = node.axisIterator(Axis.ATTRIBUTE);
        while ( it.hasNext() ) {
            XdmNode attr = (XdmNode) it.next();
            QName name = attr.getNodeName();
            if ( name.equals(TYPE_NAME) ) {
                myMultipart.type = attr.getStringValue();
            }
            else if ( name.equals(BOUND_NAME) ) {
                myMultipart.boundary = attr.getStringValue();
            }
            else {
                error(500, "Unknown attribute on web:multipart: " + name);
            }
        }
        myMultipart.bodies = new ArrayList<Body>();
        int body_count = 0;
        it = node.axisIterator(Axis.CHILD);
        while ( it.hasNext() ) {
            XdmNode child = (XdmNode) it.next();
            if ( child.getNodeKind() == XdmNodeKind.TEXT ) {
                // TODO: Check it is only whitespaces...  If not, error!
            }
            else if ( child.getNodeKind() == XdmNodeKind.ELEMENT ) {
                QName name = child.getNodeName();
                // TODO: Support web:multipart/web:header elements !
                if ( name.equals(HEADER_NAME) ) {
                    error(501, "TODO: web:multipart/web:header not supported yet!");
                }
                else if ( name.equals(BODY_NAME) ) {
                    Body b = handleBody(child);
                    if ( b.value == null && b.src == null ) {
                        b.value = bodies[body_count++];
                    }
                    myMultipart.bodies.add(b);
                }
                else {
                    error(500, "Unknown web:multipart child: " + name);
                }
            }
        }
    }

    private Body handleBody(XdmNode node)
            throws ServlexException
    {
        Body b = new Body();
        b.base = node.getBaseURI();
        b.serializer = new Serializer();
        XdmSequenceIterator it = node.axisIterator(Axis.ATTRIBUTE);
        while ( it.hasNext() ) {
            XdmNode attr = (XdmNode) it.next();
            QName name = attr.getNodeName();
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("body attribute: " + name + " = " + attr.getStringValue());
            }
            if ( name.equals(TYPE_NAME) ) {
                b.serializer.setMediaType(attr.getStringValue());
            }
            else if ( name.equals(ID_NAME) ) {
                b.id = attr.getStringValue();
            }
            else if ( name.equals(DESC_NAME) ) {
                b.description = attr.getStringValue();
            }
            else if ( name.equals(SRC_NAME) ) {
                b.src = attr.getStringValue();
            }
            else if ( name.equals(METHOD_NAME) ) {
                b.serializer.setMethod(attr.getStringValue());
            }
            else if ( name.equals(ENC_NAME) ) {
                b.serializer.setEncoding(attr.getStringValue());
            }
            else if ( name.equals(BYTE_ORDER_NAME) ) {
                b.serializer.setByteOrderMark(attr.getStringValue());
            }
            else if ( name.equals(CDATA_ELEMENTS_NAME) ) {
                b.serializer.setCdataSectionElements(attr.getStringValue());
            }
            else if ( name.equals(PUBID_NAME) ) {
                b.serializer.setDoctypePublic(attr.getStringValue());
            }
            else if ( name.equals(SYSID_NAME) ) {
                b.serializer.setDoctypeSystem(attr.getStringValue());
            }
            else if ( name.equals(ESCAPE_URI_NAME) ) {
                b.serializer.setEscapeUriAttributes(attr.getStringValue());
            }
            else if ( name.equals(INCLUDE_CT_NAME) ) {
                b.serializer.setIncludeContentType(attr.getStringValue());
            }
            else if ( name.equals(INDENT_NAME) ) {
                b.serializer.setIndent(attr.getStringValue());
            }
            else if ( name.equals(NORM_FORM_NAME) ) {
                b.serializer.setNormalizationForm(attr.getStringValue());
            }
            else if ( name.equals(OMIT_XML_DECL_NAME) ) {
                b.serializer.setOmitXmlDeclaration(attr.getStringValue());
            }
            else if ( name.equals(STANDALONE_NAME) ) {
                b.serializer.setStandalone(attr.getStringValue());
            }
            else if ( name.equals(UNDECL_PREFIXES_NAME) ) {
                b.serializer.setUndeclarePrefixes(attr.getStringValue());
            }
            else if ( name.equals(USE_CHAR_MAPS_NAME) ) {
                b.serializer.setUseCharacterMaps(attr.getStringValue());
            }
            else if ( name.equals(VERSION_NAME) ) {
                b.serializer.setVersion(attr.getStringValue());
            }
//            else if ( name.equals(SAXON_CHARACTER_REPRESENTATION_NAME) ) {
//                b.serializer.setSaxonCharacterRepresentation(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_DOUBLE_SPACE_NAME) ) {
//                b.serializer.setSaxonDoubleSpace(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_IMPLICIT_RESULT_DOCUMENT_NAME) ) {
//                b.serializer.setSaxonImplicitResultDocument(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_INDENT_SPACES_NAME) ) {
//                b.serializer.setSaxonIndentSpaces(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_NEXT_IN_CHAIN_NAME) ) {
//                b.serializer.setSaxonNextInChain(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_NEXT_IN_CHAIN_BASE_URI_NAME) ) {
//                b.serializer.setSaxonNextInChainBaseUri(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_RECOGNIZE_BINARY_NAME) ) {
//                b.serializer.setSaxonRecognizeBinary(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_REQUIRE_WELL_FORMED_NAME) ) {
//                b.serializer.setSaxonRequireWellFormed(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_STYLESHEET_VERSION_NAME) ) {
//                b.serializer.setSaxonStylesheetVersion(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_SUPPLY_SOURCE_LOCATOR_NAME) ) {
//                b.serializer.setSaxonSupplySourceLocator(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_SUPPRESS_INDENTATION_NAME) ) {
//                b.serializer.setSaxonSuppressIndentation(attr.getStringValue());
//            }
//            else if ( name.equals(SAXON_WRAP_NAME) ) {
//                b.serializer.setSaxonWrap(attr.getStringValue());
//            }
            else if ( "xml".equals(name.getPrefix()) ) {
                // nothing (ignore standard XML attributes, like xml:base, xml:id...)
            }
            else {
                error(500, "Unknown attribute on web:body: " + name);
            }
        }
        XdmSequenceIterator seq = node.axisIterator(Axis.CHILD);
        if ( seq.hasNext() ) {
            List<XdmItem> nodes = new ArrayList<XdmItem>();
            while ( seq.hasNext() ) {
                XdmItem next = seq.next();
                nodes.add(next);
            }
            b.value = new XdmValue(nodes);
        }
        return b;
    }

    // =======================================================================
    //     Send the response
    //     -----------------
    // -----------------------------------------------------------------------

    private void respondBody(Processor proc, HttpServletResponse resp)
            throws ServlexException
                 , IOException
    {
        URI src = null;
        try {
            String type     = myBody.serializer.getMediaType();
            String encoding = myBody.serializer.getEncoding();
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("media type: " + type + ", encoding: " + encoding);
            }
            resp.setContentType(type);
            resp.setCharacterEncoding(encoding);
            if ( myBody.description != null ) {
                resp.addHeader("Content-Description", myBody.description);
            }
            if ( myBody.id != null ) {
                resp.addHeader("Content-ID", myBody.id);
            }
            if ( myBody.src != null ) {
                src = myBody.base.resolve(myBody.src);
                InputStream in = null;
                if ( "jar".equals(src.getScheme()) ) {
                    in = resolveJarUri(src);
                }
                else if("file".equals(src.getScheme())) {
                    in = new FileInputStream(new File(src));
                }
                else {
                    throw new ServlexException(500, "Unsupported URI scheme: " + src);
                }
                OutputStream out = resp.getOutputStream();
                byte[] buf = new byte[4096];
                int len;
                while ( (len = in.read(buf)) > 0 ) {
                    out.write(buf, 0, len);
                }
            }
            else {
                // TODO: FIXME: Take b.type into account... (different
                //   serialization methods...)
                // TODO: FIXME: Take b.encoding into account...!
                // TODO: Should actualy web:body be able to have same
                //   properties as xsl:output and xsl:result-document (aka
                //   serialization properties) ?
                myBody.serializer.serialize(myBody.value, proc, resp.getOutputStream());
            }
        }
        catch ( FileNotFoundException ex ) {
            LOG.error("Page not found - " + myBody.src + " - " + src);
            error(404, "Page not found", ex);
        }
        catch ( SaxonApiException ex ) {
            error(500, "Internal error", ex);
        }
    }

    /**
     * Resolve a URI of the form jar:file:/dir/file.jar!/some/resource.xml.
     *
     * The file part (before the '!') is ignored, and the resource part (after
     * the '!') is used as the name of a resource which must be within the
     * classpath itself.
     *
     * TODO: Handle specific cases, bang = -1, etc.
     */
    private static InputStream resolveJarUri(URI uri)
    {
        String path = uri.getSchemeSpecificPart();
        int bang = path.indexOf('!');
        String rsrc = path.substring(bang + 1);
        if ( rsrc.startsWith("/") ) {
            rsrc = rsrc.substring(1);
        }
        ClassLoader loader = Result.class.getClassLoader();
        return loader.getResourceAsStream(rsrc);
    }

    private void respondMultipart(Processor proc, HttpServletResponse resp)
            throws ServlexException
    {
        error(501, "TODO: Multipart response not implemented yet!");
    }

    // TODO: Error management!
    private void error(int code, String msg)
            throws ServlexException
    {
        LOG.error(msg);
        throw new ServlexException(code, msg);
    }

    private void error(int code, String msg, Throwable ex)
            throws ServlexException
    {
        LOG.error(msg, ex);
        throw new ServlexException(code, msg, ex);
    }

    // TODO: Move into a class "ServlexConstants" or something like that.
    // the webapp namespace URI
    private static final String WEB_NS   = "http://expath.org/ns/webapp";
//    private static final String SAXON_NS = "http://saxon.sf.net/";
    // element names
    private static final QName RESP_NAME   = new QName(WEB_NS, "response");
    private static final QName HEADER_NAME = new QName(WEB_NS, "header");
    private static final QName BODY_NAME   = new QName(WEB_NS, "body");
    private static final QName MULTI_NAME  = new QName(WEB_NS, "multipart");
    // attribute names
    private static final QName STATUS_NAME = new QName("status");
    private static final QName MSG_NAME    = new QName("message");
    private static final QName NAME_NAME   = new QName("name");
    private static final QName VALUE_NAME  = new QName("value");
    private static final QName TYPE_NAME   = new QName("content-type");
    private static final QName BOUND_NAME  = new QName("boundary");
    private static final QName ENC_NAME    = new QName("encoding");
    private static final QName ID_NAME     = new QName("id");
    private static final QName DESC_NAME   = new QName("description");
    private static final QName SRC_NAME    = new QName("src");
    private static final QName METHOD_NAME = new QName("method");
    private static final QName BYTE_ORDER_NAME = new QName("byte-order-mark");
    private static final QName CDATA_ELEMENTS_NAME = new QName("cdata-section-elements");
    private static final QName PUBID_NAME  = new QName("doctype-public");
    private static final QName SYSID_NAME  = new QName("doctype-system");
    private static final QName ESCAPE_URI_NAME = new QName("escape-uri-attributes");
    private static final QName INCLUDE_CT_NAME = new QName("include-content-type");
    private static final QName INDENT_NAME = new QName("indent");
    private static final QName NORM_FORM_NAME = new QName("normalization-form");
    private static final QName OMIT_XML_DECL_NAME = new QName("omit-xml-declaration");
    private static final QName STANDALONE_NAME = new QName("standalone");
    private static final QName UNDECL_PREFIXES_NAME = new QName("undeclare-prefixes");
    private static final QName USE_CHAR_MAPS_NAME = new QName("use-character-maps");
    private static final QName VERSION_NAME = new QName("version");
//    private static final QName SAXON_CHARACTER_REPRESENTATION_NAME = new QName(SAXON_NS, "saxon:character-representation");
//    private static final QName SAXON_DOUBLE_SPACE_NAME = new QName(SAXON_NS, "saxon:double-space");
//    private static final QName SAXON_IMPLICIT_RESULT_DOCUMENT_NAME = new QName(SAXON_NS, "saxon:implicit-result-document");
//    private static final QName SAXON_INDENT_SPACES_NAME = new QName(SAXON_NS, "saxon:indent-spaces");
//    private static final QName SAXON_NEXT_IN_CHAIN_NAME = new QName(SAXON_NS, "saxon:next-in-chain");
//    private static final QName SAXON_NEXT_IN_CHAIN_BASE_URI_NAME = new QName(SAXON_NS, "saxon:next-in-chain-base-uri");
//    private static final QName SAXON_RECOGNIZE_BINARY_NAME = new QName(SAXON_NS, "saxon:recognize-binary");
//    private static final QName SAXON_REQUIRE_WELL_FORMED_NAME = new QName(SAXON_NS, "saxon:require-well-formed");
//    private static final QName SAXON_STYLESHEET_VERSION_NAME = new QName(SAXON_NS, "saxon:stylesheet-version");
//    private static final QName SAXON_SUPPLY_SOURCE_LOCATOR_NAME = new QName(SAXON_NS, "saxon:supply-source-locator");
//    private static final QName SAXON_SUPPRESS_INDENTATION_NAME = new QName(SAXON_NS, "saxon:suppress-indentation");
//    private static final QName SAXON_WRAP_NAME = new QName(SAXON_NS, "saxon:wrap");

    private static final Logger LOG = Logger.getLogger(Result.class);

    private int myStatus;
    private String myMsg;
    private List<Header> myHeaders;
    // TODO: Make something more complex, with the attribute values from
    // web:body, etc. (hierarchy, with concrete class for multipart, html,
    // text, xml and binary...)
    private Body myBody;
    private Multipart myMultipart;

    // TODO: Rationalize those interfaces...  Public members, really?!?
    public static class Header
    {
        public String name;
        public String value;
    }

    public static class Body
    {
        public String     id;
        public String     description;
        public String     src;
        public URI        base;
        public XdmValue   value;
        public Serializer serializer;
    }

    public static class Multipart
    {
        public String     type;
        public String     boundary;
        public List<Body> bodies;
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
