/****************************************************************************/
/*  File:       Properties.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;

/**
 * Properties (either server, webapp, session or request properties).
 *
 * @author Florent Georges
 * @date   2013-02-26
 */
public class Properties
{
    /**
     * Constructs a new Properties object without any private property name prefix.
     */
    public Properties(Processors procs)
    {
        this(null, procs);
    }

    /**
     * Constructs a new Properties object with a private property name prefix.
     */
    public Properties(String private_prefix, Processors procs)
    {
        myPrivatePrefix = private_prefix;
        myProcs = procs;
        myMap = new HashMap<String, Sequence>();
    }

    /**
     * Get a property value.
     * 
     * Never return null.
     */
    public Sequence get(String key)
            throws TechnicalException
    {
        Sequence value = myMap.get(key);
        if ( value == null ) {
            value = myProcs.emptySequence();
        }
        if ( LOG.isDebugEnabled() ) {
            logGetValue(key, value);
        }
        return value;
    }

    /**
     * Get a private property value (return it as a String).
     * 
     * Return null if not set.
     */
    public String getPrivate(String key)
            throws TechnicalException
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Properties.getPrivate: " + key);
        }
        if ( key == null ) {
            throw new NullPointerException("Key is null");
        }
        if ( ! key.startsWith(myPrivatePrefix) ) {
            throw new TechnicalException("Key does not start with the private prefix (" + myPrivatePrefix + "): " + key);
        }
        Sequence sequence = get(key);
        Item item = sequence.itemAt(0);
        if ( item == null ) {
            return null;
        }
        if ( sequence.itemAt(1) != null ) {
            throw new TechnicalException("The value of " + key + " contains more than one item.");
        }
        // TODO: Check if the item actually is a string item?
        return item.stringValue();
    }

    private void logGetValue(String key, Sequence value)
            throws TechnicalException
    {
        LOG.debug("Properties.get: " + key + ", " + value);
        if ( LOG.isTraceEnabled() ) {
            for ( Item i : value ) {
                LOG.trace("          .get: " + i);
            }
        }
    }

    /**
     * Set a property value (it is an error if the property name starts with the private prefix).
     */
    public Sequence set(String key, Sequence value)
            throws TechnicalException
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Properties.set: " + key + ", " + value);
        }
        if ( key == null ) {
            throw new NullPointerException("Key is null");
        }
        if ( key.startsWith(myPrivatePrefix) ) {
            throw new TechnicalException("Key starts with the private prefix (" + myPrivatePrefix + "): " + key);
        }
        return myMap.put(key, value);
    }

    /**
     * Set a private value (the property name must start with the private prefix).
     */
    public Sequence setPrivate(String key, String value)
            throws TechnicalException
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Properties.setPrivate: " + key + ", " + value);
        }
        if ( key == null ) {
            throw new NullPointerException("Key is null");
        }
        if ( value == null ) {
            throw new NullPointerException("Value is null");
        }
        if ( ! key.startsWith(myPrivatePrefix) ) {
            throw new TechnicalException("Key does not start with the private prefix (" + myPrivatePrefix + "): " + key);
        }
        Item string = myProcs.buildString(value);
        return myMap.put(key, string.asSequence());
    }

    /**
     * Return the number of properties.
     */
    public int size()
    {
        return myMap.size();
    }

    /**
     * Return all the property names, as a set.
     */
    public Sequence keys()
            throws TechnicalException
    {
        if ( size() == 0 ) {
            return myProcs.emptySequence();
        }
        List items = new ArrayList<Item>(size());
        for ( String name : myMap.keySet() ) {
            Item i = myProcs.buildString(name);
            items.add(i);
        }
        return myProcs.buildSequence(items);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(Servlex.class);
    /** The private property name prefix, if any. */
    private String myPrivatePrefix;
    /** The store map. */
    private Map<String, Sequence> myMap;
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
