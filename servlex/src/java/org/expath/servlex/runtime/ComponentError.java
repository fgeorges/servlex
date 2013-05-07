/****************************************************************************/
/*  File:       ComponentError.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-02-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.runtime;

import javax.xml.namespace.QName;
import org.expath.servlex.processors.Sequence;

/**
 * An error thrown by a component. Can be caught by an error handler.
 * 
 * A component error has 3 properties, like an XPath error: a name, a message,
 * and a user-supplied sequence of items.  Each of those 3 properties can be
 * null.
 *
 * @author Florent Georges
 * @date   2011-02-08
 */
public class ComponentError
        extends Exception
{
    /**
     * Build a new object, based on the initial exception and the XPath error.
     *
     * An XPath error contains a name, a message and a user sequence.
     */
    public ComponentError(Throwable ex, QName name, String msg, Sequence sequence)
    {
        super(ex);
        myName = name;
        myMsg = msg;
        mySequence = sequence;
    }

    public QName getName()
    {
        return myName;
    }

    public String getMsg()
    {
        return myMsg;
    }

    public Sequence getSequence()
    {
        return mySequence;
    }

    private QName myName;
    private String myMsg;
    private Sequence mySequence;
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
