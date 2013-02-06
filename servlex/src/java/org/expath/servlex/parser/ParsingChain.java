/****************************************************************************/
/*  File:       ParsingChain.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.expath.servlex.model.Wrapper;

/**
 * Represent a chain while parsing.
 *
 * @author Florent Georges
 * @date   2012-05-08
 */
class ParsingChain
{
    public ParsingChain(QName name)
    {
        myName = name;
    }

    public QName getName()
    {
        return myName;
    }

    public void addFilter(QName f)
    {
        myFilters.add(f);
    }

    public List<Wrapper> makeFilters(ParsingContext ctxt)
            throws ParseException
    {
        List<Wrapper> wrappers = new ArrayList<Wrapper>();
        for ( QName n : myFilters ) {
            Wrapper w = ctxt.getWrapper(n);
            wrappers.add(w);
        }
        return wrappers;
    }

    private QName       myName;
    private List<QName> myFilters = new ArrayList<QName>();
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
