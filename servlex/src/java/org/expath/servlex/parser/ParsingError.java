/****************************************************************************/
/*  File:       ParsingError.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import javax.xml.namespace.QName;
import org.expath.servlex.components.Component;
import org.expath.servlex.model.ErrorHandler;
import org.expath.servlex.model.Wrapper;

/**
 * Represent an address handler while parsing.
 *
 * @author Florent Georges
 * @date   2013-09-16
 */
class ParsingError
        extends ParsingWrapper
{
    public ParsingError(String name)
    {
        super(name);
    }

    public void setImplem(Component implem)
    {
        myImplem = implem;
    }

    public void setCode(QName code)
    {
        myCode = code;
    }

    public void setNs(String ns)
    {
        myNs = ns;
    }

    public void setLocal(String local)
    {
        myLocal = local;
    }

    @Override
    public ErrorHandler instantiate(ParsingContext ctxt)
            throws ParseException
    {
        ErrorHandler error;
        if ( myCode == null && myNs == null && myLocal == null ) {
            error = new ErrorHandler(getName(), myImplem);
        }
        else {
            error = new ErrorHandler(getName(), myImplem, myCode, myNs, myLocal);
        }
        Wrapper w = makeWrapper(ctxt);
        error.setWrapper(w);
        return error;
    }

    private Component myImplem;
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
