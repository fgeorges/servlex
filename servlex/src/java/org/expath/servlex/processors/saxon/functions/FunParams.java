/****************************************************************************/
/*  File:       FunParams.java                                              */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-08-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;
import org.expath.servlex.ServlexConstants;
import org.expath.servlex.WebRepository;

/**
 * Utils for extension functions parameters for Saxon.
 *
 * @author Florent Georges
 * @date   2013-08-22
 */
class FunParams
{
    /**
     * Check the number of parameters in params, and throw an error if not OK.
     * 
     * @param params The parameter list.
     * @param num The number of parameters.
     */
    public FunParams(SequenceIterator[] params, int num)
            throws XPathException
    {
        this(params, num, num);
    }

    /**
     * Check the number of parameters in params, and throw an error if not OK.
     * 
     * @param params The parameter list.
     * @param min The minimal number of parameters.
     * @param max The maximum number of parameters.
     */
    public FunParams(SequenceIterator[] params, int min, int max)
            throws XPathException
    {
        if ( params.length < min || params.length > max ) {
            if ( min == max ) {
                throw new XPathException("There is not exactly " + min + " params: " + params.length);
            }
            else {
                throw new XPathException("There is not between " + min + " and " + max + " params: " + params.length);
            }
        }
        myParams = params;
        myMax = max;
    }

    /**
     * Return the number of parameters.
     */
    public int number()
    {
        return myParams.length;
    }

    /**
     * Return the pos-th parameter, checking it is a string.
     * 
     * If optional is false and the parameter is the empty sequence, an
     * {@code XPathException} is thrown.  As well as if there is more than
     * one item.
     * 
     * @param pos The position of the parameter to analyze, 0-based.
     * @param optional Can the parameter be the empty sequence?
     */
    public String asString(int pos, boolean optional)
            throws XPathException
    {
        Item item = asItem(pos, optional);
        if ( item == null ) {
            return null;
        }
        if ( ! ( item instanceof StringValue ) ) {
            throw new XPathException("The " + ordinal(pos) + " param is not a string");
        }
        return item.getStringValue();
    }

    /**
     * Return the pos-th parameter, checking it is a base64 binary.
     * 
     * If optional is false and the parameter is the empty sequence, an
     * {@code XPathException} is thrown.  As well as if there is more than
     * one item.
     * 
     * @param pos The position of the parameter to analyze, 0-based.
     * @param optional Can the parameter be the empty sequence?
     */
    public byte[] asBinary(int pos, boolean optional)
            throws XPathException
    {
        Item item = asItem(pos, optional);
        if ( item == null ) {
            return null;
        }
        if ( ! ( item instanceof Base64BinaryValue ) ) {
            throw new XPathException("The " + ordinal(pos) + " param is not a base64 binary");
        }
        Base64BinaryValue bin = (Base64BinaryValue) item;
        return bin.getBinaryValue();
    }

    /**
     * Return the pos-th parameter, checking it is a repository item.
     * 
     * If optional is false and the parameter is the empty sequence, an
     * {@code XPathException} is thrown.  As well as if there is more than
     * one item.
     * 
     * @param pos The position of the parameter to analyze, 0-based.
     * @param optional Can the parameter be the empty sequence?
     */
    public WebRepository asRepository(int pos, boolean optional)
            throws XPathException
    {
        Item item = asItem(pos, optional);
        if ( item == null ) {
            return null;
        }
        if ( ! ( item instanceof ObjectValue ) ) {
            throw new XPathException("The " + ordinal(pos) + " param is not an object value");
        }
        ObjectValue value  = (ObjectValue) item;
        Object      object = value.getObject();
        if ( ! ( object instanceof WebRepository ) ) {
            throw new XPathException("The " + ordinal(pos) + " param is not a web repository");
        }
        return (WebRepository) object;
    }

    /**
     * Return the pos-th parameter, checking it is an element node.
     * 
     * If optional is false and the parameter is the empty sequence, an
     * {@code XPathException} is thrown.  As well as if there is more than
     * one item, or if the param is not an element node.
     * 
     * @param pos The position of the parameter to analyze, 0-based.
     * @param optional Can the parameter be the empty sequence?
     */
    public NodeInfo asElement(int pos, boolean optional)
            throws XPathException
    {
        Item item = asItem(pos, optional);
        if ( item == null ) {
            return null;
        }
        if ( ! ( item instanceof NodeInfo ) ) {
            throw new XPathException("The " + ordinal(pos) + " param is not a node");
        }
        NodeInfo node = (NodeInfo) item;
        int kind = node.getNodeKind();
        if ( kind != Type.ELEMENT ) {
            throw new XPathException("The " + ordinal(pos) + " param is not an element (kind: " + kind + ")");
        }
        return node;
    }

