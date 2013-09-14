/****************************************************************************/
/*  File:       Chain.java                                                  */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.runtime.Invocation;

/**
 * A sequence of wrappers (filters and error handlers).
 *
 * @author Florent Georges
 * @date   2011-02-08
 */
public class Chain
        extends Wrapper
{
    public Chain(QName name, Wrapper[] wrappers)
    {
        super(name);
        myWrappers = wrappers;
    }

    @Override
    public void logApplication(Logger log)
    {
        log.debug("      Chain");
        log.debug("         wrappers: " + myWrappers);
        if ( myWrappers != null ) {
            for ( Wrapper w : myWrappers ) {
                w.logApplication(log);
            }
        }
    }

    @Override
    public Invocation makeInvocation(String path, RequestConnector request, Invocation wrapped)
    {
        // chain all invocations, beginning by wrapped, to the outermost
        for ( int i = myWrappers.length - 1; i >= 0; --i ) {
            wrapped = myWrappers[i].makeInvocation(path, request, wrapped);
        }
        return wrapped;
    }

    private Wrapper[] myWrappers;
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
