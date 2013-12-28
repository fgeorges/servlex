/****************************************************************************/
/*  File:       WebappDecl.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A webapp declaration, as read from {@code .expath-web/webapps.xml}.
 * 
 * @author Florent Georges
 */
public class WebappDecl
{
    public WebappDecl(URI name, String root)
    {
        myName = name;
        myRoot = root;
    }

    public URI getName()
    {
        return myName;
    }

    public String getRoot()
    {
        return myRoot;
    }

    public void setConfigParam(String id, String value)
    {
        myConfigParams.put(id, value);
    }

    public Map<String, String> getConfigParams()
    {
        return myConfigParams;
    }

    private final URI    myName;
    private final String myRoot;
    private final Map<String, String> myConfigParams = new HashMap<>();
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
