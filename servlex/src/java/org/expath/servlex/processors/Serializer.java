/****************************************************************************/
/*  File:       Serializer.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import java.io.OutputStream;
import net.sf.saxon.s9api.XdmValue;
import org.expath.servlex.TechnicalException;

/**
 * An abstract serializer.
 *
 * The object is constructed, serialization parameters are accumulated if
 * any, then the XDM items and the output stream are provided for actual
 * serialization.
 *
 * @author Florent Georges
 * @date   2013-04-16
 */
public interface Serializer
{
    public String getMediaType();
    public String getEncoding();

    public void setMethod(String v);
    public void setMediaType(String v);
    public void setEncoding(String v);
    public void setByteOrderMark(String v);
    public void setCdataSectionElements(String v);
    public void setDoctypePublic(String v);
    public void setDoctypeSystem(String v);
    public void setEscapeUriAttributes(String v);
    public void setIncludeContentType(String v);
    public void setIndent(String v);
    public void setNormalizationForm(String v);
    public void setOmitXmlDeclaration(String v);
    public void setStandalone(String v);
    public void setUndeclarePrefixes(String v);
    public void setUseCharacterMaps(String v);
    public void setVersion(String v);

    // TODO: FIXME: XdmValue is Saxon-specific, this has NOTHING to do here...!
    public void serialize(XdmValue sequence, OutputStream out)
            throws TechnicalException;
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
