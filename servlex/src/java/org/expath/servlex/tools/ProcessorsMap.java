/****************************************************************************/
/*  File:       ProcessorsMap.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.expath.pkg.repo.Repository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Processors;

/**
 * A map of {@link Processors} objects, with caching.
 *
 * @author Florent Georges
 * @date   2013-09-11
 */
public class ProcessorsMap
{
    public ProcessorsMap(Processors dflt, Repository repo, ServerConfig config)
    {
        myRepo = repo;
        myConfig = config;
        myDefault = dflt;
        myMap.put(dflt.getClass().getCanonicalName(), dflt);
    }

    public ProcessorsMap(String clazz, Repository repo, ServerConfig config)
            throws TechnicalException
    {
        myRepo = repo;
        myConfig = config;
        myDefault = getProcessors(clazz);
    }

    /**
     * Reserved for testing purposes.
     */
    protected ProcessorsMap()
    {
        // nothing
    }

    public Processors getDefault()
    {
        return myDefault;
    }

    public synchronized Processors getProcessors(String class_name)
            throws TechnicalException
    {
        // if in the map, return it
        Processors procs = myMap.get(class_name);
        if ( procs != null ) {
            return procs;
        }
        // if not, instantiate it
        try {
            // get the raw class object
            ClassLoader loader = ServerConfig.class.getClassLoader();
            Class<?> class_raw = loader.loadClass(class_name);
            // check it implements Processors
            if ( ! Processors.class.isAssignableFrom(class_raw) ) {
                String msg = "The processors implementation must implement Processors: ";
                throw new TechnicalException(msg + class_name);
            }
            // get the ctor
            Class<Processors> clazz = (Class<Processors>) class_raw;
            Constructor<Processors> ctor = clazz.getConstructor(Repository.class, ServerConfig.class);
            // instantiate
            procs = ctor.newInstance(myRepo, myConfig);
            myMap.put(class_name, procs);
            return procs;
        }
        catch ( ClassNotFoundException ex ) {
            String msg = "The processors implementation class not found: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( NoSuchMethodException ex ) {
            String msg = "The processors implementation must have a constructor(Repository,ServerConfig): ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( SecurityException ex ) {
            String msg = "Servlex must have access to the processors implementation: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( InstantiationException ex ) {
            String msg = "The processors implementation must be instantiable: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( IllegalAccessException ex ) {
            String msg = "Servlex must have access to the processors implementation: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( IllegalArgumentException ex ) {
            String msg = "The processors implementation constructor must accept the Repository and ServerConfig: ";
            throw new TechnicalException(msg + class_name, ex);
        }
        catch ( InvocationTargetException ex ) {
            String msg = "The processors implementation constructor threw an exception: ";
            throw new TechnicalException(msg + class_name, ex);
        }
    }

    /** The map with all processors implementations. */
    private Map<String, Processors> myMap = new HashMap<String, Processors>();
    /** The default processors. */
    private Processors myDefault;
    /** The repo to use to instantiate new Processors objects. */
    private Repository myRepo;
    /** The server config to use to instantiate new Processors objects. */
    private ServerConfig myConfig;
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
