/****************************************************************************/
/*  File:       SetSessionFieldCall.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-06-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.Logger;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.saxon.model.SaxonSequence;
import org.expath.servlex.tools.Properties;

/**
 * Set the value of a property in the session.
 *
 * @author Florent Georges
 */
public class SetSessionFieldCall
        extends ExtensionFunctionCall
{
    @Override
    public SequenceIterator call(SequenceIterator[] orig_params, XPathContext ctxt)
            throws XPathException
    {
        // the params
        FunParams params = new FunParams(orig_params, 2, 2);
        String name = params.asString(0, false);
        SequenceIterator value = orig_params[1];
        // log it
        LOG.debug(params.format(SetSessionFieldFunction.LOCAL_NAME).param(name).param(value).value());
        // setting the sequence in the session
        try {
            Properties props = Servlex.getSessionMap();
            Sequence seq = new SaxonSequence(value);
            props.set(name, seq);
            if ( LOG.isTraceEnabled() ) {
                LOG.trace("Use session map: " + props);
                LOG.trace("Set key: " + name + ", to: " + seq + " (" + value + ")");
            }
            return EmptyIterator.getInstance();
        }
        catch ( TechnicalException ex ) {
            throw new XPathException("Error in the Servlex session management", ex);
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(SetSessionFieldCall.class);
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
