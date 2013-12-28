/****************************************************************************/
/*  File:       RequestParserTest.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.util.ArrayList;
import java.util.List;
import org.expath.servlex.model.Servlet;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.TreeBuilder;
import org.expath.servlex.test.HttpServletRequestMock;
import org.expath.servlex.test.StringTreeBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the request parser.
 *
 * @author Florent Georges
 */
public class RequestParserTest
{
    @Test
    public void simplestTest()
            throws Exception
    {
        // the HTTP request mock
        HttpServletRequestMock req = new HttpServletRequestMock();
        req.setContentType(null);
        req.setContextPath("/servlex");
        req.setMethod("GET");
        req.setQueryString("who=me");
        req.setRequestURL("http://example.org:1234/servlex/manager/remove/filters");
        req.setServletPath("");
        req.setHeader("x-simple", "value");
        req.setHeader("x-double", "value-1", "value-2");
        req.setParameter("simple", "value");
        req.setParameter("double", "value-1", "value-2");
        // the path
        String path = "/remove/filters";
        // the System Under Test
        RequestParser sut = new RequestParser(req, path, "manager", null);
        // the regex matcher
        RegexPattern pattern = new RegexPattern("/remove/([a-z]+)", null);
        RegexMatcher matcher = pattern.matcher(path);
        Assert.assertTrue("The path must match the regex", matcher.matches());
        sut.setMatcher(matcher);
        // the servlet mock
        Servlet servlet = new Servlet("remove-it", null, pattern, new String[]{ "webapp" });
        sut.setServlet(servlet);
        // the tree builder spy
        TreeBuilder builder = new StringTreeBuilder();
        // the output parameter
        List<Item> items = new ArrayList<>();
        // test it
        sut.parse(builder, items, false);
        // check
        String expected = "start elem: request\n" +
                "attribute: servlet: remove-it\n" +
                "attribute: path: /remove/filters\n" +
                "attribute: method: get\n" +
                "start content\n" +
                "text elem: uri: http://example.org:1234/servlex/manager/remove/filters?who=me\n" +
                "text elem: authority: http://example.org:1234\n" +
                "text elem: context-root: /servlex/manager\n" +
                "start elem: path\n" +
                "start content\n" +
                "text elem: part: /remove/\n" +
                "start elem: match\n" +
                "attribute: name: webapp\n" +
                "start content\n" +
                "characters: filters\n" +
                "end elem\n" +
                "end elem\n" +
                "start elem: param\n" +
                "attribute: name: simple\n" +
                "attribute: value: value\n" +
                "end elem\n" +
                "start elem: param\n" +
                "attribute: name: double\n" +
                "attribute: value: value-1\n" +
                "end elem\n" +
                "start elem: param\n" +
                "attribute: name: double\n" +
                "attribute: value: value-2\n" +
                "end elem\n" +
                "start elem: header\n" +
                "attribute: name: x-simple\n" +
                "attribute: value: value\n" +
                "end elem\n" +
                "start elem: header\n" +
                "attribute: name: x-double\n" +
                "attribute: value: value-1\n" +
                "end elem\n" +
                "start elem: header\n" +
                "attribute: name: x-double\n" +
                "attribute: value: value-2\n" +
                "end elem\n" +
                "end elem\n";
        Assert.assertEquals("Tree builder events", expected, builder.toString());
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
