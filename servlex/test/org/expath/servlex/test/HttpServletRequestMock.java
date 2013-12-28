/****************************************************************************/
/*  File:       HttpServletRequestMock.java                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * A way to create a mock request, for test purposes.
 *
 * @author Florent Georges
 */
public class HttpServletRequestMock
        implements HttpServletRequest
{
    @Override
    public String getContentType() {
        return myContentType;
    }
    public void setContentType(String type) {
        myContentType = type;
    }

    @Override
    public String getContextPath() {
        return myContextPath;
    }
    public void setContextPath(String path) {
        myContextPath = path;
    }

    @Override
    public String getMethod() {
        return myMethod;
    }
    public void setMethod(String method) {
        myMethod = method;
    }

    @Override
    public String getQueryString() {
        return myQueryString;
    }
    public void setQueryString(String query) {
        myQueryString = query;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(myRequestURL);
    }
    public void setRequestURL(String url) {
        myRequestURL = url;
    }

    @Override
    public String getServletPath() {
        return myServletPath;
    }
    public void setServletPath(String path) {
        myServletPath = path;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new IteratorEnumeration(
                myHeaders.keySet().iterator());
    }
    @Override
    public Enumeration<String> getHeaders(String name) {
        String[] values = myHeaders.get(name);
        return new ArrayEnumeration(values);
    }
    public void setHeader(String name, String... values) {
        myHeaders.put(name, values);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new IteratorEnumeration(
                myParams.keySet().iterator());
    }
    @Override
    public String[] getParameterValues(String name) {
        return myParams.get(name);
    }
    public void setParameter(String name, String... values) {
        myParams.put(name, values);
    }

    private String myContentType;
    private String myContextPath;
    private String myMethod;
    private String myQueryString;
    private String myRequestURL;
    private String myServletPath;
    private final Map<String, String[]> myHeaders = new HashMap<>();
    private final Map<String, String[]> myParams  = new HashMap<>();

    private static class IteratorEnumeration
            implements Enumeration<String>
    {
        public IteratorEnumeration(Iterator<String> iter)
        {
            myIter = iter;
        }

        @Override
        public boolean hasMoreElements()
        {
            return myIter.hasNext();
        }

        @Override
        public String nextElement()
        {
            return myIter.next();
        }

        private final Iterator<String> myIter;
    }

    private static class ArrayEnumeration
            implements Enumeration<String>
    {
        public ArrayEnumeration(String[] array)
        {
            myArray = array;
            myIdx   = 0;
        }

        @Override
        public boolean hasMoreElements()
        {
            return myIdx < myArray.length;
        }

        @Override
        public String nextElement()
        {
            return myArray[myIdx++];
        }

        private final String[] myArray;
        private int myIdx;
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDateHeader(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntHeader(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession(boolean bln) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse hsr) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String string, String string1) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Part getPart(String string) throws IOException, IllegalStateException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getContentLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParameter(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServerName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(String string, Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRealPath(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException();
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
