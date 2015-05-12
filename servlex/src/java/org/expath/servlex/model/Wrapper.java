/****************************************************************************/
/*  File:       Wrapper.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.tools.Auditor;
import org.expath.servlex.tools.Cleanable;
import org.expath.servlex.tools.Log;

/**
 * Servlet wrapper (can wrap a servlet, a filter, an error handler or a chain).
 *
 * @author Florent Georges
 */
public abstract class Wrapper
        implements Cleanable
{
    public Wrapper(String name)
    {
        myName = name;
    }

    @Override
    public void cleanup(Auditor auditor)
            throws ServlexException
    {
        auditor.cleanup("wrapper " + myName);
    }

    /**
     * Return the name of the wrapper, which can be null.
     */
    public String getName()
    {
        return myName;
    }

    public abstract void logApplication(Log log);

    public abstract Invocation makeInvocation(String path, RequestConnector request, Invocation wrapped);

    private String myName;
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
