/****************************************************************************/
/*  File:       WebappFunctions.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-24                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon;

import net.sf.saxon.s9api.Processor;
import org.expath.servlex.ServerConfig;
import net.servlex.saxon.functions.GetRequestFieldFunction;
import net.servlex.saxon.functions.GetRequestFieldNamesFunction;
import net.servlex.saxon.functions.GetServerFieldFunction;
import net.servlex.saxon.functions.GetServerFieldNamesFunction;
import net.servlex.saxon.functions.GetSessionFieldFunction;
import net.servlex.saxon.functions.GetSessionFieldNamesFunction;
import net.servlex.saxon.functions.GetWebappFieldFunction;
import net.servlex.saxon.functions.GetWebappFieldNamesFunction;
import net.servlex.saxon.functions.ParseBasicAuthFunction;
import net.servlex.saxon.functions.ParseHeaderValueFunction;
import net.servlex.saxon.functions.SetRequestFieldFunction;
import net.servlex.saxon.functions.SetServerFieldFunction;
import net.servlex.saxon.functions.SetSessionFieldFunction;
import net.servlex.saxon.functions.SetWebappFieldFunction;
import org.expath.servlex.processors.Processors;
import net.servlex.saxon.functions.ConfigParamFunction;
import net.servlex.saxon.functions.ExecuteFunction;
import net.servlex.saxon.functions.InstallEnabledFunction;
import net.servlex.saxon.functions.InstallFromCxanFunction;
import net.servlex.saxon.functions.InstallWebappFunction;
import net.servlex.saxon.functions.InstalledWebappsFunction;
import net.servlex.saxon.functions.ReloadWebappsFunction;
import net.servlex.saxon.functions.RemoveWebappFunction;
import net.servlex.saxon.functions.RepositoryFunction;

/**
 * Facade for all extensions functions in {@code org.expath.servlex.functions}.
 *
 * @author Florent Georges
 */
public class WebappFunctions
{
    /**
     * Register web:* functions on the Saxon processor object.
     * 
     * TODO: The functions should not be added statically.  They should be
     * visible only after an import (like for extension functions packaged into
     * a XAR file).  Would require overloading the Saxon URI resolver with a
     * similar mechanism here...
     * 
     * Actually, from SaxonRepository.registerExtensionFunctions() it seems
     * that extension functions are always registered, regardless of the import
     * statements...  Really?!?  Well, it seems there is no technical way to
     * achieve that with Saxon API...
     * 
     * @param procs The processors object to pass to the function objects which
     *     need one.
     * 
     * @param saxon The Saxon processor object to pass to the function objects
     *     which need one.
     * 
     * @param config The Servlex config object to pass to the function objects
     *     which need one.
     */
    public static void setup(Processors procs, Processor saxon, ServerConfig config)
    {
        // the request fields management functions
        saxon.registerExtensionFunction(new GetRequestFieldFunction());
        saxon.registerExtensionFunction(new GetRequestFieldNamesFunction());
        saxon.registerExtensionFunction(new SetRequestFieldFunction());
        // the session fields management functions
        saxon.registerExtensionFunction(new GetSessionFieldFunction());
        saxon.registerExtensionFunction(new GetSessionFieldNamesFunction());
        saxon.registerExtensionFunction(new SetSessionFieldFunction());
        // the webapp fields management functions
        saxon.registerExtensionFunction(new GetWebappFieldFunction());
        saxon.registerExtensionFunction(new GetWebappFieldNamesFunction());
        saxon.registerExtensionFunction(new SetWebappFieldFunction());
        // the server fields management functions
        saxon.registerExtensionFunction(new GetServerFieldFunction());
        saxon.registerExtensionFunction(new GetServerFieldNamesFunction());
        saxon.registerExtensionFunction(new SetServerFieldFunction());
        // the parse basic authentication function
        saxon.registerExtensionFunction(new ParseBasicAuthFunction(procs, saxon));
        // the parse header function
        saxon.registerExtensionFunction(new ParseHeaderValueFunction(procs, saxon));
        // the repo and webapps management functions
        saxon.registerExtensionFunction(new InstallEnabledFunction());
        saxon.registerExtensionFunction(new InstallFromCxanFunction());
        saxon.registerExtensionFunction(new InstallWebappFunction());
        saxon.registerExtensionFunction(new InstalledWebappsFunction());
        saxon.registerExtensionFunction(new RemoveWebappFunction());
        saxon.registerExtensionFunction(new ReloadWebappsFunction());
        saxon.registerExtensionFunction(new RepositoryFunction(config));
        // the config access and management functions
        saxon.registerExtensionFunction(new ConfigParamFunction());
        // the execute function
        saxon.registerExtensionFunction(new ExecuteFunction(procs, saxon));
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
