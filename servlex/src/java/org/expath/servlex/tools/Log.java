/****************************************************************************/
/*  File:       Log.java                                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2015-05-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2015 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Logging class, to isolate dependencies on any logging system.
 * 
 * @author Florent Georges
 */
public class Log
{
    public Log(Class c)
    {
        myLogger = Logger.getLogger(c.getName());
    }

    public boolean trace()
    {
        return enabled(TRACE);
    }

    public void trace(String msg)
    {
        log(TRACE, msg);
    }

    public boolean debug()
    {
        return enabled(DEBUG);
    }

    public void debug(String msg)
    {
        log(DEBUG, msg);
    }

    public void debug(String msg, Throwable ex)
    {
        log(DEBUG, msg, ex);
    }

    public boolean info()
    {
        return enabled(INFO);
    }

    public void info(String msg)
    {
        log(INFO, msg);
    }

    public void info(String msg, Throwable ex)
    {
        log(INFO, msg, ex);
    }

    public void warn(String msg)
    {
        log(WARN, msg);
    }

    public void warn(String msg, Throwable ex)
    {
        log(WARN, msg, ex);
    }

    public void error(String msg)
    {
        log(ERROR, msg);
    }

    public void error(String msg, Throwable ex)
    {
        log(ERROR, msg, ex);
    }

    private boolean enabled(Level lvl)
    {
        return myLogger.isLoggable(lvl);
    }

    private void log(Level lvl, String msg)
    {
        myLogger.log(lvl, msg);
    }

    private void log(Level lvl, String msg, Throwable ex)
    {
        myLogger.log(lvl, msg, ex);
    }

    @SuppressWarnings("NonConstantLogger")
    private final Logger myLogger;

    private static final Level TRACE = Level.FINEST;
    private static final Level DEBUG = Level.FINE;
    private static final Level INFO  = Level.INFO;
    private static final Level WARN  = Level.WARNING;
    private static final Level ERROR = Level.SEVERE;
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
