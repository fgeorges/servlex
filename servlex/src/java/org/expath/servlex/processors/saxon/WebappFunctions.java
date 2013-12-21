/****************************************************************************/
/*  File:       WebappFunctions.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-24                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon;

import net.sf.saxon.s9api.Processor;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.processors.saxon.functions.GetRequestFieldFunction;
import org.expath.servlex.processors.saxon.functions.GetRequestFieldNamesFunction;
import org.expath.servlex.processors.saxon.functions.GetServerFieldFunction;
import org.expath.servlex.processors.saxon.functions.GetServerFieldNamesFunction;
import org.expath.servlex.processors.saxon.functions.GetSessionFieldFunction;
import org.expath.servlex.processors.saxon.functions.GetSessionFieldNamesFunction;
import org.expath.servlex.processors.saxon.functions.GetWebappFieldFunction;
import org.expath.servlex.processors.saxon.functions.GetWebappFieldNamesFunction;
import org.expath.servlex.processors.saxon.functions.ParseBasicAuthFunction;
import org.expath.servlex.processors.saxon.functions.ParseHeaderValueFunction;
import org.expath.servlex.processors.saxon.functions.SetRequestFieldFunction;
import org.expath.servlex.processors.saxon.functions.SetServerFieldFunction;
import org.expath.servlex.processors.saxon.functions.SetSessionFieldFunction;
import org.expath.servlex.processors.saxon.functions.SetWebappFieldFunction;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.saxon.functions.ConfigParamFunction;
import org.expath.servlex.processors.saxon.functions.ExecuteFunction;
import org.expath.servlex.processors.saxon.functions.InstallEnabledFunction;
import org.expath.servlex.processors.saxon.functions.InstallFromCxanFunction;
import org.expath.servlex.processors.saxon.functions.InstallWebappFunction;
import org.expath.servlex.processors.saxon.functions.InstalledWebappsFunction;
import org.expath.servlex.processors.saxon.functions.RepositoryFunction;
import org.expath.servlex.processors.saxon.functions.TMP_ZipEntryAsXmlFunction;

/**
 * Facade for all extensions functions in {@code org.expath.servlex.functions}.
 *
 * @author Florent Georges
 * @date   2013-02-24
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
        // TODO: FIXME: The temporary ZIP entry as XML function.
        saxon.registerExtensionFunction(new TMP_ZipEntryAsXmlFunction(saxon));
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
