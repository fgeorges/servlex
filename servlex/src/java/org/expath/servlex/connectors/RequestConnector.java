/****************************************************************************/
/*  File:       RequestConnector.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.connectors;

import com.xmlcalabash.runtime.XPipeline;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.field.AbstractField;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeTokenStream;
import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.model.Servlet;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.components.XProcPipeline;
import org.expath.servlex.tools.CalabashHelper;
import org.expath.servlex.tools.ContentType;
import org.expath.servlex.tools.SaxonHelper;
import org.expath.servlex.tools.TraceInputStream;
import org.expath.servlex.tools.TreeBuilderHelper;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Connector to the HTTP servlet request object.
 *
 * @author Florent Georges
 * @date   2011-02-06
 */
public class RequestConnector
        implements Connector
{
    public RequestConnector(HttpServletRequest request, String path, String appname)
    {
        myRequest = request;
        myPath = path;
        myAppName = appname;
    }

    public void setMatcher(Matcher matcher)
    {
        myMatcher = matcher;
    }

    public void setServlet(Servlet servlet)
    {
        myServlet = servlet;
    }

    public XdmNode getWebRequest(ServerConfig config)
            throws ServlexException
    {
        ensureParsing(config);
        return myWebRequest;
    }

    private synchronized void ensureParsing(ServerConfig config)
            throws ServlexException
    {
        if ( myInput == null ) {
            try {
                // where to put the web:request element
                TreeBuilderHelper builder = new TreeBuilderHelper(config.getSaxon(), NS_URI, NS_PREFIX);
                // parse the request (to web:request + sequence of bodies)
                // (parseRequest() puts everything in the list, and returns the
                // web:request document node)
                List<XdmItem> input = new ArrayList<XdmItem>();
                myWebRequest = parseRequest(config, builder, input);
                myInput = new XdmValue(input);
            }
            catch ( XPathException ex ) {
                error(500, "Internal error", ex);
            }
            catch ( TechnicalException ex ) {
                error(500, "Internal error", ex);
            }
        }
    }

    @Override
    public void connectToXQueryFunction(XQueryEvaluator eval, ServerConfig config)
            throws ServlexException
    {
        ensureParsing(config);
        eval.setExternalVariable(new QName("input"), myInput);
    }

    public void connectToQuery(XQueryEvaluator eval, ServerConfig config)
            throws ServlexException
    {
        ensureParsing(config);
        final QName input_name = new QName(ServlexConstants.WEBAPP_NS, "input");
        eval.setContextItem(myWebRequest);
        eval.setExternalVariable(input_name, myInput);
    }

    @Override
    public void connectToXSLTComponent(XsltTransformer trans, ServerConfig config)
            throws ServlexException
    {
        ensureParsing(config);
        final QName input_name = new QName(ServlexConstants.PRIVATE_NS, "input");
        trans.setParameter(input_name, myInput);
    }

    @Override
    public void connectToStylesheet(XsltTransformer trans, ServerConfig config)
            throws ServlexException
    {
        ensureParsing(config);
        // TODO: Is it possible to set it only if it is declared?  Is this
        // actually an error if it is not declared?
        final QName bod_name = new QName(ServlexConstants.WEBAPP_NS, "input");
        trans.setInitialContextNode(myWebRequest);
        trans.setParameter(bod_name, myInput);
    }

    @Override
    public void connectToPipeline(XPipeline pipe, ServerConfig config)
            throws ServlexException
    {
        ensureParsing(config);
        final String port = XProcPipeline.INPUT_PORT_NAME;
        CalabashHelper.writeTo(pipe, port, myInput, config);
    }

    /**
     * Throws an error, as a request cannot be connected directly to the response.
     */
    @Override
    public void connectToResponse(HttpServletResponse resp, ServerConfig config)
            throws ServlexException
                 , IOException
    {
        throw new ServlexException(500, "A request cannot be connected to the response.");
    }

    /**
     * Parse the request, to produce the web:request element, and the bodies.
     *
     * The web:request element is written to the tree builder helper object. The
     * bodies are returned as an {@link XdmValue}, that is, a sequence of items
     * (each being either a document node, a string or a base64 binary item,
     * depending on the type of the body part, see the Webapp spec).
     *
     * As a convenient reminder, the web:request element looks like the
     * following (but please see the Webapp spec for complete ref):
     *
     * <pre>
     * &lt;request servlet="name" path="/some/page" method="post">
     *    &lt;uri>http://localhost:8090/servlex/my-webapp/catalog/yo&lt;/uri>
     *    &lt;context-root>/servlex/my-webapp&lt;/context-root>
     *    &lt;path>
     *       &lt;part>/catalog/&lt;/part>
     *       &lt;match name="something">yo&lt;/match>
     *    &lt;/path>
     *    &lt;param name="..." value="..."/> &lt;!-- either uri query or posted form data -->
     *    &lt;header name="..." value="..."/>
     *    &lt;multipart ...>
     *       &lt;header name="..." value="..."/>
     *       &lt;body .../>
     *    &lt;/multipart ...>
     * &lt;/request>
     * </pre>
     *
     * TODO: The web:multipart and web:body elements should cary more info, as
     * attributes.  For instance the Content-* headers.  For now they do not
     * have any attribute.
     * 
     * TODO: Document the input param (list to accumulate the request element
     * and the bodies, which will end up as the $input sequence of most of the
     * components)
     */
    private XdmNode parseRequest(ServerConfig config, TreeBuilderHelper b, List<XdmItem> input)
            throws XPathException
                 , ServlexException
                 , TechnicalException
    {
        // some values
        String servlet   = myServlet == null ? null : myServlet.getName();
        String path      = myPath;
        String method    = myRequest.getMethod();
        String uri       = getRequestUri();
        String authority = getAuthority(uri);
        String ctxt_root =
                myRequest.getContextPath()
                + myRequest.getServletPath()
                + "/"
                + myAppName;
        // log them?
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Request - servlet  : " + servlet);
            LOG.debug("Request - path     : " + path);
            LOG.debug("Request - method   : " + method);
            LOG.debug("Request - uri      : " + uri);
            LOG.debug("Request - authority: " + authority);
            LOG.debug("Request - ctxt_root: " + ctxt_root);
        }
        // use them in web:request
        b.startElem("request");
        if ( servlet != null ) {
            b.attribute("servlet", servlet);
        }
        b.attribute("path", path);
        b.attribute("method", method.toLowerCase());
        b.startContent();
        b.textElem("uri", uri);
        b.textElem("authority", authority);
        b.textElem("context-root", ctxt_root);
        // the path
        makeElementPath(b);
        // the parameters
        makeElementsParam(b);
        // the headers
        makeElementsHeader(b);
        // add null as the first item, as a placeholder for the request element
        // once it has been built
        input.add(null);
        // parse the bodies
        makeBodies(config, b, input);
        // end the request element
        b.endElem();
        // return the request document node
        XdmNode doc = b.getRoot();
        XdmNode elem = SaxonHelper.getDocumentRootElement(doc);
        input.set(0, elem);
        return doc;
    }

    /**
     * Return the request URI as a string.
     */
    private String getRequestUri()
    {
        StringBuffer uribuf = myRequest.getRequestURL();
        if ( myRequest.getQueryString() != null ) {
            uribuf.append('?');
            uribuf.append(myRequest.getQueryString());
        }
        return uribuf.toString();
    }

    /**
     * Return the request URI as a string.
     */
    private String getAuthority(String uri)
    {
        int slash = uri.indexOf('/');
        if ( slash >= 0 ) {
            slash = uri.indexOf('/', slash + 1); // second slash
        }
        if ( slash >= 0 ) {
            slash = uri.indexOf('/', slash + 1); // third slash
        }
        if ( slash >= 0 ) {
            return uri.substring(0, slash);
        }
        else {
            return uri;
        }
    }

    /**
     * Make the element web:path within the web:request, and put it in {@code b}.
     */
    private void makeElementPath(TreeBuilderHelper b)
            throws XPathException
    {
        b.startElem("path");
        b.startContent();
        if ( myMatcher == null || myServlet == null ) { // -> welcome file or resource
            b.textElem("part", myPath);
        }
        else {
            int last_index = 0;
            String[] groups = myServlet.getGroupNames();
            for ( int i = 1; i <= myMatcher.groupCount(); ++i ) {
                if ( last_index < myMatcher.start(i) ) {
                    b.textElem("part", myPath.substring(last_index, myMatcher.start(i)));
                }
                if ( myMatcher.group(i) != null ) {
                    b.startElem("match");
                    if ( i <= groups.length && groups[i-1] != null ) {
                        b.attribute("name", groups[i-1]);
                    }
                    b.startContent();
                    b.characters(myMatcher.group(i));
                    b.endElem();
                    last_index = myMatcher.end(i);
                }
            }
            if ( last_index < myPath.length() ) {
                b.textElem("part", myPath.substring(last_index));
            }
        }
        b.endElem();
    }

    /**
     * Make the elements web:param within the web:request, and put them in {@code b}.
     */
    private void makeElementsParam(TreeBuilderHelper b)
            throws XPathException
    {
        for ( Enumeration<String> e = myRequest.getParameterNames(); e.hasMoreElements(); /* */ ) {
            String name = e.nextElement();
            for ( String value : myRequest.getParameterValues(name) ) {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug("Request - param    : " + name + " / " + value);
                }
                b.startElem("param");
                b.attribute("name", name);
                b.attribute("value", value);
                b.endElem();
            }
        }
    }

    /**
     * Make the elements web:header within the web:request, and put them in {@code b}.
     */
    private void makeElementsHeader(TreeBuilderHelper b)
            throws XPathException
    {
        for ( Enumeration<String> e = myRequest.getHeaderNames(); e.hasMoreElements(); /* */ ) {
            String name = e.nextElement();
            for ( Enumeration<String> e2 = myRequest.getHeaders(name); e2.hasMoreElements(); /* */ ) {
                String value = e2.nextElement();
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug("Request - header   : " + name + " / " + value);
                }
                b.startElem("header");
                b.attribute("name", name);
                b.attribute("value", value);
                b.endElem();
            }
        }
    }

    /**
     * Return the list of bodies.
     *
     * TODO: Handle form fields separately?  Have a look at how they are
     * handled in Java Servlets...
     *
     * TODO: Must add more info on web:multipart and web:body elements.
     */
    private void makeBodies(ServerConfig config, TreeBuilderHelper builder, List<XdmItem> input)
            throws ServlexException
                 , XPathException
                 , TechnicalException
    {
        // the content type
        String ctype_raw = myRequest.getContentType();
        if ( ctype_raw == null ) {
            // if the content type is null, we assume there is no content
            return;
        }
        ContentType ctype = new ContentType(ctype_raw);
        try {
            // the input stream
            ServletInputStream in = myRequest.getInputStream();
            if ( LOG.isTraceEnabled() && config.isTraceContentEnabled() ) {
                in = new TraceInputStream(in);
            }
            // either multipart or single part
            if( ctype.isMultipart() ) {
                builder.startElem("multipart");
                builder.startContent();
                MimeTokenStream parser = new MimeTokenStream();
                parser.parseHeadless(in, ctype_raw);
                int position = 1;
                for ( int state = parser.getState();
                      state != MimeTokenStream.T_END_OF_STREAM;
                      state = parser.next() )
                {
                    handleParserState(parser, builder, input, position, config);
                    if ( parser.getState() == MimeTokenStream.T_BODY ) {
                        ++position;
                    }
                }
                builder.endElem();
            }
            else {
                XdmItem parsed = parseBody(config, in, ctype, 1, builder);
                input.add(parsed);
            }
        }
        catch ( MimeException ex ) {
            error(400, "Bad request", ex);
        }
        catch ( IOException ex ) {
            error(500, "Internal error", ex);
        }
    }

    /**
     * Do the job for one parser event, in case of a multipart.
     */
    private void handleParserState(MimeTokenStream parser, TreeBuilderHelper builder, List<XdmItem> items, int position, ServerConfig config)
            throws ServlexException
                 , XPathException
                 , MimeException
                 , TechnicalException
    {
        int state = parser.getState();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("MIME parser state: " + MimeTokenStream.stateToString(state));
        }
        switch ( state ) {
            // It seems that in a headless parsing, END_HEADER appears
            // right after START_MESSAGE (without the corresponding
            // START_HEADER).  So if headers == null, we can just ignore
            // this state.
            case MimeTokenStream.T_END_HEADER: {
                // TODO: Just ignore anyway...?
                break;
            }
            case MimeTokenStream.T_FIELD: {
                Field f = parser.getField();
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug("  field: " + f);
                }
                String body  = Integer.toString(position);
                String name  = f.getName().toLowerCase();
                String value = AbstractField.parse(f.getRaw()).getBody();
                builder.startElem("header");
                builder.attribute("body", body);
                builder.attribute("name", name);
                builder.attribute("value", value);
                builder.startContent();
                builder.endElem();
                break;
            }
            case MimeTokenStream.T_BODY: {
                // TOD: Do I really need to maintain the subtype value, or has the
                // body descriptor get it?
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug("  body desc: " + parser.getBodyDescriptor());
                }
                String ctype_raw = parser.getBodyDescriptor().getMimeType();
                if ( ctype_raw == null ) {
                    LOG.error("Content type of a subpart is null, at position " + position);
                    // is it really a bad request if a subpart hasn't a content type?
                    error(400, "Bad request");
                }
                ContentType ctype = new ContentType(ctype_raw);
                // TODO: Use getReader() instead of getInputStream() when possible
                // (that is, always except for binary content).  That needs some
                // refactoring wrt how input are passed to parseBody().
                InputStream in = parser.getInputStream();
                XdmItem part = parseBody(config, in, ctype, position, builder);
                items.add(part);
                break;
            }
            // START_HEADER is handled in the calling analyzeParts()
            case MimeTokenStream.T_START_HEADER:
            case MimeTokenStream.T_END_BODYPART:
            case MimeTokenStream.T_END_MESSAGE:
            case MimeTokenStream.T_END_MULTIPART:
            case MimeTokenStream.T_EPILOGUE:
            case MimeTokenStream.T_PREAMBLE:
            case MimeTokenStream.T_START_BODYPART:
            case MimeTokenStream.T_START_MESSAGE:
            case MimeTokenStream.T_START_MULTIPART: {
                // ignore
                break;
            }
            // In a first time, take a very defensive approach, and
            // throw an error for all unexpected states, even if we
            // should discover slowly that we should probably just
            // ignore some of them.
            default: {
                String s = MimeTokenStream.stateToString(state);
                LOG.error("Unknown parsing state: " + s);
                error(500, "Internal error");
            }
        }
    }

    /**
     * ...
     *
     * TODO: FIXME: This is the usual parsing depending on the MIME type of the
     * content: return either a base64 item, an xs:string or an XML document
     * (maybe tidied up from an HTML doc).  This is duplicated in several places,
     * like in the HTTP Client implementation for Saxon.  We should provide a
     * common helper for that (even maybe if the class itself is duplicated to
     * minimize dependencies, but at least that will be easier to reuse and
     * maintain than being implemented several times independently...)
     *
     * TODO: Ensure we use the correct encoding when reading parts...
     */
    private XdmItem parseBody(ServerConfig config, InputStream input, ContentType ctype, int position, TreeBuilderHelper builder)
            throws ServlexException
                 , XPathException
    {
        try {
            // TODO: Add more information on the web:body element (@content-type,
            // etc., see the HTTP Client module and the XProc p:http-request).
            builder.startElem("body");
            builder.attribute("content-type", ctype.getMainType() + "/" + ctype.getSubType());
            builder.attribute("position", Integer.toString(position));
            builder.startContent();
            builder.endElem();
            switch ( ctype.getMediaType() ) {
                case HTML: {
                    // TODO: Pass the charset as well, if it is set explicitly
                    return parseBodyXml(config, input, true);
                }
                case XML: {
                    // TODO: Pass the charset as well, if it is set explicitly
                    return parseBodyXml(config, input, false);
                }
                case TEXT: {
                    String charset = ctype.getCharset();
                    if ( charset == null ) {
                        // use UTF-8 by default...
                        charset = "utf-8";
                    }
                    return parseBodyText(config, input, charset);
                }
                case BINARY: {
                    return parseBodyBinary(input);
                }
            }
        }
        catch ( SaxonApiException ex ) {
            error(500, "Internal error", ex);
        }
        catch ( SAXException ex ) {
            error(500, "Internal error", ex);
        }
        catch ( IOException ex ) {
            error(500, "Internal error", ex);
        }
        // cannot happen, because error() always throws an exception
        throw new ServlexException(500, "Cannot happen (parseBody)");
    }

    /**
     * Parse content as XML (tidied up from HTML if {@code html} is true).
     */
    private XdmNode parseBodyXml(ServerConfig config, InputStream input, boolean html)
            throws SaxonApiException
                 , SAXException
    {
        String sys_id = "TODO-find-a-useful-systemId";
        Source src;
        if ( html ) {
            Parser parser = new Parser();
            parser.setFeature(Parser.namespacesFeature, true);
            parser.setFeature(Parser.namespacePrefixesFeature, true);
            InputSource source = new InputSource(input);
            src = new SAXSource(parser, source);
            src.setSystemId(sys_id);
        }
        else {
            src = new StreamSource(input, sys_id);
        }
        Processor saxon = config.getSaxon();
        DocumentBuilder builder = saxon.newDocumentBuilder();
        XdmNode doc = builder.build(src);
        if ( LOG.isTraceEnabled() && config.isTraceContentEnabled() ) {
            LOG.trace("Content parsed as document node: " + doc);
        }
        return doc;
    }

    /**
     * Parse content as text.
     */
    private XdmAtomicValue parseBodyText(ServerConfig config, InputStream input, String charset)
            throws IOException
    {
        // BufferedReader handles the ends of line (all \n, \r, and \r\n are
        // treated as end-of-line)
        StringBuilder builder = new StringBuilder();
        Reader reader = new InputStreamReader(input, charset);
        BufferedReader buf_in = new BufferedReader(reader);
        String buf;
        while ( (buf = buf_in.readLine()) != null ) {
            builder.append(buf);
            builder.append('\n');
        }
        String str = builder.toString();
        if ( LOG.isTraceEnabled() && config.isTraceContentEnabled() ) {
            LOG.trace("Content parsed as text: " + str);
        }
        return new XdmAtomicValue(str);
    }

    /**
     * Parse content as binary.
     */
    private XdmItem parseBodyBinary(InputStream input)
            throws IOException
                 , SaxonApiException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ( (read = input.read(buf)) > 0 ) {
            out.write(buf, 0, read);
        }
        byte[] bytes = out.toByteArray();
        return TodoBinaryItem.makeBinaryItem(bytes);
    }

    /**
     * TODO: Work around, see http://saxon.markmail.org/thread/sufwctvikfphdh2m
     */
    private static class TodoBinaryItem
            extends XdmItem
    {
        public static XdmItem makeBinaryItem(byte[] bytes)
        {
            Item value = new Base64BinaryValue(bytes);
            return wrapItem(value);
        }
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
        LOG.error(code + ": " + msg, ex);
        throw new ServlexException(code, msg, ex);
    }

    /** The EXPath servlet namespace URI. */
    private static final String NS_URI    = "http://expath.org/ns/webapp";
    /** The usual EXPath servlet namespace prefix. */
    private static final String NS_PREFIX = "web";
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(RequestConnector.class);

    /** The Java EE HTTP request object. */
    private final HttpServletRequest myRequest;
    /** The path for this request. */
    private final String myPath;
    /** The name of the webapp (used in the URL). */
    private final String myAppName;
    /** The servlet to serve this request. */
    private Servlet myServlet = null;
    /** The regex matcher to get the groups out of the URI. */
    private Matcher myMatcher = null;
    /** The all input sequence, that is, the request element followed by bodies. */
    private XdmValue myInput = null;
    /** The web:request document node, null at beginning, placed here when parsed. */
    private XdmNode myWebRequest = null;
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
