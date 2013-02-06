/****************************************************************************/
/*  File:       GetWebappFieldNamesCall.java                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.functions;

import java.util.Map;
import javax.servlet.ServletException;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;
import org.apache.log4j.Logger;
import org.expath.servlex.Servlex;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 * @date   2010-11-22
 */
public class GetWebappFieldNamesCall
        extends ExtensionFunctionCall
{
    @Override
    public SequenceIterator call(SequenceIterator[] params, XPathContext ctxt)
            throws XPathException
    {
        // num of params
        if ( params.length != 0 ) {
            throw new XPathException("There are actual params: " + params.length);
        }
        // returning the name of every fields in the webapp
        try {
            LOG.debug("Get webapp field names");
            Map<String, SequenceIterator> webapp = Servlex.getWebappMap();
            if ( webapp.size() == 0 ) {
                return EmptyIterator.getInstance();
            }
            Item[] items = new Item[webapp.size()];
            int i = 0;
            for ( String name : webapp.keySet() ) {
                items[i] = new StringValue(name);
                ++i;
            }
            return new ArrayIterator(items);
        }
        catch ( ServletException ex ) {
            throw new XPathException("Error in the Servlex webapp management", ex);
        }
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(GetWebappFieldCall.class);
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
