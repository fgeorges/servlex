/****************************************************************************/
/*  File:       Log.java                                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2015-05-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2015 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Logging class, to isolate dependencies on any logging system.
 * 
 * @author Florent Georges
 */
public class Log
{
    public Log(Class c)
    {
        myLogger = LogManager.getLogger(c);
    }

    public boolean trace()
    {
        return myLogger.isTraceEnabled();
    }

    public void trace(String msg)
    {
        myLogger.trace(msg);
    }

    public boolean debug()
    {
        return myLogger.isDebugEnabled();
    }

    public void debug(String msg)
    {
        myLogger.debug(msg);
    }

    public void debug(String msg, Throwable ex)
    {
        myLogger.debug(msg, ex);
    }

    public boolean info()
    {
        return myLogger.isInfoEnabled();
    }

    public void info(String msg)
    {
        myLogger.info(msg);
    }

    public void info(String msg, Throwable ex)
    {
        myLogger.info(msg, ex);
    }

    public void warn(String msg)
    {
        myLogger.warn(msg);
    }

    public void warn(String msg, Throwable ex)
    {
        myLogger.warn(msg, ex);
    }

    public void error(String msg)
    {
        myLogger.error(msg);
    }

    public void error(String msg, Throwable ex)
    {
        myLogger.error(msg, ex);
    }

    private Logger myLogger;
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
