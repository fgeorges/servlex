/****************************************************************************/
/*  File:       Auditor.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-03-31                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.Servlex;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;

/**
 * Log audit information.
 *
 * @author Florent Georges
 */
public class Auditor
{
    public Auditor(ServerConfig config, Processors procs)
            throws ServlexException
    {
        myConfig = config;
        try {
            myFile = config.getProfileFile("servlex-audit");
        }
        catch ( TechnicalException ex ) {
            String msg = "Internal error, opening the audit file";
            throw new ServlexException(500, msg, ex);
        }
        if ( myFile != null ) {
            try {
                myWriter = new XmlWriter(myFile, procs);
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, opening the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void begin(RequestConnector request)
            throws ServlexException
    {
        myStart = new Date();
        if ( myWriter != null ) {
            // getting the request id
            String id = null;
            try {
                id = Servlex.getRequestMap().getPrivate("web:request-id");
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, getting the request-id.";
                throw new ServlexException(500, msg, ex);
            }
            // opening the profile element
            try {
                myWriter.openElement("profile", 0, a("request-id", id));
                myWriter.ln();
                myWriter.emptyElement("begin", 1, a("date", format(myStart)));
                myWriter.ln();
                myWriter.flush();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
            // the web request
            try {
                Document doc = request.getWebRequest(myConfig);
                myWriter.write(doc);
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing the web request to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void end()
            throws ServlexException
    {
        myStop = new Date();
        long ms = myStop.getTime() - myStart.getTime();
        if ( myWriter != null ) {
            try {
                String d = format(myStop);
                String m = Long.toString(ms);
                myWriter.textElement("end", duration(ms), 1, a("date", d), a("ms", m));
                myWriter.ln();
                myWriter.closeElement("profile", 0);
                myWriter.ln();
                myWriter.flush();
                myWriter.close();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void compilationStarts(String type)
            throws ServlexException
    {
        Date now = new Date();
        long ms = now.getTime() - myStart.getTime();
        if ( myWriter != null ) {
            try {
                String d = format(now);
                String m = Long.toString(ms);
                myWriter.emptyElement("start-compilation", 1, a("date", d), a("ms", m), a("type", type));
                myWriter.ln();
                myWriter.flush();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void compilationStops()
            throws ServlexException
    {
        Date now = new Date();
        long ms = now.getTime() - myStart.getTime();
        if ( myWriter != null ) {
            try {
                String d = format(now);
                String m = Long.toString(ms);
                myWriter.emptyElement("stop-compilation", 1, a("date", d), a("ms", m));
                myWriter.ln();
                myWriter.flush();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void connect(String from, String to)
            throws ServlexException
    {
        if ( myWriter != null ) {
            try {
                myWriter.emptyElement("connect", 1, a("from", from), a("to", to));
                myWriter.ln();
                myWriter.flush();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void run(String what)
            throws ServlexException
    {
        if ( myWriter != null ) {
            try {
                myWriter.openElement("run", 1);
                myWriter.text(what);
                myWriter.closeElement("run", 0);
                myWriter.ln();
                myWriter.flush();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void invoke(String kind, String name, String path, String... components)
            throws ServlexException
    {
        if ( myWriter != null ) {
            try {
                myWriter.openElement("invoke", 1);
                myWriter.ln();
                myWriter.textElement("kind", kind, 2);
                myWriter.ln();
                if ( name != null ) {
                    myWriter.textElement("name", name, 2);
                    myWriter.ln();
                }
                myWriter.textElement("path", path, 2);
                myWriter.ln();
                for ( String c : components ) {
                    myWriter.textElement("component", c, 2);
                    myWriter.ln();
                }
                myWriter.closeElement("invoke", 1);
                myWriter.ln();
                myWriter.flush();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    public void cleanup(String what)
            throws ServlexException
    {
        if ( myWriter != null ) {
            try {
                myWriter.openElement("cleanup", 1);
                myWriter.text(what);
                myWriter.closeElement("cleanup", 0);
                myWriter.ln();
                myWriter.flush();
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    private String format(Date date)
    {
        return ISO_FORMAT.format(date);
    }

    private XmlWriter.Attribute a(String name, String value)
    {
        return new XmlWriter.Attribute(name, value);
    }

    /**
     * Return a literal XML Schema duration, from a number of milliseconds.
     * 
     * Stolen from DayTimeDurationValue, to remove the dependency on Saxon.
     */
    private String duration(long millis)
            throws ServlexException
    {
        int days    = (int) (millis / (24L * 60L * 60L * 1000L));
        int hours   = (int) (millis % (24L * 60L * 60L * 1000L) / (60L * 60L * 1000L));
        int minutes = (int) (millis % (60L * 60L * 1000L) / (60L * 1000L));
        int seconds = (int) (millis % (60L * 1000L) / 1000L);
        int ms      = (int) (millis % 1000L);

        StringBuilder buf = new StringBuilder("P");
        if (days != 0) {
            buf.append(days + "D");
        }
        if (days == 0 || hours != 0 || minutes != 0 || seconds != 0 || ms != 0) {
            buf.append('T');
        }
        if (hours != 0) {
            buf.append(hours + "H");
        }
        if (minutes != 0) {
            buf.append(minutes + "M");
        }
        if (seconds != 0 || ms != 0 || (days == 0 && minutes == 0 && hours == 0)) {
            if (ms == 0) {
                buf.append(seconds + "S");
            }
            else {
                long total = (seconds * 1000) + ms;
                String mss = total + "";
                if (seconds == 0) {
                    mss = "0000" + mss;
                    mss = mss.substring(mss.length() - 4);
                }
                buf.append(mss.substring(0, mss.length() - 3));
                buf.append('.');
                int lastSigDigit = mss.length() - 1;
                while (mss.charAt(lastSigDigit) == '0') {
                    lastSigDigit--;
                }
                buf.append(mss.substring(mss.length() - 3, lastSigDigit + 1));
                buf.append('S');
            }
        }
        return buf.toString();
    }

    private ServerConfig myConfig;
    private File myFile;
    private XmlWriter myWriter;
    private Date myStart;
    private Date myStop;
    /** The ISO 8601 date formatter. */
    private static final DateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
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
