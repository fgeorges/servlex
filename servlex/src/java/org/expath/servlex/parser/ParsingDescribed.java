/****************************************************************************/
/*  File:       ParsingDescribed.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-27                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

/**
 * Represent a described object while parsing.
 *
 * @author Florent Georges
 */
abstract class ParsingDescribed
{
    public String getName()
    {
        return myName;
    }

    public void setName(String name)
    {
        myName = name;
    }

    public String getDescription()
    {
        return myDesc;
    }

    public void setDescription(String desc)
    {
        myDesc = desc;
    }

    private String myName;
    private String myDesc;
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
