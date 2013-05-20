/****************************************************************************/
/*  File:       Properties.java                                             */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-02-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.expath.servlex.Servlex;
import org.expath.servlex.TechnicalException;

/**
 * Properties (either server, webapp, session or request properties).
 *
 * @author Florent Georges
 * @date   2013-02-26
 */
public abstract class Properties<Value>
{
    /**
     * Constructs a new Properties object with a private property name prefix.
     */
    public Properties(String private_prefix)
    {
        myPrivatePrefix = private_prefix;
        myMap = new HashMap<String, Iterable<Value>>();
    }

    /**
     * Get a property value.
     */
    public Iterable<Value> get(String key)
            throws TechnicalException
    {
        Iterable<Value> value = myMap.get(key);
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
        Iterable<Value> value = get(key);
        Iterator<Value> iter = value.iterator();
        if ( ! iter.hasNext() ) {
            return null;
        }
        Value first = iter.next();
        if ( iter.hasNext() ) {
            throw new TechnicalException("The value of " + key + " contains more than one item (second is: " + iter.next() + ".");
        }
        return valueAsString(key, first);
    }

    protected abstract String valueAsString(String key, Value value)
            throws TechnicalException;

    protected abstract Iterable<Value> valueFromString(String value)
            throws TechnicalException;

    private void logGetValue(String key, Iterable<Value> value)
            throws TechnicalException
    {
        LOG.debug("Properties.get: " + key + ", " + value);
        if ( LOG.isTraceEnabled() ) {
            for ( Value v : value ) {
                LOG.trace("          .get: " + v);
            }
        }
    }

    /**
     * Set a property value (it is an error if the property name starts with the private prefix).
     */
    public Iterable<Value> set(String key, Iterable<Value> value)
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
    public Iterable<Value> setPrivate(String key, String value)
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
        Iterable<Value> v = valueFromString(value);
        return myMap.put(key, v);
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
    public Set<String> keys()
            throws TechnicalException
    {
        return myMap.keySet();
    }

    /** The logger. */
    protected static final Logger LOG = Logger.getLogger(Servlex.class);
    /** The private property name prefix, if any. */
    private String myPrivatePrefix;
    /** The store map. */
    private Map<String, Iterable<Value>> myMap;
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
