/****************************************************************************/
/*  File:       ConfigParam.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-27                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import org.apache.log4j.Logger;

/**
 * A config parameter.
 *
 * @author Florent Georges
 */
public class ConfigParam
{
    public ConfigParam(String id, String name, String desc, String value)
    {
        myId    = id;
        myName  = name;
        myDesc  = desc;
        myValue = value;
    }

    public String getId()
    {
        return myId;
    }

    public String getName()
    {
        return myName;
    }

    public String getDesc()
    {
        return myDesc;
    }

    public String getValue()
    {
        return myValue;
    }

    public void setValue(String value)
    {
        myValue = value;
    }

    public void logApplication(Logger log)
    {
        log.debug("      Config param: " + myId);
        log.debug("         Name : " + myName);
        log.debug("         Desc : " + myDesc);
        log.debug("         Value: " + myValue);
    }

    private final String myId;
    private final String myName;
    private final String myDesc;
    private String myValue;
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
