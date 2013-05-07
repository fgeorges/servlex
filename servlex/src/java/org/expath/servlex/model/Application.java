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
import org.expath.servlex.tools.Properties;


/**
 * Represent a webapp, with its metadata, its resources and its servlets.
 *
 * @author Florent Georges
 * @date   2009-12-12
 */
public class Application
{
    /**
     * TODO: ...
     */
    public Application(String name, String title, Package pkg, Properties props)
    {
        myName  = name;
        myTitle = title;
        myPkg   = pkg;
        myProps = props;
    }

    public String getName()
    {
        return myName;
    }

    public String getTitle()
    {
        return myTitle;
    }

    public Package getPackage()
    {
        return myPkg;
    }

    public List<AddressHandler> getHandlers()
    {
        return myHandlers;
    }

    public void addHandler(AddressHandler h)
    {
        h.setApplication(this);
        myHandlers.add(h);
    }

    public Properties getProperties()
    {
        return myProps;
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
    private Properties myProps;
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
