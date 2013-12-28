/****************************************************************************/
/*  File:       StringTreeBuilder.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-12-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.test;

import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Attribute;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.Element;
import org.expath.servlex.processors.Item;
import org.expath.servlex.processors.Sequence;
import org.expath.servlex.processors.TreeBuilder;

/**
 * An implementation of {@link TreeBuilder} that generates a string from the events.
 *
 * @author Florent Georges
 */
public class StringTreeBuilder
        implements TreeBuilder
{
    @Override
    public void startElem(String local)
            throws TechnicalException
    {
        myBuffer.append("start elem: ");
        myBuffer.append(local);
        myBuffer.append('\n');
    }

    @Override
    public void attribute(String local, String value)
            throws TechnicalException
    {
        myBuffer.append("attribute: ");
        myBuffer.append(local);
        myBuffer.append(": ");
        myBuffer.append(value);
        myBuffer.append('\n');
    }

    @Override
    public void startContent()
            throws TechnicalException
    {
        myBuffer.append("start content\n");
    }

    @Override
    public void characters(String value)
            throws TechnicalException
    {
        myBuffer.append("characters: ");
        myBuffer.append(value.replace("\n", "\\n"));
        myBuffer.append('\n');
    }

    @Override
    public void endElem()
            throws TechnicalException
    {
        myBuffer.append("end elem\n");
    }

    @Override
    public void textElem(String local, String value)
            throws TechnicalException
    {
        myBuffer.append("text elem: ");
        myBuffer.append(local);
        myBuffer.append(": ");
        myBuffer.append(value.replace("\n", "\\n"));
        myBuffer.append('\n');
    }

    @Override
    public Document getRoot()
            throws TechnicalException
    {
        return new Document() {
            @Override
            public Element getRootElement() throws TechnicalException {
                return new Element() {
                    @Override
                    public QName name() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                    @Override
                    public URI baseUri() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                    @Override
                    public Iterator<Attribute> attributes() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                    @Override
                    public Iterator<Element> elements() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                    @Override
                    public Iterator<Item> children() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                    @Override
                    public Sequence asSequence() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                    @Override
                    public String stringValue() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                };
            }
            @Override
            public Sequence asSequence() {
                throw new UnsupportedOperationException("Not supported.");
            }
            @Override
            public String stringValue() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }

    @Override
    public String toString()
    {
        return myBuffer.toString();
    }

    private final StringBuilder myBuffer = new StringBuilder();
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
