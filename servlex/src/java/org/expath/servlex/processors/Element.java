/****************************************************************************/
/*  File:       Element.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-05                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 * Represents an abstract element node.
 *
 * @author Florent Georges
 */
public interface Element
        extends Item
{
    /**
     * The element name.
     */
    public QName name();

    /**
     * The element base URI.
     */
    public URI baseUri();

    /**
     * The attributes of the element.
     */
    public Iterator<Attribute> attributes();

    /**
     * The children of this element, there must only be elements.
     * 
     * If there is any other kind of node than elements in the children, this
     * is an error (except for whitespace-only text nodes and comment nodes,
     * which are discarded).
     */
    public Iterator<Element> elements();

    /**
     * The children of this element.
     */
    public Iterator<Item> children();
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
