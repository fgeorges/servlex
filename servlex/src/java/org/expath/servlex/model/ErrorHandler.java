/****************************************************************************/
/*  File:       ErrorHandler.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-04-25                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import javax.xml.namespace.QName;
import org.expath.servlex.components.Component;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.runtime.ErrorHandlerInvocation;
import org.expath.servlex.runtime.Invocation;

/**
 * An error handler.
 * 
 * For now only a global error handler (global to a specific application), but
 * we should probably  be able to set specific handlers on specific servlets,
 * groups, and/or chains.
 *
 * @author Florent Georges
 * @date   2012-04-25
 */
public class ErrorHandler
        extends Wrapper
{
    /**
     * Build a new error handler matching every error.
     */
    public ErrorHandler(QName name, Component impl)
    {
        super(name);
        myImpl  = impl;
        myEvery = true;
    }

    /**
     * Build a new error handler matching a specific error.
     * 
     * If it has to match a specific code, no parameter can be null.  If it
     * has to match errors in a specific namespace, the code and the local
     * name must be null.  If it has to match a specific local name, the code
     * and the namespace must be null.  Any other combination is an error.
     * 
     * @param name The name of the error handler itself.
     *          Can be null.
     * @param impl The implementation of the error handler.
     *          Cannot be null.
     * @param code The error code matched by this handler.
     *          Can be null (see above).
     * @param namespace The error code namespace matched by this handler.
     *          Can be null (see above).
     * @param local The error code local name matched by this handler.
     *          Can be null (see above).
     */
    public ErrorHandler(QName name, Component impl, QName code, String namespace, String local)
    {
        super(name);
        myImpl  = impl;
        myEvery = false;
        myCode  = code;
        myNs    = namespace;
        myLocal = local;
    }

    @Override
    public Invocation makeInvocation(String path, RequestConnector request, Invocation wrapped)
    {
        return new ErrorHandlerInvocation(path, request, wrapped, myImpl, myEvery, myCode, myNs, myLocal);
    }

    private Component myImpl;
    private boolean   myEvery;
    private QName     myCode;
    private String    myNs;
    private String    myLocal;
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
