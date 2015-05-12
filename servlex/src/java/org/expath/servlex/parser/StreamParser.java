/****************************************************************************/
/*  File:       StreamParser.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import org.expath.servlex.tools.Log;

/**
 * Wrap an XMLStreamReader with dedicated methods and consistent error handling.
 *
 * @author Florent Georges
 */
public class StreamParser
{
    /**
     * TODO: Is it possible to validate on-the-fly with an XMLStreamReader?
     * That would be handy!
     */
    public StreamParser(Source input, String target_ns)
            throws ParseException
    {
        myTargetNs = target_ns;
        // instantiate the parser
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            myParser = factory.createXMLStreamReader(input);
        }
        catch ( XMLStreamException ex ) {
            throw new ParseException("Error opening the input stream", ex);
        }
        // check the first event ia a start document event
        int event = myParser.getEventType();
        if ( event != XMLStreamConstants.START_DOCUMENT ) {
            eventError("The current event is not START_DOCUMENT", event);
        }
    }

    /**
     * DEBUG: This is only used within not-implemented-yet-code, to skip the
     * whole element...
     */
    public void debug_skipElement()
            throws ParseException
    {
        LOG.warn("expath-web.xml parser: IGNORING element '" + getLocalName() + "': not supported yet!");
        int open = 1;
        do {
            int e;
            try {
                e = myParser.next();
            }
            catch ( XMLStreamException ex ) {
                throw new ParseException("Error skipping an element, open=" + open, ex);
            }
            if ( e == XMLStreamConstants.START_ELEMENT ) {
                open++;
            }
            else if ( e == XMLStreamConstants.END_ELEMENT ) {
                open--;
            }
        }
        while ( open > 0 );
    }

    /**
     * Resolve a namespace prefix to the namespace URI it is bound to.
     * 
     * Use the current context to look for namespace bindings.  Throw an error
     * if the prefix is not bound in the current context.
     */
    public String resolvePrefix(String prefix)
            throws ParseException
    {
        String res = null;
        try {
            NamespaceContext ctxt = myParser.getNamespaceContext();
            res = ctxt.getNamespaceURI(prefix);
        }
        catch ( IllegalArgumentException ex ) {
            parseError("Prefix '" + prefix + "' not bound", ex);
        }
        return res;
    }

    /**
     * @return the current event type.
     */
    public int getEventType()
    {
        return myParser.getEventType();
    }

    /**
     * @return the current event name.
     */
    public String getEventName()
            throws ParseException
    {
        return getEventName(getEventType());
    }

    /**
     * @return the content of the text-only current element.
     */
    public String getElementText()
            throws ParseException
    {
        try {
            return myParser.getElementText();
        }
        catch ( XMLStreamException ex ) {
            throw new ParseException("Error getting the element text", ex);
        }
    }

    /**
     * Get the value of the attribute.
     */
    public String getAttribute(String attr)
    {
        return myParser.getAttributeValue("", attr);
    }

    /**
     * Get the current local name.
     */
    public String getLocalName()
    {
        return myParser.getLocalName();
    }

    /**
     * Get the current QName.
     */
    public QName getName()
    {
        return myParser.getName();
    }

    /**
     * Go to the next tag, ensure it is a start tag, and ensure the element name.
     */
    public void ensureNextElement(String local_name)
            throws ParseException
    {
        nextTag();
        ensureStartTag(local_name);
    }

    /**
     * Wrapper around {@link #parseLiteralQName(NamespaceContext,String)}.
     */
    public QName parseLiteralQName(String literal)
            throws ParseException
    {
        NamespaceContext namespaces = myParser.getNamespaceContext();
        return parseLiteralQName(namespaces, literal);
    }

    /**
     * Decode a literal QName.
     *
     * A literal QName is a string which is either in James Clark form (like
     * this: {@code "{http://example.org/ns}local-name"}, so does not rely on
     * the namespace context) or a literal QName in the sense of XSLT (like
     * this: {@code "ns:local-name"}, so does rely on the namespace context to
     * resolve the prefix).
     */
    public QName parseLiteralQName(NamespaceContext namespaces, String literal)
            throws ParseException
    {
        if ( literal.startsWith("{") ) {
            int closing = literal.indexOf('}');
            if ( closing < 0 ) {
                throw new ParseException("Ill-formed James Clark QName: " + literal);
            }
            String uri   = literal.substring(1, closing);
            String local = literal.substring(closing + 1);
            return new QName(uri, local);
        }
        int colon = literal.indexOf(':');
        if ( colon < 0 ) { // no colon
            return new QName(literal);
        }
        else {
            String prefix = literal.substring(0, colon);
            String local  = literal.substring(colon + 1);
            String uri    = namespaces.getNamespaceURI(prefix);
            return new QName(uri, local, prefix);
        }
    }

    /**
     * @return whether the current event is a start tag.
     */
    public boolean isStartTag()
    {
        int event = myParser.getEventType();
        return event == XMLStreamConstants.START_ELEMENT;
    }

    /**
     * @return whether the current event is a start tag, with a given name.
     */
    public boolean isStartTag(String local_name)
    {
        return isStartTag() && hasName(local_name);
    }

    /**
     * @return whether the current event has a given name.
     */
    public boolean hasName(String local_name)
    {
        QName ref    = new QName(myTargetNs, local_name);
        QName actual = myParser.getName();
        return ref.equals(actual);
    }

    /**
     * Ensure the current event is a start tag.
     */
    public void ensureStartTag()
            throws ParseException
    {
        int event = myParser.getEventType();
        if ( event != XMLStreamConstants.START_ELEMENT ) {
            eventError("The current event is not START_ELEMENT", event);
        }
    }

    /**
     * Ensure the current event is a start tag, and ensure its name.
     */
    public void ensureStartTag(String local_name)
            throws ParseException
    {
        ensureStartTag();
        ensureName(local_name);
    }

    /**
     * Ensure the current event is an end tag.
     */
    public void ensureEndTag()
            throws ParseException
    {
        int event = myParser.getEventType();
        if ( event != XMLStreamConstants.END_ELEMENT ) {
            eventError("The current event is not END_ELEMENT", event);
        }
    }

    /**
     * Ensure the current event is an end tag, and ensure its name.
     */
    public void ensureEndTag(String local_name)
            throws ParseException
    {
        ensureEndTag();
        ensureName(local_name);
    }

    /**
     * Ensure the name of the current event.
     */
    public void ensureName(String local_name)
            throws ParseException
    {
        QName ref    = new QName(myTargetNs, local_name);
        QName actual = myParser.getName();
        if ( ! ref.equals(actual) ) {
            parseError("The element is not a web:" + local_name + " (" + actual + ")");
        }
    }

    /**
     * Ensure the current event is a start tag, and ensure its namespace.
     */
    public void ensureNamespace()
            throws ParseException
    {
        ensureStartTag();
        if ( ! myTargetNs.equals(myParser.getNamespaceURI()) ) {
            parseError("The element is not in the webapp namespace");
        }
    }

    /**
     * Wrap nextTag(), and add logging around it.
     *
     * TODO: As we are here, log the exception...
     */
    public int nextTag()
            throws ParseException
    {
        int event;
        try {
            event = myParser.nextTag();
        }
        catch ( XMLStreamException ex ) {
            throw new ParseException("Error getting the next tag", ex);
        }
        if ( LOG.trace()) {
            LOG.trace("PARSER EVENT: " + getEventName(event) + " - " + myParser.getName());
        }
        return event;
    }

    /**
     * Throw an exception with the event name.
     */
    private static boolean eventError(String msg, int event)
            throws ParseException
    {
        String event_name = getEventName(event);
        throw new ParseException(msg + " (" + event_name + ")");
    }

    /**
     * Return the event name as a string.
     */
    public static String getEventName(int event)
            throws ParseException
    {
        switch ( event ) {
            case XMLStreamConstants.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLStreamConstants.CDATA:
                return "CDATA";
            case XMLStreamConstants.CHARACTERS:
                return "CHARACTERS";
            case XMLStreamConstants.COMMENT:
                return "COMMENT";
            case XMLStreamConstants.DTD:
                return "DTD";
            case XMLStreamConstants.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLStreamConstants.END_ELEMENT:
                return "END_ELEMENT";
            case XMLStreamConstants.ENTITY_DECLARATION:
                return "ENTITY_DECLARATION";
            case XMLStreamConstants.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLStreamConstants.NAMESPACE:
                return "NAMESPACE";
            case XMLStreamConstants.NOTATION_DECLARATION:
                return "NOTATION_DECLARATION";
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLStreamConstants.SPACE:
                return "SPACE";
            case XMLStreamConstants.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLStreamConstants.START_ELEMENT:
                return "START_ELEMENT";
            default:
                throw new ParseException("Unknown event code: " + event);
        }
    }

    private String errorMessage(String msg)
            throws ParseException
    {
        int event = getEventType();
        StringBuilder buf = new StringBuilder(myParser.getLocation().toString());
        buf.append("(event: ");
        buf.append(getEventName(event));
        if ( event == XMLStreamConstants.START_ELEMENT || event == XMLStreamConstants.END_ELEMENT ) {
            buf.append(", ");
            buf.append(getLocalName());
        }
        buf.append("): ");
        buf.append(msg);
        return buf.toString();
    }

    public void parseError(String msg)
            throws ParseException
    {
        String m = errorMessage(msg);
        LOG.error(m);
        throw new ParseException(m);
    }

    public void parseError(String msg, Throwable ex)
            throws ParseException
    {
        String m = errorMessage(msg);
        LOG.error(m, ex);
        throw new ParseException(m, ex);
    }

    /** The logger. */
    private static final Log LOG = new Log(EXPathWebParser.class);
    /** The underlying parser. */
    private final XMLStreamReader myParser;
    /** THE namespace the parsed document uses. */
    private final String myTargetNs;
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
