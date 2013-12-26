/****************************************************************************/
/*  File:       RestError.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-25                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.rest;

import org.expath.servlex.ServlexException;

/**
 * Generic exception for REST.
 *
 * @author Florent Georges
 */
public class RestError
        extends ServlexException
{
    public RestError(int code, String status, String msg)
    {
        super(code, status);
        myMsg = msg;
    }

    public RestError(int code, String status, String msg, Throwable cause)
    {
        super(code, status, cause);
        myMsg = msg;
    }

    public String getUserMessage()
    {
        return myMsg;
    }

    private final String myMsg;
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
