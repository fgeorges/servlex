/****************************************************************************/
/*  File:       StringsProperties.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.ArrayList;
import java.util.List;
import org.expath.servlex.TechnicalException;

/**
 * A {@link Properties} implementation for lists of {@link String} values.
 *
 * @author Florent Georges
 * @date   2013-05-19
 */
public class StringsProperties
        extends Properties<String>
{
    /**
     * Constructs a new Properties object with a private property name prefix.
     */
    public StringsProperties(String private_prefix)
    {
        super(private_prefix);
    }

    @Override
    protected String valueAsString(String key, String value)
            throws TechnicalException
    {
        return value;
    }

    @Override
    protected Iterable<String> valueFromString(String value)
            throws TechnicalException
    {
        List<String> list = new ArrayList<String>(1);
        list.add(value);
        return list;
    }
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
