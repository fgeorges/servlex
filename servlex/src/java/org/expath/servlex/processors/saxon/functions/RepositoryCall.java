/****************************************************************************/
/*  File:       RepositoryCall.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.ObjectValue;
import org.apache.log4j.Logger;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.WebRepository;

/**
 * Implements web:installed-webapps().
 * 
 * The XPath signatures:
 *
 *     web:installed-webapps() as xs:string*
 * 
 * TODO: Maybe return more information about each webapp, as XML elements.
 * 
 * @author Florent Georges
 * @date   2013-09-15
 */
public class RepositoryCall
        extends ExtensionFunctionCall
{
    public RepositoryCall(ServerConfig config)
    {
        myConfig = config;
    }

    @Override
    public SequenceIterator call(SequenceIterator[] orig_params, XPathContext ctxt)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 0, 0);
        // log it
        LOG.debug(params.format(RepositoryFunction.LOCAL_NAME).value());
        // do it
        WebRepository  repo   = myConfig.getRepository();
        ObjectValue    object = new ObjectValue(repo);
        return SingletonIterator.makeIterator(object);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(RepositoryCall.class);

    /** The repository. */
    private ServerConfig myConfig;
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
