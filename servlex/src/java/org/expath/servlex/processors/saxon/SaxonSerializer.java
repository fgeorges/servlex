/****************************************************************************/
/*  File:       SaxonSerializer.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmValue;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.Serializer;

/**
 * Implementation of serializer for Saxon.
 *
 * The object is constructed, serialization parameters are accumulated if
 * any, then the XDM items and the output stream are provided for actual
 * serialization.
 *
 * @author Florent Georges
 */
class SaxonSerializer
        implements Serializer
{
    public SaxonSerializer(Processor saxon){
        mySaxon = saxon;
    }

    @Override
    public String getMediaType() {
        return myMediaType;
    }
    @Override
    public String getEncoding() {
        return myEncoding;
    }

    @Override
    public void setExtension(QName name, String value)
            throws TechnicalException
    {
        if ( name.equals(S_ATTRIBUTE_ORDER) ) {
            throw new TechnicalException("Output property attribute not supported yet: " + name);
        }
        else if ( name.equals(S_CHARACTER_REPRESENTATION) ) {
            mySaxonCharacterRepresentation = value;
        }
        else if ( name.equals(S_DOUBLE_SPACE) ) {
            mySaxonDoubleSpace = value;
        }
        else if ( name.equals(S_IMPLICIT_RESULT_DOCUMENT) ) {
            throw new TechnicalException("Output property attribute not supported: " + name);
        }
        else if ( name.equals(S_INDENT_SPACES) ) {
            mySaxonIndentSpaces = value;
        }
        else if ( name.equals(S_LINE_LENGTH) ) {
            mySaxonLineLength = value;
        }
        else if ( name.equals(S_NEXT_IN_CHAIN) ) {
            throw new TechnicalException("Output property attribute not supported: " + name);
        }
        else if ( name.equals(S_NEXT_IN_CHAIN_BASE_URI) ) {
            throw new TechnicalException("Output property attribute not supported: " + name);
        }
        else if ( name.equals(S_RECOGNIZE_BINARY) ) {
            mySaxonRecognizeBinary = value;
        }
        else if ( name.equals(S_REQUIRE_WELL_FORMED) ) {
            mySaxonRequireWellFormed = value;
        }
        else if ( name.equals(S_STYLESHEET_VERSION) ) {
            mySaxonStylesheetVersion = value;
        }
        else if ( name.equals(S_SUPPLY_SOURCE_LOCATOR) ) {
            throw new TechnicalException("Output property attribute not supported: " + name);
        }
        else if ( name.equals(S_SUPPRESS_INDENTATION) ) {
            mySaxonSuppressIndentation = value;
        }
        else if ( name.equals(S_WRAP) ) {
            mySaxonWrap = value;
        }
        else {
            throw new TechnicalException("Unknown output property attribute: " + name);
        }
    }

    @Override
    public void setMethod(String v) {
        myMethod = v;
    }
    @Override
    public void setMediaType(String v) {
        myMediaType = v;
    }
    @Override
    public void setEncoding(String v) {
        myEncoding = v;
    }
    @Override
    public void setByteOrderMark(String v) {
        myByteOrderMark = v;
    }
    @Override
    public void setCdataSectionElements(String v) {
        myCdataSectionElements = v;
    }
    @Override
    public void setDoctypePublic(String v) {
        myDoctypePublic = v;
    }
    @Override
    public void setDoctypeSystem(String v) {
        myDoctypeSystem = v;
    }
    @Override
    public void setEscapeUriAttributes(String v) {
        myEscapeUriAttributes = v;
    }
    @Override
    public void setIncludeContentType(String v) {
        myIncludeContentType = v;
    }
    @Override
    public void setIndent(String v) {
        myIndent = v;
    }
    @Override
    public void setNormalizationForm(String v) {
        myNormalizationForm = v;
    }
    @Override
    public void setOmitXmlDeclaration(String v) {
        myOmitXmlDeclaration = v;
    }
    @Override
    public void setStandalone(String v) {
        myStandalone = v;
    }
    @Override
    public void setUndeclarePrefixes(String v) {
        myUndeclarePrefixes = v;
    }
    @Override
    public void setUseCharacterMaps(String v) {
        myUseCharacterMaps = v;
    }
    @Override
    public void setVersion(String v) {
        myVersion = v;
    }

    @Override
    public void serialize(Document doc, OutputStream out)
            throws TechnicalException
    {
        XdmValue value = SaxonHelper.toXdmValue(doc);
        serialize(value, out);
    }

    @Override
    public void serialize(Sequence sequence, OutputStream out)
            throws TechnicalException
    {
        XdmValue value = SaxonHelper.toXdmValue(sequence);
        serialize(value, out);
    }

    private void serialize(XdmValue sequence, OutputStream out)
            throws TechnicalException
    {
        String method = methodFromMime(myMediaType);
        // TODO: @method could also contain "base64" or "hex".  Take it into account!
        // TODO: Can I use Saxon extension methods here?  Like "saxon:base64Binary"
        // or "saxon:hexBinary"...
        // See http://saxonica.com/documentation/#!extensions/output-extras.
        // TODO: What if myMethod is set?  We don't take it into account here...?
        if ( "binary".equals(method) ) {
            method = "text";
            out = new Base64OutputStream(out, false);
        }
        net.sf.saxon.s9api.Serializer serial = new net.sf.saxon.s9api.Serializer();
        serial.setOutputStream(out);
        if ( myMethod == null && myMediaType != null ) {
            myMethod = method;
        }

        setOutputProperty(serial, Property.METHOD,                 myMethod);
        setOutputProperty(serial, Property.MEDIA_TYPE,             myMediaType);
        setOutputProperty(serial, Property.ENCODING,               myEncoding);
        setOutputProperty(serial, Property.BYTE_ORDER_MARK,        myByteOrderMark);
        setOutputProperty(serial, Property.CDATA_SECTION_ELEMENTS, myCdataSectionElements);
        setOutputProperty(serial, Property.DOCTYPE_PUBLIC,         myDoctypePublic);
        setOutputProperty(serial, Property.DOCTYPE_SYSTEM,         myDoctypeSystem);
        setOutputProperty(serial, Property.ESCAPE_URI_ATTRIBUTES,  myEscapeUriAttributes);
        setOutputProperty(serial, Property.INCLUDE_CONTENT_TYPE,   myIncludeContentType);
        setOutputProperty(serial, Property.INDENT,                 myIndent);
        setOutputProperty(serial, Property.NORMALIZATION_FORM,     myNormalizationForm);
        setOutputProperty(serial, Property.OMIT_XML_DECLARATION,   myOmitXmlDeclaration);
        setOutputProperty(serial, Property.STANDALONE,             myStandalone);
        setOutputProperty(serial, Property.UNDECLARE_PREFIXES,     myUndeclarePrefixes);
        setOutputProperty(serial, Property.USE_CHARACTER_MAPS,     myUseCharacterMaps);
        setOutputProperty(serial, Property.VERSION,                myVersion);

        setOutputProperty(serial, Property.SAXON_CHARACTER_REPRESENTATION, mySaxonCharacterRepresentation);
        setOutputProperty(serial, Property.SAXON_DOUBLE_SPACE,             mySaxonDoubleSpace);
        setOutputProperty(serial, Property.SAXON_INDENT_SPACES,            mySaxonIndentSpaces);
        setOutputProperty(serial, Property.SAXON_LINE_LENGTH,              mySaxonLineLength);
        setOutputProperty(serial, Property.SAXON_RECOGNIZE_BINARY,         mySaxonRecognizeBinary);
        setOutputProperty(serial, Property.SAXON_REQUIRE_WELL_FORMED,      mySaxonRequireWellFormed);
        setOutputProperty(serial, Property.SAXON_STYLESHEET_VERSION,       mySaxonStylesheetVersion);
        setOutputProperty(serial, Property.SAXON_SUPPRESS_INDENTATION,     mySaxonSuppressIndentation);
        setOutputProperty(serial, Property.SAXON_WRAP,                     mySaxonWrap);

        try {
            mySaxon.writeXdmValue(sequence, serial);
        }
        catch ( SaxonApiException ex ) {
            throw new TechnicalException("Error serializing sequence to the output stream", ex);
        }
    }

    /**
     * Take care of checking if the value is null.
     */
    private void setOutputProperty(net.sf.saxon.s9api.Serializer serial, Property p, String v)
    {
        if ( v != null ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("Serializer, set property '" + p + "' to '" + v + "'");
            }
            serial.setOutputProperty(p, v);
        }
    }

    /**
     * Decode the content type from a MIME type string (only single parts).
     *
     * TODO: Handle XHTML (and new binary methods?)...
     */
    private String methodFromMime(String mime)
    {
        if ( myMethod != null ) {
            // if an explicit method, return it
            return myMethod;
        }
        else if ( mime == null ) {
            return "binary";
        }
        else if ( mime.startsWith("multipart/") ) {
            throw new IllegalArgumentException("Multipart not handled yet!");
        }
        else if ( "text/html".equals(mime) ) {
            return "xhtml";
        }
        else if ( mime.endsWith("+xml") || XML_TYPES.contains(mime) ) {
            return "xml";
        }
        else if ( mime.startsWith("text/") || TEXT_TYPES.contains(mime) ) {
            return "text";
        }
        else {
            return "binary";
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(SaxonSerializer.class);

    /**
     * The Saxon namespace (to declare extension output properties on web:body).
     * 
     * @see http://saxonica.com/documentation/#!extensions/output-extras
     */
    private static final String NS = "http://saxon.sf.net/";
    // attribute names
    private static final QName S_ATTRIBUTE_ORDER          = new QName(NS, "saxon:attribute-order");
    private static final QName S_CHARACTER_REPRESENTATION = new QName(NS, "saxon:character-representation");
    private static final QName S_DOUBLE_SPACE             = new QName(NS, "saxon:double-space");
    private static final QName S_IMPLICIT_RESULT_DOCUMENT = new QName(NS, "saxon:implicit-result-document");
    private static final QName S_INDENT_SPACES            = new QName(NS, "saxon:indent-spaces");
    private static final QName S_LINE_LENGTH              = new QName(NS, "saxon:line-length");
    private static final QName S_NEXT_IN_CHAIN            = new QName(NS, "saxon:next-in-chain");
    private static final QName S_NEXT_IN_CHAIN_BASE_URI   = new QName(NS, "saxon:next-in-chain-base-uri");
    private static final QName S_RECOGNIZE_BINARY         = new QName(NS, "saxon:recognize-binary");
    private static final QName S_REQUIRE_WELL_FORMED      = new QName(NS, "saxon:require-well-formed");
    private static final QName S_STYLESHEET_VERSION       = new QName(NS, "saxon:stylesheet-version");
    private static final QName S_SUPPLY_SOURCE_LOCATOR    = new QName(NS, "saxon:supply-source-locator");
    private static final QName S_SUPPRESS_INDENTATION     = new QName(NS, "saxon:suppress-indentation");
    private static final QName S_WRAP                     = new QName(NS, "saxon:wrap"); 

    private Processor mySaxon;

    // TODO: Directly create a property map, instead of several fields...
    private String myMethod;
    private String myMediaType;
    private String myEncoding = "UTF-8"; // by default...
    private String myByteOrderMark;
    private String myCdataSectionElements;
    private String myDoctypePublic;
    private String myDoctypeSystem;
    private String myEscapeUriAttributes;
    private String myIncludeContentType;
    private String myIndent;
    private String myNormalizationForm;
    private String myOmitXmlDeclaration;
    private String myStandalone;
    private String myUndeclarePrefixes;
    private String myUseCharacterMaps;
    private String myVersion;

    private String mySaxonCharacterRepresentation;
    private String mySaxonDoubleSpace;
    private String mySaxonIndentSpaces;
    private String mySaxonLineLength;
    private String mySaxonRecognizeBinary;
    private String mySaxonRequireWellFormed;
    private String mySaxonStylesheetVersion;
    private String mySaxonSuppressIndentation;
    private String mySaxonWrap;

    /** Media types that must be treated as text types (in addition to text/*). */
    private static Set<String> TEXT_TYPES;
    static {
        Set<String> types = new HashSet<String>();
        types.add("application/x-www-form-urlencoded");
        types.add("application/xml-dtd");
        TEXT_TYPES = types;
    }

    /** Media types that must be treated as XML types (in addition to *+xml). */
    private static Set<String> XML_TYPES;
    static {
        // Doc: does not handle "application/xml-dtd" as XML
        // TODO: What about ".../xml-external-parsed-entity" ?
        Set<String> types = new HashSet<String>();
        types.add("text/xml");
        types.add("application/xml");
        types.add("text/xml-external-parsed-entity");
        types.add("application/xml-external-parsed-entity");
        XML_TYPES = types;
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
