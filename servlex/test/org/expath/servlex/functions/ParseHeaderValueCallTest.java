/****************************************************************************/
/*  File:       ParseHeaderValueCallTest.java                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-12-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.functions;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.junit.Test;

/**
 * Test of ParseHeaderValueCall.
 *
 * @author Florent Georges
 * @date   2010-12-19
 */
public class ParseHeaderValueCallTest
{
//    @Test
//    public void testCall()
//            throws Exception
//    {
//        final Processor proc = new Processor(false);
//        proc.registerExtensionFunction(new ParseHeaderValueFunction(proc));
//        XQueryCompiler compiler = proc.newXQueryCompiler();
//        compiler.declareNamespace("web", ServlexConstants.WEBAPP_NS);
//        String value = "attachment; filename=\"fname.ext\"";
//        XQueryExecutable exec = compiler.compile("web:parse-header-value('" + value + "')");
//        XQueryEvaluator eval = exec.load();
//        Serializer serial = new Serializer();
//        serial.setOutputStream(System.err);
//        eval.setDestination(serial);
//        eval.evaluate();
//    }

    @Test
    public void reproduceTheAlgorithm()
    {
        String value = "attachment; filename=\"fname.ext\"";
        HeaderValueParser parser = new BasicHeaderValueParser();
        HeaderElement[] elems = BasicHeaderValueParser.parseElements(value, parser);
        for ( HeaderElement e : elems ) {
            System.err.println("element: " + e.getName());
            if ( e.getValue() != null ) {
                System.err.println("   : " + e.getValue());
            }
            for ( NameValuePair p : e.getParameters() ) {
                System.err.println("  param: " + p.getName());
                if ( p.getValue() != null ) {
                    System.err.println("     : " + p.getValue());
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
