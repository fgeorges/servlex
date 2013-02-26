/****************************************************************************/
/*  File:       WebappFunctions.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-24                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import net.sf.saxon.s9api.Processor;
import org.expath.servlex.functions.GetRequestFieldFunction;
import org.expath.servlex.functions.GetRequestFieldNamesFunction;
import org.expath.servlex.functions.GetServerFieldFunction;
import org.expath.servlex.functions.GetServerFieldNamesFunction;
import org.expath.servlex.functions.GetSessionFieldFunction;
import org.expath.servlex.functions.GetSessionFieldNamesFunction;
import org.expath.servlex.functions.GetWebappFieldFunction;
import org.expath.servlex.functions.GetWebappFieldNamesFunction;
import org.expath.servlex.functions.ParseBasicAuthFunction;
import org.expath.servlex.functions.ParseHeaderValueFunction;
import org.expath.servlex.functions.SetRequestFieldFunction;
import org.expath.servlex.functions.SetServerFieldFunction;
import org.expath.servlex.functions.SetSessionFieldFunction;
import org.expath.servlex.functions.SetWebappFieldFunction;

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
     */
    public static void setup(Processor saxon)
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
        saxon.registerExtensionFunction(new ParseBasicAuthFunction(saxon));
        // the parse header function
        saxon.registerExtensionFunction(new ParseHeaderValueFunction(saxon));
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