    /**
     * Return the pos-th parameter, checking it is an element node, as well as its name.
     * 
     * If optional is false and the parameter is the empty sequence, an
     * {@code XPathException} is thrown.  As well as if there is more than
     * one item, or if the param is not an element node, or if the element
     * name is not equal to {@code name} (in the webapp namespace).
     * 
     * @param pos The position of the parameter to analyze, 0-based.
     * @param optional Can the parameter be the empty sequence?
     * @param name The required local name of the element.
     */
    public NodeInfo asElement(int pos, boolean optional, String name)
            throws XPathException
    {
        NodeInfo elem = asElement(pos, optional);
        String actual_name = elem.getLocalPart();
        if ( ! actual_name.equals(name) ) {
            String msg = "The " + ordinal(pos) + " param element local name is: "
                    + actual_name + ", instead of: " + name;
            throw new XPathException(msg);
        }
        String actual_ns = elem.getURI();
        if ( ! actual_ns.equals(ServlexConstants.WEBAPP_NS) ) {
            String msg = "The " + ordinal(pos) + " param element namespace is: "
                    + actual_ns + ", instead of: " + ServlexConstants.WEBAPP_NS;
            throw new XPathException(msg);
        }
        return elem;
    }

    /**
     * Return the pos-th parameter, checking its arity.
     * 
     * If optional is false and the parameter is the empty sequence, an
     * {@code XPathException} is thrown.  As well as if there is more than
     * one item.
     * 
     * @param pos The position of the parameter to analyze, 0-based.
     * @param optional Can the parameter be the empty sequence?
     */
    private Item asItem(int pos, boolean optional)
            throws XPathException
    {
        if ( pos < 0 || pos >= number() ) {
            throw new XPathException("Asked for the " + ordinal(pos) + " param of " + number());
        }
        SequenceIterator param = myParams[pos];
        Item item = param.next();
        if ( item == null ) {
            if ( optional ) {
                return null;
            }
            throw new XPathException("The " + ordinal(pos) + " param is an empty sequence");
        }
        if ( param.next() != null ) {
            throw new XPathException("The " + ordinal(pos) + " param sequence has more than one item");
        }
        return item;
    }

    private String ordinal(int pos)
            throws XPathException
    {
        if ( pos == 0 ) {
            return "1st";
        }
        else if ( pos == 1 ) {
            return "2d";
        }
        else if ( pos == 2 ) {
            return "3d";
        }
        else if ( pos > 2 ) {
            return (pos + 1) + "th";
        }
        else {
            throw new XPathException("pos must be 0 or above, and is: " + pos);
        }
    }

    public Formatter format(String name)
    {
        return new Formatter(name, myParams.length, myMax);
    }

    public class Formatter
    {
        public Formatter(String name, int num, int max)
        {
            myNum = num;
            myMax = max;
            myI   = 0;
            myBuf = new StringBuilder("Calling ");
            myBuf.append(ServlexConstants.WEBAPP_PREFIX);
            myBuf.append(":");
            myBuf.append(name);
            myBuf.append("(");
        }

        public Formatter param(String value)
            throws XPathException
        {
            if ( checkPos() ) {
                if ( value == null ) {
                    myBuf.append("()");
                }
                else {
                    myBuf.append("'");
                    myBuf.append(value.replace("'", "''"));
                    myBuf.append("'");
                }
            }
            return this;
        }

        // TODO: Adapt specifically for element nodes?
        public Formatter param(Item item)
            throws XPathException
        {
            if ( checkPos() ) {
                if ( item == null ) {
                    myBuf.append("()");
                }
                else {
                    myBuf.append(item.toString());
                }
            }
            return this;
        }

        public Formatter param(WebRepository value)
            throws XPathException
        {
            if ( checkPos() ) {
                if ( value == null ) {
                    myBuf.append("()");
                }
                else {
                    // TODO: Add more info about the repository?  Like its root
                    // directory or classpath?
                    myBuf.append("#<repository-item>");
                }
            }
            return this;
        }

        public Formatter param(byte[] value)
            throws XPathException
        {
            if ( checkPos() ) {
                if ( value == null ) {
                    myBuf.append("()");
                }
                else {
                    myBuf.append("#<TODO: binary: ");
                    myBuf.append(value);
                    myBuf.append(">");
                }
            }
            return this;
        }

        public Formatter param(SequenceIterator value)
            throws XPathException
        {
            if ( checkPos() ) {
                if ( value == null ) {
                    myBuf.append("()");
                }
                else {
                    myBuf.append("#<TODO: sequence: ");
                    myBuf.append(value);
                    myBuf.append(">");
                }
            }
            return this;
        }

        public String value()
        {
            myBuf.append(")");
            return myBuf.toString();
        }

        /**
         * Return true if the value must be output.
         */
        private boolean checkPos()
            throws XPathException
        {
            ++myI;
            if ( myI > myMax ) {
                throw new XPathException("too much params: " + ordinal(myI) + ", max: " + myMax);
            }
            boolean doit = myI <= myNum;
            if ( doit && myI > 1 ) {
                myBuf.append(", ");
            }
            return doit;
        }

        private final StringBuilder myBuf;
        private final int myNum;
        private final int myMax;
        private int myI;
    }

    private SequenceIterator[] myParams;
    private int myMax;
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
