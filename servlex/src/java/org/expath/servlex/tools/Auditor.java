/****************************************************************************/
/*  File:       Auditor.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-03-31                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.DayTimeDurationValue;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.Servlex;
import org.expath.servlex.ServlexException;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.connectors.RequestConnector;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Serializer;

/**
 * Log audit information.
 *
 * @author Florent Georges
 * @date   2013-03-31
 */
public class Auditor
{
    public Auditor(ServerConfig config)
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
        myWriter = null;
        if ( myFile != null ) {
            try {
                myOutput = new FileOutputStream(myFile);
                myWriter = new OutputStreamWriter(myOutput, "utf-8");
            }
            catch ( IOException ex ) {
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
            try {
                String  id  = Servlex.getRequestMap().getPrivate("web:request-id");
                Document doc = request.getWebRequest(myConfig);
                myWriter.append("<profile request-id=\"");
                myWriter.append(id);
                myWriter.append("\">\n");
                myWriter.append("   <begin date=\"");
                myWriter.append(format(myStart));
                myWriter.append("\"/>\n");
                myWriter.flush();
                Serializer serial = myConfig.getProcessors().makeSerializer();
                serial.setMethod("xml");
                serial.setIndent("yes");
                serial.setOmitXmlDeclaration("yes");
                serial.serialize(doc, myOutput);
            }
            catch ( TechnicalException ex ) {
                String msg = "Internal error, getting the request-id.";
                throw new ServlexException(500, msg, ex);
            }
            catch ( IOException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
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
                myWriter.append("   <end date=\"");
                myWriter.append(format(myStop));
                myWriter.append("\" ms=\"");
                myWriter.append(Long.toString(ms));
                myWriter.append("\">");
                myWriter.append(duration(ms));
                myWriter.append("</end>\n");
                myWriter.append("</profile>\n");
                myWriter.close();
            }
            catch ( IOException ex ) {
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
                myWriter.append("   <start-compilation date=\"");
                myWriter.append(format(now));
                myWriter.append("\" after-ms=\"");
                myWriter.append(Long.toString(ms));
                myWriter.append("\" type=\"");
                myWriter.append(type);
                myWriter.append("\"/>\n");
            }
            catch ( IOException ex ) {
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
                myWriter.append("   <stop-compilation date=\"");
                myWriter.append(format(now));
                myWriter.append("\" after-ms=\"");
                myWriter.append(Long.toString(ms));
                myWriter.append("\"/>\n");
            }
            catch ( IOException ex ) {
                String msg = "Internal error, writing to the audit file: " + myFile;
                throw new ServlexException(500, msg, ex);
            }
        }
    }

    private String format(Date date)
    {
        return ISO_FORMAT.format(date);
    }

    /**
     * Return a literal XML Schema duration, from a number of milliseconds.
     * 
     * TODO: Should not depend on Saxon here...
     */
    private String duration(long millis)
            throws ServlexException
    {
        try {
            DayTimeDurationValue duration = DayTimeDurationValue.fromMilliseconds(millis);
            return duration.getStringValue();
        }
        catch ( ValidationException ex ) {
            String msg = "Internal error, computing duration from: " + millis;
            throw new ServlexException(500, msg, ex);
        }
    }

    private ServerConfig myConfig;
    private File myFile;
    private Writer myWriter;
    private OutputStream myOutput;
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
