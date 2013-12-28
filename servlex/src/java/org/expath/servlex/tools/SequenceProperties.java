/****************************************************************************/
/*  File:       SequenceProperties.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-05-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;

/**
 * A {@link Properties} implementation for {@link Sequence} values.
 *
 * @author Florent Georges
 */
public class SequenceProperties
        extends Properties<Item>
{
    /**
     * Constructs a new Properties object with a private property name prefix and processors.
     */
    public SequenceProperties(String private_prefix, Processors procs)
    {
        super(private_prefix);
        myProcs = procs;
    }

    /**
     * Get a property value.
     * 
     * Never return null (return an empty sequence instead).
     */
    @Override
    public Sequence get(String key)
            throws TechnicalException
    {
        Iterable<Item> v = super.get(key);
        if ( v == null ) {
            return myProcs.emptySequence();
        }
        else if ( ! (v instanceof Sequence) ) {
            throw new TechnicalException("Not a sequence?!?: " + v);
        }
        else {
            return (Sequence) v;
        }
    }

    @Override
    protected String valueAsString(String key, Item value)
            throws TechnicalException
    {
        // TODO: Check if the item actually is a string item?
        return value.stringValue();
    }

    @Override
    protected Sequence valueFromString(String value)
            throws TechnicalException
    {
        Item string = myProcs.buildString(value);
        return string.asSequence();
    }

    @Override
    public Iterable<Item> set(String key, Iterable<Item> value)
            throws TechnicalException
    {
        if ( ! (value instanceof Sequence) ) {
            throw new TechnicalException("Not a sequence?!?: " + value);
        }
        return super.set(key, value);
    }

    /** The processors to use. */
    private Processors myProcs;
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
