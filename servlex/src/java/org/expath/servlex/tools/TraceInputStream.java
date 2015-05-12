/****************************************************************************/
/*  File:       TraceInputStream.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-03-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.io.IOException;
import javax.servlet.ServletInputStream;

/**
 * Proxy a {@link ServletInputStream} and log all data at trace level.
 *
 * @author Florent Georges
 */
public class TraceInputStream
        extends ServletInputStream
{
    public TraceInputStream(ServletInputStream in)
    {
        LOG.trace("TraceInputStream(" + in + ")");
        myIn = in;
    }

    /**
     * Push an array of bytes in the buffer as two-digit hexadecimals, separated by spaces.
     */
    private StringBuilder toHexes(byte[] b, int off, int len, StringBuilder buf)
    {
        if ( len > -1 ) {
            buf.append(" /");
            int end = off + len;
            for ( int i = off; i < end; ++i ) {
                buf.append(" ");
                String hex = String.format(" %02x", b[i]);
                buf.append(hex);
            }
        }
        return buf;
    }

    /**
     * Create a new buffer and push a representation of the call, with actual parameter values.
     */
    private StringBuilder makeCall(String method, byte[] b)
    {
        StringBuilder buf = new StringBuilder(method);
        buf.append("(");
        buf.append(b.toString());
        buf.append("): ");
        return buf;
    }

    /**
     * Create a new buffer and push a representation of the call, with actual parameter values.
     */
    private StringBuilder makeCall(String method, byte[] b, int off, int len)
    {
        StringBuilder buf = new StringBuilder(method);
        buf.append("(");
        buf.append(b.toString());
        buf.append(", ");
        buf.append(Integer.toString(off));
        buf.append(", ");
        buf.append(Integer.toString(len));
        buf.append("): ");
        return buf;
    }

    @Override
    public int read()
            throws IOException
    {
        int res = myIn.read();
        LOG.trace("read(): " + Integer.toString(res));
        return res;
    }

    @Override
    public int readLine(byte[] b, int off, int len)
            throws IOException
    {
        int res = myIn.readLine(b, off, len);
        StringBuilder buf = makeCall("readLine", b, off, len);
        buf.append(Integer.toString(res));
        toHexes(b, off, res, buf);
        LOG.trace(buf.toString());
        return res;
    }

    @Override
    public int read(byte[] b)
            throws IOException
    {
        int res = myIn.read(b);
        StringBuilder buf = makeCall("read", b);
        buf.append(Integer.toString(res));
        toHexes(b, 0, res, buf);
        LOG.trace(buf.toString());
        return res;
    }

    @Override
    public int read(byte[] b, int off, int len)
            throws IOException
    {
        int res = myIn.read(b, off, len);
        StringBuilder buf = makeCall("read", b, off, len);
        buf.append(Integer.toString(res));
        toHexes(b, off, res, buf);
        LOG.trace(buf.toString());
        return res;
    }

    @Override
    public long skip(long l)
            throws IOException
    {
        long res = myIn.skip(l);
        LOG.trace("skip(" + l + "): " + res);
        return res;
    }

    @Override
    public int available()
            throws IOException
    {
        int res = myIn.available();
        LOG.trace("available(): " + res);
        return res;
    }

    @Override
    public void close()
            throws IOException
    {
        LOG.trace("close()");
        myIn.close();
    }

    @Override
    public synchronized void mark(int i)
    {
        LOG.trace("mark(" + i + ")");
        myIn.mark(i);
    }

    @Override
    public synchronized void reset()
            throws IOException
    {
        LOG.trace("reset()");
        myIn.reset();
    }

    @Override
    public boolean markSupported()
    {
        boolean res = myIn.markSupported();
        LOG.trace("markSupported(): " + res);
        return res;
    }

    /** The logger. */
    private static final Log LOG = new Log(TraceInputStream.class);
    /** The proxied input stream. */
    private ServletInputStream myIn;
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
