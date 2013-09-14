/****************************************************************************/
/*  File:       Resource.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-08-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.runtime.ResourceInvocation;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.RequestConnector;


/**
 * A resource in an application, bound to a URI pattern.
 *
 * @author Florent Georges
 * @date   2010-08-17
 */
public class Resource
        extends AddressHandler
{
    public Resource(Pattern url_pattern, String java_regex, String rewrite, String type)
    {
        super(url_pattern);
        myType = type;
        myJavaRegex = java_regex;
        myRewrite = rewrite;
    }

    public String getType()
    {
        return myType;
    }

    @Override
    public void logApplication(Logger log)
    {
        super.logApplication(log);
        log.debug("   (is a Resource):");
        log.debug("      type   : " + myType);
        log.debug("      regex  : " + myJavaRegex);
        log.debug("      rewrite: " + myRewrite);
    }

    @Override
    protected Invocation makeInvocation(String path, String method, RequestConnector connector)
            throws ServlexException
    {
        if ( ! method.toLowerCase().equals("get") ) {
            LOG.error("405: Method not allowed: " + method + " (Allow: GET)");
            ServlexException ex = new ServlexException(405, "Method not allowed");
            ex.addHeader("Allow", "GET");
            throw ex;
        }
        return new ResourceInvocation(this, path, connector, myJavaRegex, myRewrite);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(Application.class);

    private String myType;
    private String myJavaRegex;
    private String myRewrite;
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
