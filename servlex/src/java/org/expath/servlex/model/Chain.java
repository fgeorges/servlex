/****************************************************************************/
/*  File:       Chain.java                                                  */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import org.apache.log4j.Logger;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.tools.Auditor;

/**
 * A sequence of wrappers (filters and error handlers).
 *
 * @author Florent Georges
 * @date   2011-02-08
 */
public class Chain
        extends Wrapper
{
    public Chain(String name, Wrapper[] wrappers)
    {
        super(name);
        myWrappers = wrappers;
        int len = wrappers.length;
        myReverse  = new Wrapper[len];
        for ( int i = 0; i < len; ++i ) {
            myReverse[i] = myWrappers[len - 1 - i];
        }
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("chain");
        for ( Wrapper w : myWrappers ) {
            w.cleanup(auditor);
        }
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
        for ( Wrapper w : myReverse ) {
            wrapped = w.makeInvocation(path, request, wrapped);
        }
        return wrapped;
    }

    private final Wrapper[] myWrappers;
    private final Wrapper[] myReverse;
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
