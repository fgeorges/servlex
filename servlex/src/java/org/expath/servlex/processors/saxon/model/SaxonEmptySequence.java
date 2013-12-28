/****************************************************************************/
/*  File:       SaxonEmptySequence.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.model;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import org.expath.servlex.processors.Element;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Sequence;

/**
 * An empty sequence for Saxon.
 * 
 * @author Florent Georges
 */
public class SaxonEmptySequence
        extends SaxonSequence
{
    private static final List<XdmItem> ourItemsList = new ArrayList<XdmItem>();
    private static final XdmValue ourXdmValue = new XdmValue(ourItemsList);
    private static final SaxonEmptySequence ourInstance = new SaxonEmptySequence();

    public static SaxonEmptySequence getInstance()
    {
        return ourInstance;
    }

    private SaxonEmptySequence()
    {
        super(ourXdmValue);
    }

    @Override
    public Item itemAt(int position)
    {
        return null;
    }

    @Override
    public Element elementAt(int position)
    {
        return null;
    }

    @Override
    public Sequence subSequence(int start)
    {
        return ourInstance;
    }

    // TODO: Should be package visible, but is used in XdmConnector (which
    // should use instead a method on SaxonHelper which should be move here...)
    @Override
    public XdmValue makeSaxonValue()
    {
        return ourXdmValue;
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
