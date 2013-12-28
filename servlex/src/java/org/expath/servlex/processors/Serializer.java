/****************************************************************************/
/*  File:       Serializer.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import java.io.OutputStream;
import javax.xml.namespace.QName;
import org.expath.servlex.TechnicalException;

/**
 * An abstract serializer.
 *
 * The object is constructed, serialization parameters are accumulated if
 * any, then the XDM items and the output stream are provided for actual
 * serialization.
 *
 * @author Florent Georges
 */
public interface Serializer
{
    /**
     * Serialize a document to an output stream.
     * 
     * The serializer uses the values of the various output properties.
     */
    public void serialize(Document doc, OutputStream out)
            throws TechnicalException;

    /**
     * Serialize a sequence to an output stream.
     * 
     * The serializer uses the values of the various output properties.
     */
    public void serialize(Sequence sequence, OutputStream out)
            throws TechnicalException;

    /**
     * Return the output property {@code media-type}.
     */
    public String getMediaType();

    /**
     * Return the output property {@code encoding}.
     */
    public String getEncoding();

    /**
     * Set an extension output property.
     * 
     * The name of the property is the name of the attribute on the element
     * web:body on the result.  If the property is not known by the specific
     * serializer implementation, it must raise a technical exception.
     */
    public void setExtension(QName n, String v)
            throws TechnicalException;

    /**
     * Set the output property {@code method}.
     */
    public void setMethod(String v);

    /**
     * Set the output property {@code media-type}.
     */
    public void setMediaType(String v);

    /**
     * Set the output property {@code encoding}.
     */
    public void setEncoding(String v);

    /**
     * Set the output property {@code byte-order-mark}.
     */
    public void setByteOrderMark(String v);

    /**
     * Set the output property {@code cdata-section-elements}.
     */
    public void setCdataSectionElements(String v);

    /**
     * Set the output property {@code doctype-public}.
     */
    public void setDoctypePublic(String v);

    /**
     * Set the output property {@code doctype-system}.
     */
    public void setDoctypeSystem(String v);

    /**
     * Set the output property {@code escape-uri-attributes}.
     */
    public void setEscapeUriAttributes(String v);

    /**
     * Set the output property {@code include-content-type}.
     */
    public void setIncludeContentType(String v);

    /**
     * Set the output property {@code indent}.
     */
    public void setIndent(String v);

    /**
     * Set the output property {@code normalization-form}.
     */
    public void setNormalizationForm(String v);

    /**
     * Set the output property {@code omit-xml-declaration}.
     */
    public void setOmitXmlDeclaration(String v);

    /**
     * Set the output property {@code standalone}.
     */
    public void setStandalone(String v);

    /**
     * Set the output property {@code undeclare-prefixes}.
     */
    public void setUndeclarePrefixes(String v);

    /**
     * Set the output property {@code use-character-maps}.
     */
    public void setUseCharacterMaps(String v);

    /**
     * Set the output property {@code version}.
     */
    public void setVersion(String v);
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
