/****************************************************************************/
/*  File:       BodyParser.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-13                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.ccil.cowan.tagsoup.Parser;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import static org.expath.servlex.tools.ContentType.MediaType.BINARY;
import static org.expath.servlex.tools.ContentType.MediaType.HTML;
import static org.expath.servlex.tools.ContentType.MediaType.TEXT;
import static org.expath.servlex.tools.ContentType.MediaType.XML;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parse bodies from HTTP requests or from files (i.e. from input streams).
 *
 * @author Florent Georges
 */
public class BodyParser
{
    /**
     * Create a new body parser.
     * 
     * @param trace_content If true, then the content itself is logged (using
     * the trace level, so trace has to be enabled as well).  This can result
     * in huge, fast-growing log files!
     */
    public BodyParser(boolean trace_content, Processors procs)
    {
        myTrace = trace_content;
        myProcs = procs;
    }

    public Item parse(InputStream input, ContentType ctype)
            throws TechnicalException
    {
        try {
            switch ( ctype.getMediaType() ) {
                case HTML: {
                    // TODO: Pass the charset as well, if it is set explicitly
                    return parseBodyXml(input, true);
                }
                case XML: {
                    // TODO: Pass the charset as well, if it is set explicitly
                    return parseBodyXml(input, false);
                }
                case TEXT: {
                    String charset = ctype.getCharset();
                    if ( charset == null ) {
                        // use UTF-8 by default...
                        charset = "utf-8";
                    }
                    return parseBodyText(input, charset);
                }
                case BINARY: {
                    return parseBodyBinary(input);
                }
            }
        }
        catch ( SAXException ex ) {
            throw new TechnicalException("Internal error", ex);
        }
        catch ( IOException ex ) {
            throw new TechnicalException("Internal error", ex);
        }
        // cannot happen, just to make Java compiler happy
        throw new TechnicalException("Cannot happen!?!");
    }

    /**
     * Parse content as XML (tidied up from HTML if {@code html} is true).
     */
    private Document parseBodyXml(InputStream input, boolean html)
            throws TechnicalException
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
        Document doc = myProcs.buildDocument(src);
        if ( myTrace && LOG.trace()) {
            LOG.trace("Content parsed as document node: " + doc);
        }
        return doc;
    }

    /**
     * Parse content as text.
     */
    private Item parseBodyText(InputStream input, String charset)
            throws IOException
                 , TechnicalException
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
        if ( myTrace && LOG.trace()) {
            LOG.trace("Content parsed as text: " + str);
        }
        return myProcs.buildString(str);
    }

    /**
     * Parse content as binary.
     */
    private Item parseBodyBinary(InputStream input)
            throws IOException
                 , TechnicalException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ( (read = input.read(buf)) > 0 ) {
            out.write(buf, 0, read);
        }
        byte[] bytes = out.toByteArray();
        return myProcs.buildBinary(bytes);
    }

    /** The logger. */
    private static final Log LOG = new Log(RequestConnector.class);

    /** Log content? */
    private boolean myTrace;
    /** Processors to use to build the data model objects. */
    private Processors myProcs;
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
