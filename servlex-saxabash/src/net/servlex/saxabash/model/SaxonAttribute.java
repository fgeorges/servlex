/****************************************************************************/
/*  File:       SaxonAttribute.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxabash.model;

import javax.xml.namespace.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Attribute;

/**
 * An element for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonAttribute
        extends SaxonItem
        implements Attribute
{
    public SaxonAttribute(XdmNode attr)
            throws TechnicalException
    {
        super(attr);
        if ( attr == null ) {
            throw new NullPointerException("Underlying node is null for Saxon attribute");
        }
        XdmNodeKind kind = attr.getNodeKind();
        if ( kind != XdmNodeKind.ATTRIBUTE ) {
            throw new TechnicalException("Node is not an attribute, for Saxon attribute: " + kind);
        }
        myAttr = attr;
    }

    @Override
    public QName name()
    {
        net.sf.saxon.s9api.QName name = myAttr.getNodeName();
        String ns     = name.getNamespaceURI();
        String local  = name.getLocalName();
        String prefix = name.getPrefix();
        return new QName(ns, local, prefix);
    }

    private XdmNode myAttr;
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
