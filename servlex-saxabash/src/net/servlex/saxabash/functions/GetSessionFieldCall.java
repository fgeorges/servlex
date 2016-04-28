/****************************************************************************/
/*  File:       GetSessionFieldCall.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-06-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxabash.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.tools.Log;
import org.expath.servlex.tools.SequenceProperties;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 */
public class GetSessionFieldCall
        extends ExtensionFunctionCall
{
    @Override
    public Sequence call(XPathContext ctxt, Sequence[] orig_params)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 1, 1);
        String name = params.asString(0, false);
        // log it
        LOG.debug(params.format(GetSessionFieldFunction.LOCAL_NAME).param(name).value());
        // getting the sequence in the session
        try {
            SequenceProperties props = Servlex.getSessionMap();
            org.expath.servlex.processors.Sequence seq = props.get(name);
            if ( LOG.trace() ) {
                LOG.trace("Use session map: " + props);
                LOG.trace("Get key: " + name + ", is: " + seq);
            }
            return FunReturn.value(seq);
        }
        catch ( TechnicalException ex ) {
            throw new XPathException("Error in the Servlex session management", ex);
        }
    }

    /** The logger. */
    private static final Log LOG = new Log(GetSessionFieldCall.class);
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
