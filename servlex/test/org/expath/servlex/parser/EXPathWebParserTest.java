/****************************************************************************/
/*  File:       EXPathWebParserTest.java                                    */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Storage;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.components.Component;
import org.expath.servlex.model.Application;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.Serializer;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.processors.XProcProcessor;
import org.expath.servlex.processors.XQueryProcessor;
import org.expath.servlex.processors.XSLTProcessor;
import org.expath.servlex.tools.ProcessorsMap;
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
     * 
     * @throws Exception if any error.
     */
    @Test
    public void testParseDescriptors_ok()
            throws Exception
    {
        ProcessorsMap fake = new FakeProcessorsMap();
        Package pkg = new FakePackage("test-pkg-name");
        // the descriptor
        ClassLoader loader = EXPathWebParserTest.class.getClassLoader();
        InputStream rsrc = loader.getResourceAsStream(FILTERS_ORDER_RSRC);
        Source desc = new StreamSource(rsrc);
        // the System Under Test
        EXPathWebParser sut = new EXPathWebParser(fake);
        // parse the descriptor
        Application result = sut.parseDescriptorFile(desc, pkg, EXPathWebParser.DESC_NS);
        System.err.println("RESULT: " + result);
        result.logApplication();
    }

    private static class FakeProcessorsMap
            extends ProcessorsMap
    {
        public FakeProcessorsMap()
                throws TechnicalException
                     , PackageException
        {
            super(new FakeProcessors(), null, null);
        }

        @Override
        public Processors getProcessors(String clazz)
                throws TechnicalException
        {
            throw new TechnicalException("Must not be called in test...!");
        }
    }

    private static class FakeProcessors
            implements Processors
    {
        @Override
        public XSLTProcessor getXSLT()
                throws TechnicalException
        {
            return new FakeXSLT();
        }

        @Override
        public XQueryProcessor getXQuery()
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public XProcProcessor getXProc()
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Serializer makeSerializer()
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public TreeBuilder makeTreeBuilder(String uri, String prefix)
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sequence emptySequence()
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sequence buildSequence(Iterable<Item> items)
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Document buildDocument(Source src)
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Item buildString(String value)
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Item buildBinary(byte[] value)
                throws TechnicalException
        {
            throw new UnsupportedOperationException();
        }
        
    }

    private static class FakeXSLT
            implements XSLTProcessor
    {
        @Override
        public Component makeTransform(String uri)
        {
            return null;
        }

        @Override
        public Component makeFunction(String uri, String ns, String localname)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Component makeTemplate(String uri, String ns, String localname)
        {
            throw new UnsupportedOperationException();
        }
        
    }
    private static class FakePackage
            extends Package
    {
        public FakePackage(String name)
        {
            super(null, new FakeResolver(), name, null, null, null, null);
        }
    }

    private static class FakeResolver
            extends Storage.PackageResolver
    {
        @Override
        public String getResourceName()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Source resolveResource(String string)
                throws PackageException, Storage.NotExistException
        {
            if ( "servlex.xml".equals(string) ) {
                return null;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Source resolveComponent(String string)
                throws PackageException, Storage.NotExistException
        {
            throw new UnsupportedOperationException();
        }
    }

    private static final String FILTERS_ORDER_RSRC =
            "org/expath/servlex/parser/descriptors/filters-order.xml";
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
