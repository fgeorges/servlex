/****************************************************************************/
/*  File:       SaxonDocument.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-30                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxabash.model;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Element;
import net.servlex.saxabash.SaxonHelper;

/**
 * A document for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonDocument
        extends SaxonItem
        implements Document
{
    public SaxonDocument(XdmNode doc)
            throws TechnicalException
    {
        super(doc);
        if ( doc == null ) {
            throw new NullPointerException("Underlying node is null for Saxon document");
        }
        XdmNodeKind kind = doc.getNodeKind();
        if ( kind != XdmNodeKind.DOCUMENT ) {
            throw new TechnicalException("Node is not a document, for Saxon document: " + kind);
        }
        myDoc = doc;
    }

    @Override
    public Element getRootElement()
            throws TechnicalException
    {
        XdmNode elem = SaxonHelper.getDocumentRootElement(this);
        return new SaxonElement(elem);
    }

    // TODO: Should be package visible, but is used in CalabashHelper (which
    // should be moved in this package...)
    public XdmNode getSaxonNode()
    {
        return myDoc;
    }

    SaxonSequence getChildren()
    {
        XdmSequenceIterator children = myDoc.axisIterator(Axis.CHILD);
        return new SaxonSequence(children);
    }

    private XdmNode myDoc;
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
