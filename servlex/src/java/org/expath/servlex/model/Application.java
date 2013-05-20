/****************************************************************************/
/*  File:       Application.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2009-12-12                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.Package;
import org.expath.servlex.runtime.Invocation;
import org.expath.servlex.ServlexException;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.tools.SequenceProperties;


/**
 * Represent a webapp, with its metadata, its resources and its servlets.
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public class Application
{
    /**
     * Create a new application, based on its name, title, package and properties.
     */
    public Application(String name, String title, Package pkg, Processors procs)
    {
        myName       = name;
        myTitle      = title;
        myPkg        = pkg;
        myProcessors = procs;
        myProperties = new SequenceProperties("web:", procs);
    }

    /**
     * Return the application name (the {@code @abbrev} from {@code expath-web.xml}).
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Return the application title (the {@code title} element from {@code expath-web.xml}).
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Return the package used for the webapp.
     */
    public Package getPackage()
    {
        return myPkg;
    }

    /**
     * Return the application handlers (the resources and servlets).
     */
    public List<AddressHandler> getHandlers()
    {
        return myHandlers;
    }

    /**
     * Add one handler to the application (either a resource or a servlet).
     * 
     * The order they are added to the application is relevant.  When resolving
     * a path, the code tries each in sequence, until it finds a match.  So the
     * first handler added is the first one tries to match a path.
     */
    public void addHandler(AddressHandler h)
    {
        h.setApplication(this);
        myHandlers.add(h);
    }

    /**
     * Return the application processors object.
     */
    public Processors getProcessors()
    {
        return myProcessors;
    }

    /**
     * Return the application properties object.
     * 
     * This object is used to store (and retrieve) properties at the level of
     * a specific application.
     */
    public SequenceProperties getProperties()
    {
        return myProperties;
    }

    /**
     * Find the first component matching the path.
     *
     * The component is either a servlet or a resource.  A corresponding
     * invocation object is returned, which can be used to get the response
     * corresponding to the actual request.
     */
    public Invocation resolve(String path, String method, RequestConnector connector)
            throws ServlexException
    {
        for ( AddressHandler h : myHandlers ) {
            Invocation invoc = h.resolve(path, method, connector);
            if ( invoc != null ) {
                return invoc;
            }
        }
        LOG.error("404: Page not found: " + path);
        throw new ServlexException(404, "Page not found");
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(Application.class);

    private String myName;
    private String myTitle;
    private Package myPkg;
    private Processors myProcessors;
    private SequenceProperties myProperties;
    private List<AddressHandler> myHandlers = new ArrayList<AddressHandler>();
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
