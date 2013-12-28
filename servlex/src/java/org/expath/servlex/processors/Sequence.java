/****************************************************************************/
/*  File:       Sequence.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-30                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import org.expath.servlex.TechnicalException;

/**
 * Represents an abstract XDM item.
 *
 * @author Florent Georges
 * @date   2013-04-30
 */
public interface Sequence
        extends Iterable<Item>
{
    /**
     * Return the item at {@code position}, first item is at position 0.
     * 
     * Return null if {@code position} is outside the sequence.
     */
    public Item itemAt(int position);

    /**
     * Return the element at {@code position}, first item is at position 0.
     * 
     * Return null if {@code position} is outside the sequence.  The sequence
     * can be of any type, but the item at {@code position} (if any) must be an
     * element node (if there is such an item but it is not an element, this is
     * en error).
     * 
     * If the item at that position is exactly one document node, and its
     * children are exactly one element node (ignoring whitespace-only text
     * nodes), then it is returned directly instead.
     */
    public Element elementAt(int position)
            throws TechnicalException;

    /**
     * Return the sub-sequence starting at the item at position {@code start}.
     * 
     * Position starts at 0.  So if {@code start} is 1, returns the same
     * sequence without the first item.  If {@code start} is outside the
     * boundaries of the sequence, an empty sequence is returned.
     * 
     * If the sequence is exactly one document node, then its children nodes
     * are used directly instead.
     */
    public Sequence subSequence(int start);
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
