/****************************************************************************/
/*  File:       StreamParser.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-05-07                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.io.InputStream;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.log4j.Logger;

/**
 * Wrap an XMLStreamReader with dedicated methods and consistent error handling.
 *
 * @author Florent Georges
 * @date   2012-05-07
 */
public class StreamParser
{
    /**
     * TODO: Is it possible to validate on-the-fly with an XMLStreamReader?
     * That would be handy!
     */
    public StreamParser(InputStream input, String target_ns)
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
            throws XMLStreamException
    {
        LOG.warn("expath-web.xml parser: IGNORING element '" + getLocalName() + "': not supported yet!");
        int open = 1;
        do {
            int e = myParser.next();
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
        try {
            NamespaceContext ctxt = myParser.getNamespaceContext();
            return ctxt.getNamespaceURI(prefix);
        }
        catch ( IllegalArgumentException ex ) {
            parseError("Prefix '" + prefix + "' not bound", ex);
            // cannot happen, parseError() never return
            return null;
        }
    }

    /**
     * Get the value of the attribute.
     */
    public int getEventType()
    {
        return myParser.getEventType();
    }

    /**
     * Get the value of the attribute.
     */
    public String getElementText()
            throws XMLStreamException
    {
        return myParser.getElementText();
    }

    /**
     * Get the value of the attribute.
     */
    public String getAttribute(String attr)
    {
        return myParser.getAttributeValue(null, attr);
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
    public boolean ensureNextElement(String local_name, boolean error)
            throws ParseException
    {
        try {
            nextTag();
            return ensureStartTag(local_name, error);
        }
        catch ( XMLStreamException ex ) {
            parseError("Error parsing the webapp descriptor", ex);
            // to make the compiler happy, it does not know parseError never returns
            return false;
        }
    }

    /**
     * Wrapper around {@link #parseLiteralQName(NamespaceContext,String)}.
     */
    public QName parseLiteralQName(String literal)
            throws ParseException
                 , XMLStreamException
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
                 , XMLStreamException
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
     * Ensure the current event is a start tag.
     */
    public boolean ensureStartTag(boolean error)
            throws ParseException
    {
        int event = myParser.getEventType();
        if ( event != XMLStreamConstants.START_ELEMENT ) {
            return error
                ? eventError("The current event is not START_ELEMENT", event)
                : false;
        }
        return true;
    }

    /**
     * Ensure the current event is a start tag, and ensure its name.
     */
    public boolean ensureStartTag(String local_name, boolean error)
            throws ParseException
    {
        boolean res = ensureStartTag(error);
        if ( res ) {
            res = ensureName(local_name, error);
        }
        return res;
    }

    /**
     * Ensure the current event is an end tag.
     */
    public boolean ensureEndTag(boolean error)
            throws ParseException
    {
        int event = myParser.getEventType();
        if ( event != XMLStreamConstants.END_ELEMENT ) {
            return error
                ? eventError("The current event is not END_ELEMENT", event)
                : false;
        }
        return true;
    }

    /**
     * Ensure the current event is an end tag, and ensure its name.
     */
    public boolean ensureEndTag(String local_name, boolean error)
            throws ParseException
    {
        boolean res = ensureEndTag(error);
        if ( res ) {
            res = ensureName(local_name, error);
        }
        return res;
    }

    /**
     * Ensure the name of the current event.
     */
    public boolean ensureName(String local_name, boolean error)
            throws ParseException
    {
        QName ref    = new QName(myTargetNs, local_name);
        QName actual = myParser.getName();
        if ( ! ref.equals(actual) ) {
            if ( error ) {
                parseError("The element is not a web:" + local_name + " (" + actual + ")");
            }
            else {
                return false;
            }
        }
        return true;
    }

    /**
     * Ensure the current event is a start tag, and ensure its namespace.
     */
    public boolean ensureNamespace(boolean error)
            throws ParseException
    {
        ensureStartTag(error);
        if ( ! myTargetNs.equals(myParser.getNamespaceURI()) ) {
            if ( error ) {
                parseError("The element is not in the webapp namespace");
            }
            else {
                return false;
            }
        }
        return true;
    }

    /**
     * Wrap nextTag(), and add logging around it.
     *
     * TODO: As we are here, log the exception...
     */
    public int nextTag()
            throws XMLStreamException
                 , ParseException
    {
        int event = myParser.nextTag();
        if ( LOG.isTraceEnabled() ) {
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

    public void parseError(String msg)
            throws ParseException
    {
        String m = myParser.getLocation() + ": " + msg;
        LOG.error(m);
        throw new ParseException(m);
    }

    public void parseError(String msg, Throwable ex)
            throws ParseException
    {
        String m = myParser.getLocation() + ": " + msg;
        LOG.error(m, ex);
        throw new ParseException(m, ex);
    }

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(EXPathWebParser.class);
    /** The underlying parser. */
    private XMLStreamReader myParser;
    /** THE namespace the parsed document uses. */
    private String myTargetNs;
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
