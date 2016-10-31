/****************************************************************************/
/*  File:       ParsingConfigParam.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.net.URI;
import java.net.URISyntaxException;
import org.expath.servlex.model.ConfigParam;

/**
 * Represent config parameter element while parsing.
 *
 * @author Florent Georges
 */
class ParsingConfigParam
        extends ParsingDescribed
{
    public ParsingConfigParam(String id)
    {
        myId = id;
    }

    public String getId()
    {
        return myId;
    }

    public String getValue()
    {
        return myValue;
    }

    public void setValue(String value)
    {
        myValue = value;
    }

    public String getURI()
    {
        return myUri;
    }

    public void setURI(String uri)
    {
        myUri = uri;
    }

    public ConfigParam makeConfigParam(ParsingContext ctxt)
            throws ParseException
    {
        // validity checks
        if ( myId == null ) {
            throw new ParseException("@name is null on config param");
        }
        if ( myValue == null && myUri == null ) {
            throw new ParseException("Both @value and @uri are null on config param: " + myId);
        }
        if ( myValue != null && myUri != null ) {
            throw new ParseException(
                    "Both @value and @uri are set on config param: "
                    + myId + ", value: " + myValue + ", uri: " + myUri);
        }
        // get value
        String value = myValue;
        if ( myUri != null ) {
            URI base = ctxt.getBase();
            value = resolveURI(myUri, base);
        }
        // instantiate
        return new ConfigParam(myId, getName(), getDescription(), value);
    }

    @SuppressWarnings("null")
    private String resolveURI(String relative, URI base)
            throws ParseException
    {
        URI rel = null;
        try {
            rel = new URI(relative);
        }
        catch ( URISyntaxException ex ) {
            if ( base == null ) {
                throw new ParseException("Relative URI not valid and base URI null: " + relative);
            }
            try {
                base.resolve(relative);
            }
            catch ( IllegalArgumentException exx ) {
                throw new ParseException("Non valid URI in config param: " + relative, exx);
            }
        }
        if ( rel.isAbsolute() ) {
            return rel.toString();
        }
        if ( base == null ) {
            throw new ParseException("Relative URI not absolute and base URI null: " + relative);
        }
        URI resolved = base.resolve(rel);
        return resolved.toString();
    }

    private final String myId;
    private String myValue;
    private String myUri;
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
