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
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmValue;
import org.apache.commons.codec.binary.Base64OutputStream;
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
 * @date   2010-02-15
 */
class SaxonSerializer
        implements Serializer
{
    public SaxonSerializer(Processor saxon){
        mySaxon = saxon;
    }

    public String getMediaType() {
        return myMediaType;
    }
    public String getEncoding() {
        return myEncoding;
    }

    public void setMethod(String v) {
        myMethod = v;
    }
    public void setMediaType(String v) {
        myMediaType = v;
    }
    public void setEncoding(String v) {
        myEncoding = v;
    }
    public void setByteOrderMark(String v) {
        myByteOrderMark = v;
    }
    public void setCdataSectionElements(String v) {
        myCdataSectionElements = v;
    }
    public void setDoctypePublic(String v) {
        myDoctypePublic = v;
    }
    public void setDoctypeSystem(String v) {
        myDoctypeSystem = v;
    }
    public void setEscapeUriAttributes(String v) {
        myEscapeUriAttributes = v;
    }
    public void setIncludeContentType(String v) {
        myIncludeContentType = v;
    }
    public void setIndent(String v) {
        myIndent = v;
    }
    public void setNormalizationForm(String v) {
        myNormalizationForm = v;
    }
    public void setOmitXmlDeclaration(String v) {
        myOmitXmlDeclaration = v;
    }
    public void setStandalone(String v) {
        myStandalone = v;
    }
    public void setUndeclarePrefixes(String v) {
        myUndeclarePrefixes = v;
    }
    public void setUseCharacterMaps(String v) {
        myUseCharacterMaps = v;
    }
    public void setVersion(String v) {
        myVersion = v;
    }

    public void serialize(Document doc, OutputStream out)
            throws TechnicalException
    {
        XdmValue value = SaxonHelper.toXdmValue(doc);
        serialize(value, out);
    }

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
        if ( "binary".equals(method) ) {
            method = "text";
            out = new Base64OutputStream(out, false);
        }
        net.sf.saxon.s9api.Serializer serial = new net.sf.saxon.s9api.Serializer();
        serial.setOutputStream(out);
        if ( myMethod == null && myMediaType != null ) {
            myMethod = method;
        }
        setOutputProperty(serial, Property.METHOD,         myMethod);
        setOutputProperty(serial, Property.MEDIA_TYPE,     myMediaType);
        setOutputProperty(serial, Property.ENCODING,       myEncoding);
        setOutputProperty(serial, Property.BYTE_ORDER_MARK, myByteOrderMark);
        setOutputProperty(serial, Property.CDATA_SECTION_ELEMENTS, myCdataSectionElements);
        setOutputProperty(serial, Property.DOCTYPE_PUBLIC, myDoctypePublic);
        setOutputProperty(serial, Property.DOCTYPE_SYSTEM, myDoctypeSystem);
        setOutputProperty(serial, Property.ESCAPE_URI_ATTRIBUTES, myEscapeUriAttributes);
        setOutputProperty(serial, Property.INCLUDE_CONTENT_TYPE, myIncludeContentType);
        setOutputProperty(serial, Property.INDENT, myIndent);
        setOutputProperty(serial, Property.NORMALIZATION_FORM, myNormalizationForm);
        setOutputProperty(serial, Property.OMIT_XML_DECLARATION, myOmitXmlDeclaration);
        setOutputProperty(serial, Property.STANDALONE, myStandalone);
        setOutputProperty(serial, Property.UNDECLARE_PREFIXES, myUndeclarePrefixes);
        setOutputProperty(serial, Property.USE_CHARACTER_MAPS, myUseCharacterMaps);
        setOutputProperty(serial, Property.VERSION, myVersion);
//        setOutputProperty(serial, Property.SAXON_CHARACTER_REPRESENTATION, mySaxonCharacterRepresentation);
//        setOutputProperty(serial, Property.SAXON_DOUBLE_SPACE, mySaxonDoubleSpace);
//        setOutputProperty(serial, Property.SAXON_IMPLICIT_RESULT_DOCUMENT, mySaxonImplicitResultDocument);
//        setOutputProperty(serial, Property.SAXON_INDENT_SPACES, mySaxonIndentSpaces);
//        setOutputProperty(serial, Property.SAXON_NEXT_IN_CHAIN, mySaxonNextInChain);
//        setOutputProperty(serial, Property.SAXON_NEXT_IN_CHAIN_BASE_URI, mySaxonNextInChainBaseUri);
//        setOutputProperty(serial, Property.SAXON_RECOGNIZE_BINARY, mySaxonRecognizeBinary);
//        setOutputProperty(serial, Property.SAXON_REQUIRE_WELL_FORMED, mySaxonRequireWellFormed);
//        setOutputProperty(serial, Property.SAXON_STYLESHEET_VERSION, mySaxonStylesheetVersion);
//        setOutputProperty(serial, Property.SAXON_SUPPLY_SOURCE_LOCATOR, mySaxonSupplySourceLocator);
//        setOutputProperty(serial, Property.SAXON_SUPPRESS_INDENTATION, mySaxonSuppressIndentation);
//        setOutputProperty(serial, Property.SAXON_WRAP, mySaxonWrap);

        // TODO: Other serialization parameters...
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
            return "html";
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
//    private String mySaxonCharacterRepresentation;
//    private String mySaxonDoubleSpace;
//    private String mySaxonImplicitResultDocument;
//    private String mySaxonIndentSpaces;
//    private String mySaxonNextInChain;
//    private String mySaxonNextInChainBaseUri;
//    private String mySaxonRecognizeBinary;
//    private String mySaxonRequireWellFormed;
//    private String mySaxonStylesheetVersion;
//    private String mySaxonSupplySourceLocator;
//    private String mySaxonSuppressIndentation;
//    private String mySaxonWrap;

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
