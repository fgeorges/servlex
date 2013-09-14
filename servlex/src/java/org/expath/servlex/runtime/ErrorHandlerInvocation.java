/****************************************************************************/
/*  File:       ErrorHandlerInvocation.java                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.runtime;

import javax.xml.namespace.QName;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.ServlexException;
import org.expath.servlex.components.Component;
import org.expath.servlex.connectors.Connector;
import org.expath.servlex.connectors.ErrorConnector;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.model.Application;
import org.expath.servlex.tools.Auditor;

/**
 * The runtime part of an error handler.
 *
 * TODO: For now, an error handler invocation matches a QName.  It must
 * actually be able to match a wildcard.  That is, it must accept a NameTest
 * from the XPath 2.0 grammar:
 *
 * <pre>
 * [36] NameTest ::= QName | Wildcard
 * [37] Wildcard ::= "*" | (NCName ":" "*") | ("*" ":" NCName)
 * </pre>
 *
 * @author Florent Georges
 * @date   2011-02-09
 */
public class ErrorHandlerInvocation
        extends Invocation
{
    public ErrorHandlerInvocation(String path, RequestConnector request, Invocation wrapped, Component impl, boolean every, QName code, String ns, String local)
    {
        super(path, request);
        myWrapped = wrapped;
        myImpl    = impl;
        myEvery   = every;
        myCode    = code;
        myNs      = ns;
        myLocal   = local;
    }

    @Override
    public Connector invoke(Connector connector, Application app, ServerConfig config, Auditor auditor)
            throws ServlexException
                 , ComponentError
    {
        auditor.invoke("error handler");
        try {
            return myWrapped.invoke(connector, app, config, auditor);
        }
        catch ( ComponentError ex ) {
            if ( matches(ex.getName()) ) {
                try {
                    Connector c = new ErrorConnector(ex, getRequest(), auditor);
                    return myImpl.run(c, config, auditor);
                }
                catch ( ComponentError ex2 ) {
                    throw new ServlexException(500, "Internal error (error in an error handler)", ex2);
                }
            }
            else {
                throw ex;
            }
        }
    }

    public boolean matches(QName name)
    {
        if ( myEvery ) {
            return true;
        }
        else if ( myNs == null && myLocal.equals(name.getLocalPart()) ) {
            return true;
        }
        else if ( myLocal == null && myNs.equals(name.getNamespaceURI()) ) {
            return true;
        }
        else {
            return myCode.equals(name);
        }
    }

    private Invocation myWrapped;
    private Component  myImpl;
    private boolean    myEvery;
    private QName      myCode;
    private String     myNs;
    private String     myLocal;
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
