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
import org.expath.servlex.TechnicalException;

/**
 * Properties (either server, webapp, session or request properties).
 * 
 * A set of properties, of a given type.  In addition to those properties, this
 * object also stores "private" properties, always of type string, that all
 * begin with the same prefix (keys of non-private properties in this object
 * cannot start with the same prefix.  Interfaces providing access to the
 * properties to the outside world (like extension functions for XPath) should
 * not provide access to the private properties.
 *
 * @author Florent Georges
 * 
 * @param <Value> The type of value stored by this property set (private
 *     properties are always strings).
 */
public abstract class Properties<Value>
{
    /**
     * Constructs a new Properties object with a private property name prefix.
     * 
     * @param private_prefix The prefix to use for the keys of private properties.
     */
    public Properties(String private_prefix)
    {
        myPrivatePrefix = private_prefix;
        myMap = new HashMap<>();
    }

    /**
     * Get a property value.
     * 
     * @return The value of the property with the key {@code key}. Or null if
     *     there is no such property.
     * 
     * @param key The key of the property to retrieve.
     * 
     * @throws TechnicalException In case of any error.
     */
    public Iterable<Value> get(String key)
            throws TechnicalException
    {
        Iterable<Value> value = myMap.get(key);
        logValue("get", key, value);
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
        String result = valueAsString(key, first);
        logValue("getPrivate", key, result);
        return result;
    }

    protected abstract String valueAsString(String key, Value value)
            throws TechnicalException;

    protected abstract Iterable<Value> valueFromString(String value)
            throws TechnicalException;

    private void logValue(String op, String key, Iterable<Value> value)
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Properties." + op + ": " + key + ", " + value);
            if ( LOG.isTraceEnabled() ) {
                for ( Value v : value ) {
                    LOG.trace("          ." + op + ": " + v);
                }
            }
        }
    }

    private void logValue(String op, String key, String value)
    {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Properties." + op + ": " + key + ", " + value);
        }
    }

    /**
     * Set a property value (it is an error if the property name starts with the private prefix).
     */
    public Iterable<Value> set(String key, Iterable<Value> value)
            throws TechnicalException
    {
        logValue("set", key, value);
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
        logValue("setPrivate", key, value);
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
    protected static final Logger LOG = Logger.getLogger(Properties.class);
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
