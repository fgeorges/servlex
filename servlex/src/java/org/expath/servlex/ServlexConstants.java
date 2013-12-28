/****************************************************************************/
/*  File:       ServlexConstants.java                                       */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2010-06-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 */
public class ServlexConstants
{
    /** The default prefix for the webapp namespace. */
    public static final String WEBAPP_PREFIX = "web";
    /** The webapp namespace. */
    public static final String WEBAPP_NS     = "http://expath.org/ns/webapp";
    /** The webapp descriptor namespace. */
    public static final String DESCRIPTOR_NS = "http://expath.org/ns/webapp/descriptor";
    /** The private webapp namespace. */
    public static final String PRIVATE_NS    = "http://expath.org/ns/webapp/private";

    /** The name of the XProc option with the name of the error code. */
    public static final String OPTION_CODE_NAME = "code-name";
    /** The name of the XProc option with the namespace of the error code. */
    public static final String OPTION_CODE_NS   = "code-namespace";
    /** The name of the XProc option with the error message. */
    public static final String OPTION_MESSAGE   = "message";

    /** The prefix for the private webapp properties. */
    public static final String PRIVATE_PROPS_PREFIX  = "web:";
    /** The name of the property with a unique ID for the request. */
    public static final String PROP_REQUEST_ID       = "web:request-id";
    /** The name of the property with the product name and version. */
    public static final String PROP_PRODUCT          = "web:product";
    /** The name of the property with the product name. */
    public static final String PROP_PRODUCT_NAME     = "web:product-name";
    /** The name of the property with the product version. */
    public static final String PROP_PRODUCT_VERSION  = "web:product-version";
    /** The name of the property with the product revision. */
    public static final String PROP_PRODUCT_REVISION = "web:product-revision";
    /** The name of the property with the product name and version, formatted in HTML. */
    public static final String PROP_PRODUCT_HTML     = "web:product-html";
    /** The name of the property with the vendor description. */
    public static final String PROP_VENDOR           = "web:vendor";
    /** The name of the property with the vendor description, formatted in HTML. */
    public static final String PROP_VENDOR_HTML      = "web:vendor-html";

    /** The system property name for the processors implementation class. */
    public static final String PROCESSORS_PROPERTY      = "org.expath.servlex.processors";
    /** The system property name for the repo directory. */
    public static final String REPO_DIR_PROPERTY        = "org.expath.servlex.repo.dir";
    /** The system property name for the repo classpath prefix. */
    public static final String REPO_CP_PROPERTY         = "org.expath.servlex.repo.classpath";
    /** The system property name for the log directory. */
    public static final String PROFILE_DIR_PROPERTY     = "org.expath.servlex.profile.dir";
    /** The system property name for whether logging HTTP entity content. */
    public static final String TRACE_CONTENT_PROPERTY   = "org.expath.servlex.trace.content";
    /** The system property name for whether logging HTTP entity content. */
    public static final String DEFAULT_CHARSET_PROPERTY = "org.expath.servlex.default.charset";

    /** The default processors implementation class to use. */
    public static final String DEFAULT_PROCESSORS
            = "org.expath.servlex.processors.saxon.SaxonCalabash";
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
