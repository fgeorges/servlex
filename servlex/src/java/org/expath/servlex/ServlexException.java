/****************************************************************************/
/*  File:       ServlexException.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-09-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

/**
 * Generic exception for Servlex.
 *
 * It must be initialized with an HTTP error code (e.g. 404).  If the exception
 * goes up to the last Servlex layer, this error code and the exception message
 * are used to return an error to the HTTP client.
 *
 * It is also possible to give some header to set in case this exception results
 * in an HTTP response.  For instance, an HTTP 405 (Method not allowed) requires
 * the headers Allow to be set in the response, with the list of allowed methods.
 *
 * @author Florent Georges
 */
public class ServlexException
        extends Exception
{
    public ServlexException(int code, String msg)
    {
        super(msg);
        myCode = code;
    }

    public ServlexException(int code, String msg, Throwable cause)
    {
        super(msg, cause);
        myCode = code;
    }

    /**
     * Add a new header, to be set on the response in case sendError() is called.
     */
    public void addHeader(String name, String value)
    {
        if ( myHeaders == null ) {
            myHeaders = new ArrayList<>();
        }
        myHeaders.add(new HeaderPair(name, value));
    }

    public void setStatus(HttpServletResponse resp)
            throws IOException
    {
        if ( myHeaders != null ) {
            for ( HeaderPair h : myHeaders ) {
                resp.addHeader(h.name, h.value);
            }
        }
        resp.setStatus(myCode);
    }

    private final int myCode;
    private List<HeaderPair> myHeaders = null;

    private static class HeaderPair {
        public HeaderPair(String n, String v) {
            name = n;
            value = v;
        }
        public String name;
        public String value;
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
