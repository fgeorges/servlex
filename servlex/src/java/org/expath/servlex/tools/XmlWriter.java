/****************************************************************************/
/*  File:       XmlWriter.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-14                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.processors.Serializer;

/**
 * Simple class to output XML to an {@link InputStream}.
 *
 * @author Florent Georges
 */
public class XmlWriter
{
    public XmlWriter(File out, Processors procs)
            throws TechnicalException
    {
        myProcs = procs;
        if ( out != null ) {
            try {
                myPath = out.getCanonicalPath();
                myOutput = new FileOutputStream(out);
                myWriter = new OutputStreamWriter(myOutput, "utf-8");
            }
            catch ( IOException ex ) {
                String msg = "Internal error, opening the file: " + myPath;
                throw new TechnicalException(msg, ex);
            }
        }
    }

    /**
     * Flush any pending output.
     */
    public void flush()
            throws TechnicalException
    {
        try {
            myWriter.flush();
            // needed as well?
            myOutput.flush();
        }
        catch ( IOException ex ) {
            String msg = "Internal error, flushing the file: " + myPath;
            throw new TechnicalException(msg, ex);
        }
    }

    /**
     * Flush any pending output.
     */
    public void close()
            throws TechnicalException
    {
        try {
            myWriter.close();
            // needed as well?
            myOutput.close();
        }
        catch ( IOException ex ) {
            String msg = "Internal error, closing the file: " + myPath;
            throw new TechnicalException(msg, ex);
        }
    }

    /**
     * Serialize the document to the output.
     */
    public void write(Document doc)
            throws TechnicalException
    {
        Serializer serial = myProcs.makeSerializer();
        serial.setMethod("xml");
        serial.setIndent("yes");
        serial.setOmitXmlDeclaration("yes");
        serial.serialize(doc, myOutput);
    }

    /**
     * Output a newline.
     */
    public void ln()
            throws TechnicalException
    {
        try {
            myWriter.append("\n");
        }
        catch ( IOException ex ) {
            String msg = "Internal error, writing to the file: " + myPath;
            throw new TechnicalException(msg, ex);
        }
    }

    /**
     * Write text content.
     */
    public void text(String text)
            throws TechnicalException
    {
        try {
            myWriter.append(text);
        }
        catch ( IOException ex ) {
            String msg = "Internal error, writing to the file: " + myPath;
            throw new TechnicalException(msg, ex);
        }
    }

    /**
     * Write an opening tag, with its attributes, at a specific indent level.
     */
    public void openElement(String name, int indent, Attribute... attrs)
            throws TechnicalException
    {
        openTag(name, indent, false, attrs);
    }

    /**
     * Write a closing tag, at a specific indent level.
     */
    public void closeElement(String name, int indent)
            throws TechnicalException
    {
        try {
            indent(indent);
            myWriter.append("</");
            myWriter.append(name);
            myWriter.append(">");
        }
        catch ( IOException ex ) {
            String msg = "Internal error, writing to the file: " + myPath;
            throw new TechnicalException(msg, ex);
        }
    }

    /**
     * Write an empty element, with its attributes, at a specific indent level.
     */
    public void emptyElement(String name, int indent, Attribute... attrs)
            throws TechnicalException
    {
        openTag(name, indent, true, attrs);
    }

    /**
     * Write an element with text content, with its attributes, at a specific indent level.
     */
    public void textElement(String name, String content, int indent, Attribute... attrs)
            throws TechnicalException
    {
        openElement(name, indent, attrs);
        text(content);
        closeElement(name, 0);
    }

    /**
     * Common implementation of openElement() and emptyElement().
     */
    private void openTag(String name, int indent, boolean empty, Attribute... attrs)
            throws TechnicalException
    {
        try {
            indent(indent);
            myWriter.append("<");
            myWriter.append(name);
            for ( Attribute a : attrs ) {
                myWriter.append(" ");
                myWriter.append(a.myName);
                myWriter.append("=\"");
                myWriter.append(a.myValue.replaceAll("\"", "&quot;"));
                myWriter.append("\"");
            }
            if ( empty ) {
                myWriter.append("/>");
            }
            else {
                myWriter.append(">");
            }
        }
        catch ( IOException ex ) {
            String msg = "Internal error, writing to the file: " + myPath;
            throw new TechnicalException(msg, ex);
        }
    }

    /**
     * Write an indentation at indent level.
     */
    private void indent(int indent)
            throws IOException
    {
        for ( ; indent > 0; --indent ) {
            myWriter.append(INDENT_SPACES);
        }
    }

    /** The string to use to indent one level. */
    private static final String INDENT_SPACES = "   ";

    /** The actual output, as an output stream. */
    private OutputStream myOutput;
    /** The actual output, as a writer. */
    private Writer myWriter;
    /** The path to the output file, for error reporting purpose. */
    private String myPath;
    /** The processors object to use. */
    private Processors myProcs;

    public static class Attribute
    {
        public Attribute(String name, String value)
        {
            myName  = name;
            myValue = value;
        }

        private String myName;
        private String myValue;
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
