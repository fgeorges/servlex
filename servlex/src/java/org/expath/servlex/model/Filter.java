/****************************************************************************/
/*  File:       Filter.java                                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.expath.servlex.components.Component;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.runtime.FilterInvocation;
import org.expath.servlex.runtime.Invocation;

/**
 * A filter around a servlet (or around another filter, error handler, etc.)
 *
 * @author Florent Georges
 * @date   2011-02-07
 */
public class Filter
        extends Wrapper
{
    public Filter(QName name, Component in, Component out)
    {
        super(name);
        myIn = in;
        myOut = out;
    }

    @Override
    public void logApplication(Logger log)
    {
        log.debug("      Filter");
        log.debug("         in : " + myIn);
        log.debug("         out: " + myOut);
        if ( myIn != null ) {
            myIn.logApplication(log);
        }
        if ( myOut != null ) {
            myOut.logApplication(log);
        }
    }

    @Override
    public Invocation makeInvocation(String path, RequestConnector request, Invocation wrapped)
    {
        return new FilterInvocation(myIn, myOut, wrapped, path, request);
    }

    private Component myIn;
    private Component myOut;
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
