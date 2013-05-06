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
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.expath.servlex.processors.Attribute;
import org.expath.servlex.processors.Element;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.Serializer;

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
    public Result(Sequence sequence, Processors procs)
            throws ServlexException
    {
        myProcs = procs;
        myStatus = -1;
        myMsg = null;
        myHeaders = new ArrayList<Header>();
        // the web:response element
        Element resp;
        try {
            resp = sequence.elementAt(0);
        }
        catch ( TechnicalException ex ) {
            error(500, "First item must be an element", ex);
            return; // error() always throws an exception
        }
        // if not a web:response, error
        if ( ! resp.name().equals(RESP_NAME) ) {
            error(500, "First item must be a web:response element (" + resp.name() + ")");
        }
        // the response bodies
        Sequence bodies = sequence.subSequence(1);
        // decode the response and bodies
        try {
            decodeResponse(resp, bodies);
        }
        catch ( TechnicalException ex ) {
            error(500, "Error decoding the result", ex);
        }
    }

    /**
     * TODO: ...
     * @param resp
     */
    public void respond(HttpServletResponse resp)
            throws ServlexException
                 , IOException
    {
        resp.setStatus(myStatus, myMsg);
        for ( Result.Header h : myHeaders ) {
            resp.addHeader(h.name, h.value);
        }
        if ( myMultipart != null ) {
            respondMultipart(resp);
        }
        else if ( myBody == null ) {
            // nothing
        }
        else {
            respondBody(resp);
        }
    }

    // =======================================================================
    //     Decode the response
    //     -------------------
    // -----------------------------------------------------------------------

    private void decodeResponse(Element resp, Sequence bodies)
            throws ServlexException
                 , TechnicalException
    {
        Iterator<Attribute> attributes = resp.attributes();
        while ( attributes.hasNext() ) {
            Attribute attr = attributes.next();
            QName name = attr.name();
            if ( name.equals(STATUS_NAME) ) {
                myStatus = Integer.valueOf(attr.value());
            }
            else if ( name.equals(MSG_NAME) ) {
                myMsg = attr.value();
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
        Iterator<Element> children = resp.elements();
        while ( children.hasNext() ) {
            Element child = children.next();
            QName name = child.name();
            if ( name.equals(HEADER_NAME) ) {
                handleHeader(child);
            }
            else if ( name.equals(MULTI_NAME) ) {
                handleMultipart(child, bodies);
            }
            else if ( name.equals(BODY_NAME) ) {
                myBody = handleBody(child);
                if ( myBody.value == null && myBody.src == null ) {
                    Item body = bodies.itemAt(body_count++);
                    myBody.value = body.asSequence();
                    if ( myBody.value == null ) {
                        error(500, "Not enough bodies: " + body_count);
                    }
                }
            }
            else {
                error(500, "Unknown web:response child: " + name);
            }
        }
    }

    private void handleHeader(Element header)
            throws ServlexException
                 , TechnicalException
    {
        Header h = new Header();
        Iterator<Attribute> attributes = header.attributes();
        while ( attributes.hasNext() ) {
            Attribute attr = attributes.next();
            QName name = attr.name();
            if ( name.equals(NAME_NAME) ) {
                h.name = attr.value();
            }
            else if ( name.equals(VALUE_NAME) ) {
                h.value = attr.value();
            }
            else {
                error(500, "Unknown attribute on web:header: " + name);
            }
        }
        myHeaders.add(h);
    }

    private void handleMultipart(Element multipart, Sequence bodies)
            throws ServlexException
                 , TechnicalException
    {
        myMultipart = new Multipart();
        Iterator<Attribute> attributes = multipart.attributes();
        while ( attributes.hasNext() ) {
            Attribute attr = attributes.next();
            QName name = attr.name();
            if ( name.equals(TYPE_NAME) ) {
                myMultipart.type = attr.value();
            }
            else if ( name.equals(BOUND_NAME) ) {
                myMultipart.boundary = attr.value();
            }
            else {
                error(500, "Unknown attribute on web:multipart: " + name);
            }
        }
        myMultipart.bodies = new ArrayList<Body>();
        int body_count = 0;
        Iterator<Element> children = multipart.elements();
        while ( children.hasNext() ) {
            Element child = children.next();
            QName name = child.name();
            // TODO: Support web:multipart/web:header elements !
            if ( name.equals(HEADER_NAME) ) {
                error(501, "TODO: web:multipart/web:header not supported yet!");
            }
            else if ( name.equals(BODY_NAME) ) {
                Body b = handleBody(child);
                if ( b.value == null && b.src == null ) {
                    Item body = bodies.itemAt(body_count++);
                    b.value = body.asSequence();
                }
                myMultipart.bodies.add(b);
            }
            else {
                error(500, "Unknown web:multipart child: " + name);
            }
        }
    }

    private Body handleBody(Element body)
            throws ServlexException
                 , TechnicalException
    {
        Body b = new Body();
        b.base = body.baseUri();
        try {
            b.serializer = myProcs.makeSerializer();
        }
        catch ( TechnicalException ex ) {
            error(500, "Error instantiating a serializer", ex);
        }
        Iterator<Attribute> attributes = body.attributes();
        while ( attributes.hasNext() ) {
            Attribute attr = attributes.next();
            QName name = attr.name();
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("body attribute: " + name + " = " + attr.value());
            }
            if ( name.equals(TYPE_NAME) ) {
                b.serializer.setMediaType(attr.value());
            }
            else if ( name.equals(ID_NAME) ) {
                b.id = attr.value();
            }
            else if ( name.equals(DESC_NAME) ) {
                b.description = attr.value();
            }
            else if ( name.equals(SRC_NAME) ) {
                b.src = attr.value();
            }
            else if ( name.equals(METHOD_NAME) ) {
                b.serializer.setMethod(attr.value());
            }
            else if ( name.equals(ENC_NAME) ) {
                b.serializer.setEncoding(attr.value());
            }
            else if ( name.equals(BYTE_ORDER_NAME) ) {
                b.serializer.setByteOrderMark(attr.value());
            }
            else if ( name.equals(CDATA_ELEMENTS_NAME) ) {
                b.serializer.setCdataSectionElements(attr.value());
            }
            else if ( name.equals(PUBID_NAME) ) {
                b.serializer.setDoctypePublic(attr.value());
            }
            else if ( name.equals(SYSID_NAME) ) {
                b.serializer.setDoctypeSystem(attr.value());
            }
            else if ( name.equals(ESCAPE_URI_NAME) ) {
                b.serializer.setEscapeUriAttributes(attr.value());
            }
            else if ( name.equals(INCLUDE_CT_NAME) ) {
                b.serializer.setIncludeContentType(attr.value());
            }
            else if ( name.equals(INDENT_NAME) ) {
                b.serializer.setIndent(attr.value());
            }
            else if ( name.equals(NORM_FORM_NAME) ) {
                b.serializer.setNormalizationForm(attr.value());
            }
            else if ( name.equals(OMIT_XML_DECL_NAME) ) {
                b.serializer.setOmitXmlDeclaration(attr.value());
            }
            else if ( name.equals(STANDALONE_NAME) ) {
                b.serializer.setStandalone(attr.value());
            }
            else if ( name.equals(UNDECL_PREFIXES_NAME) ) {
                b.serializer.setUndeclarePrefixes(attr.value());
            }
            else if ( name.equals(USE_CHAR_MAPS_NAME) ) {
                b.serializer.setUseCharacterMaps(attr.value());
            }
            else if ( name.equals(VERSION_NAME) ) {
                b.serializer.setVersion(attr.value());
            }
            else if ( "xml".equals(name.getPrefix()) ) {
                // nothing (ignore standard XML attributes, like xml:base, xml:id...)
            }
            else {
                error(500, "Unknown attribute on web:body: " + name);
            }
        }
        Iterator<Item> children = body.children();
        if ( children.hasNext() ) {
            List<Item> nodes = new ArrayList<Item>();
            while ( children.hasNext() ) {
                Item next = children.next();
                nodes.add(next);
            }
            b.value = myProcs.buildSequence(nodes);
        }
        return b;
    }

    // =======================================================================
    //     Send the response
    //     -----------------
    // -----------------------------------------------------------------------

    private void respondBody(HttpServletResponse resp)
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
                myBody.serializer.serialize(myBody.value, resp.getOutputStream());
            }
        }
        catch ( FileNotFoundException ex ) {
            LOG.error("Page not found - " + myBody.src + " - " + src);
            error(404, "Page not found", ex);
        }
        catch ( TechnicalException ex ) {
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

    private void respondMultipart(HttpServletResponse resp)
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

    private static final Logger LOG = Logger.getLogger(Result.class);

    private Processors myProcs;
    private int myStatus;
    private String myMsg;
    private List<Header> myHeaders;
    // TODO: Make something more complex, with the attribute values from
    // web:body, etc. (hierarchy, with concrete class for multipart, html,
    // text, xml and binary...)
    private Body myBody;
    private Multipart myMultipart;

    // TODO: Rationalize those interfaces...
    private static class Header
    {
        public String name;
        public String value;
    }

    private static class Body
    {
        public String     id;
        public String     description;
        public String     src;
        public URI        base;
        public Sequence   value;
        public Serializer serializer;
    }

    private static class Multipart
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
