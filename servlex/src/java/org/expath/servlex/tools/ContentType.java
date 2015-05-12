/****************************************************************************/
/*  File:       ContentType.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-03-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import javax.servlet.ServletRequest;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.connectors.RequestConnector;

/**
 * Represent a HTTP content-type header.
 *
 * @author Florent Georges
 */
public class ContentType
{
    public enum MediaType
    {
        BINARY,
        HTML,
        TEXT,
        XML
    }

    /**
     * Parse the Content-Type header value.
     * 
     * @throws TechnicalException If the format of the value is not correct.
     */
    public ContentType(String value)
            throws TechnicalException
    {
        init(value);
    }

    /**
     * Parse the Content-Type header value of the request.
     * 
     * @throws TechnicalException If the format of the value is not correct.
     */
    public ContentType(ServletRequest request)
            throws TechnicalException
    {
        this(request.getContentType());
    }

    /**
     * Return the original content-type value.
     */
    public String getOriginal()
    {
        return myOriginal;
    }

    /**
     * Return the main type.
     */
    public String getMainType()
    {
        return myMainType;
    }

    /**
     * Return the sub type.
     */
    public String getSubType()
    {
        return mySubType;
    }

    /**
     * Return the charset.  Might be null if not explicitly set.
     */
    public String getCharset()
    {
        return myCharset;
    }

    /**
     * Return true if the main type is "multipart".
     */
    public boolean isMultipart()
    {
        return myMainType.equalsIgnoreCase("multipart");
    }

    /**
     * Return true if the main type is {@code main} and the sub type is {@code sub}.
     * 
     * TODO: Ignore case?
     */
    public boolean isType(String main, String sub)
    {
        return main.equals(myMainType) && sub.equals(mySubType);
    }

    /**
     * Return the equivalent media type (either binary, html, text or xml).
     */
    public MediaType getMediaType()
    {
        if ( isType("text", "html") ) {
            return MediaType.HTML;
        }
        else if ( isType("text", "xml")
                || isType("application", "xml")
                || isType("text", "xml-external-parsed-entity")
                || isType("application", "xml-external-parsed-entity")
                || mySubType.endsWith("+xml") ) {
            return MediaType.XML;
        }
        else if ( "text".equals(myMainType)
                || isType("application", "xml-dtd") ) {
            return MediaType.TEXT;
        }
        else {
            return MediaType.BINARY;
        }
    }

    private void init(String value)
            throws TechnicalException
    {
        LOG.debug("Content type - original : " + value);
        myOriginal = value;
        HeaderValueParser parser = new BasicHeaderValueParser();
        HeaderElement[] elems = BasicHeaderValueParser.parseElements(value, parser);
        if ( elems.length != 1 ) {
            String msg = "Content-Type does not have exactly one element, it has ";
            throw new TechnicalException(msg + elems.length + " (" + value + ")");
        }
        String type = elems[0].getName();
        int slash = type.indexOf('/');
        if ( slash <= 0 ) {
            String msg = "Content-Type does not contain any slash character (" + value + ")";
            throw new TechnicalException(msg);
        }
        myMainType = type.substring(0, slash);
        mySubType = type.substring(slash + 1);
        NameValuePair param = elems[0].getParameterByName("charset");
        if ( param != null ) {
            myCharset = param.getValue();
        }
        LOG.debug("Content type - main type: " + myMainType);
        LOG.debug("Content type - sub type : " + mySubType);
        LOG.debug("Content type - charset  : " + myCharset);
    }

    /** The logger. */
    private static final Log LOG = new Log(RequestConnector.class);

    /** The original content-type value. */
    private String myOriginal;
    // TODO: What is the exact defined word for "main type"?
    /** The main type. */
    private String myMainType;
    // TODO: What is the exact defined word for "sub type"?
    /** The sub type. */
    private String mySubType;
    /** The charset parameter. */
    private String myCharset;
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
