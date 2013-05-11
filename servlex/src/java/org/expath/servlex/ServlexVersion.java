/****************************************************************************/
/*  File:       ServlexVersion.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * The version of Servlex.
 *
 * @author Florent Georges
 * @date   2013-05-11
 */
public class ServlexVersion
{
    /**
     * Return the singleton instance (instantiate it if not already).
     */
    public static ServlexVersion getInstance()
            throws TechnicalException
    {
        if ( INSTANCE == null ) {
            INSTANCE = new ServlexVersion();
        }
        return INSTANCE;
    }

    /**
     * Return the Servlex implementation version number.
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Return the Servlex implementation code repository revision number.
     */
    public String getRevision()
    {
        return myRevision;
    }

    /**
     * Set the version and revision number by reading the properties file.
     */
    private ServlexVersion()
            throws TechnicalException
    {
        Properties props = new Properties();
        InputStream rsrc = ServerConfig.class.getResourceAsStream(VERSION_RSRC);
        if ( rsrc == null ) {
            throw new TechnicalException("Version properties file does not exist: " + VERSION_RSRC);
        }
        try {
            props.load(rsrc);
            rsrc.close();
        }
        catch ( IOException ex ) {
            throw new TechnicalException("Error reading the version properties: " + VERSION_RSRC, ex);
        }
        myVersion  = props.getProperty(VERSION_PROP);
        myRevision = props.getProperty(REVISION_PROP);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(ServlexVersion.class);

    /** The singleton instance. */
    private static ServlexVersion INSTANCE = null;

    /** The resource name of the version properties file. */
    private static final String VERSION_RSRC = "/org/expath/servlex/tools/version.properties";
    /** The property for the version number. */
    private static final String VERSION_PROP = "org.expath.servlex.version";
    /** The property for the revision number. */
    private static final String REVISION_PROP = "org.expath.servlex.revision";

    /** The Servlex implementation version. */
    private String myVersion;
    /** The Servlex implementation revision number. */
    private String myRevision;
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
