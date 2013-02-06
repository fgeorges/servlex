/****************************************************************************/
/*  File:       EXPathWebParserTest.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.io.File;
import java.util.Set;
import org.expath.pkg.repo.FileSystemStorage;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.model.AddressHandler;
import org.expath.servlex.model.Application;
import org.expath.servlex.model.Servlet;
import org.expath.servlex.model.Wrapper;
import org.junit.Test;
//import static org.junit.Assert.*;

/**
 * Test {@link EXPathWebParser}.
 *
 * For now, use the hard-coded repo "~/tmp/servlex/repo".  TODO: Create a local,
 * test-dedicated repo.
 *
 * @author Florent Georges
 * @date   2010-02-09
 */
public class EXPathWebParserTest
{
    /**
     * Test of parseDescriptors method, of class EXPathWebParser, when
     * everything is ok.
     */
    @Test
    public void testParseDescriptors_ok()
            throws ParseException
                 , PackageException
    {
        File repo_dir = new File(System.getProperty("user.home"), "tmp/servlex/repo");
        Storage storage = new FileSystemStorage(repo_dir);
        SaxonRepository repo = new SaxonRepository(storage);
        // the System Under Test
        EXPathWebParser sut = new EXPathWebParser(repo);
        Set<Application> result = sut.parseDescriptors();
        System.err.println("RESULT: " + result);
        for ( Application app : result ) {
            System.err.println("  APP: " + app.getName());
            for ( AddressHandler h : app.getHandlers() ) {
                System.err.println("    AddressHandler: " + h);
                if ( h instanceof Servlet ) {
                    Servlet s =  (Servlet) h;
                    Wrapper w = s.getWrapper();
                    if ( w != null ) {
                        System.err.println("      Wrapper: " + w);
                    }
                }
            }
        }
    }
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
