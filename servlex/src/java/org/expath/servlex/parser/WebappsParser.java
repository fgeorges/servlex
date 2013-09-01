/****************************************************************************/
/*  File:       WebappsParser.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-01                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import org.expath.pkg.repo.Repository;

/**
 * Facade class for this package, to parse the webapps.xml configuration file.
 *
 * @author Florent Georges
 * @date   2013-09-01
 */
public class WebappsParser
{
    public WebappsParser(Repository repo)
    {
        
    }

    /** The webapps.xml configuration file path, from the repo root directory. */
    private static final String WEBAPPS_FILE_PATH = ".expath-web/webapps.xml";

    /** The repository. */
    private Repository myRepo;
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
