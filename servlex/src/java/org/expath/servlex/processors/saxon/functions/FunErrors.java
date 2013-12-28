/****************************************************************************/
/*  File:       FunErrors.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import org.expath.servlex.ServlexConstants;

/**
 * Utils for extension functions errors for Saxon.
 *
 * @author Florent Georges
 * @date   2013-09-16
 */
class FunErrors
{
    /**
     * Returns the XPath exception for web:not-found.
     */
    public static XPathException notFound(Exception cause)
    {
        return FunErrors.make("not-found", cause.getMessage(), cause);
    }

    /**
     * Returns the XPath exception for web:online-error.
     */
    public static XPathException onlineError(Exception cause)
    {
        return FunErrors.make("online-error", cause.getMessage(), cause);
    }

    /**
     * Returns the XPath exception for web:invalid-param.
     */
    public static XPathException invalidParam(String msg)
    {
        return FunErrors.make("invalid-param", msg);
    }

    /**
     * Returns the XPath exception for web:invalid-param.
     */
    public static XPathException invalidParam(String msg, Exception cause)
    {
        return FunErrors.make("invalid-param", msg, cause);
    }

    /**
     * Returns the XPath exception for web:cannot-install.
     */
    public static XPathException cannotInstall(Exception cause)
    {
        return FunErrors.make("cannot-install", cause.getMessage(), cause);
    }

    /**
     * Returns the XPath exception for web:invalid-context-root.
     */
    public static XPathException invalidContextRoot(Exception cause)
    {
        return FunErrors.make("invalid-context-root", cause.getMessage(), cause);
    }

    /**
     * Returns the XPath exception for web:invalid-config-list.
     */
    public static XPathException invalidConfigList(String msg)
    {
        return FunErrors.make("invalid-config-list", msg);
    }

    /**
     * Returns the XPath exception for web:already-installed.
     */
    public static XPathException alreadyInstalled(Exception cause)
    {
        return FunErrors.make("already-installed", cause.getMessage(), cause);
    }

    /**
     * Returns the XPath exception for web:unexpected.
     */
    public static XPathException unexpected(Exception ex)
    {
        return unexpected("Unexpected error", ex);
    }

    /**
     * Returns the XPath exception for web:unexpected.
     */
    public static XPathException unexpected(String msg, Exception ex)
    {
        return FunErrors.make("unexpected", msg, ex);
    }

    /**
     * Make an XPath exception with {@code code} in the web:* namespace.
     */
    private static XPathException make(String code, String msg)
    {
        XPathException  ex    = new XPathException(msg);
        StructuredQName qname = new StructuredQName(PREFIX, NS, code);
        ex.setErrorCodeQName(qname);
        return ex;
    }

    /**
     * Make an XPath exception with {@code code} in the web:* namespace.
     */
    private static XPathException make(String code, String msg, Exception cause)
    {
        XPathException  ex    = new XPathException(msg, cause);
        StructuredQName qname = new StructuredQName(PREFIX, NS, code);
        ex.setErrorCodeQName(qname);
        return ex;
    }

    private static final String PREFIX = ServlexConstants.WEBAPP_PREFIX;
    private static final String NS     = ServlexConstants.WEBAPP_NS;
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
