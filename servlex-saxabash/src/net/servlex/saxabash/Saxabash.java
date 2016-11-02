/****************************************************************************/
/*  File:       Saxabash.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2016-04-28                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2016 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxabash;

import com.xmlcalabash.core.XProcConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.servlex.saxon.Saxon;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.XProcProcessor;

/**
 * XSLT, XQuery and XProc processors based on Saxon and Calabash.
 * 
 * The "XSLT version" used here is the XSLT version number (a string, either
 * "2.0" or "3.0") to use for the "wrapper stylesheets" used for function and
 * names template components.
 *
 * @author Florent Georges
 */
public class Saxabash
    extends Saxon
{
    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    public Saxabash(Repository repo, ServerConfig config)
            throws TechnicalException
    {
        super(repo, config);
        List<String> info = new ArrayList<>();
        info.add("this is a Saxabash instance");
        info.addAll(Arrays.asList(myInfo));
        try {
            myXProc = new CalabashXProc(getSaxon(), getRepository(), config, this);
            info.add("calabash processor: " + myXProc);
            info.add("calabash version: " + XProcConstants.XPROC_VERSION);
        }
        catch ( PackageException ex ) {
            throw new TechnicalException("Error initializing the saxon and calabash processors", ex);
        }
        myInfo = info.toArray(new String[]{});
    }

    @Override
    public XProcProcessor getXProc()
    {
        return myXProc;
    }

    private final CalabashXProc myXProc;
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
