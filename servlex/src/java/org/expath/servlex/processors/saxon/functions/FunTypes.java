/****************************************************************************/
/*  File:       FunUtils.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-11                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors.saxon.functions;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import org.expath.servlex.ServlexConstants;

/**
 * Utils for extension functions types for Saxon.
 *
 * @author Florent Georges
 */
class FunTypes
{
    // exactly one (no type podifier in XPath)
    private static final int SINGLE   = StaticProperty.EXACTLY_ONE;
    // zero or one ("?" in XPath)
    private static final int OPTIONAL = StaticProperty.ALLOWS_ZERO_OR_ONE;
    // zero or more ("*" in XPath)
    private static final int ANY      = StaticProperty.ALLOWS_ZERO_OR_MORE;
    // one or more ("+" in XPath)
    private static final int SEVERAL  = StaticProperty.ALLOWS_ONE_OR_MORE;

    // item()
    private static final ItemType ITEM     = AnyItemType.getInstance();
    // built-in atomic types
    private static final ItemType BASE64   = BuiltInAtomicType.BASE64_BINARY;
    private static final ItemType BOOLEAN  = BuiltInAtomicType.BOOLEAN;
    private static final ItemType BYTE     = BuiltInAtomicType.BYTE;
    private static final ItemType DATE     = BuiltInAtomicType.DATE;
    private static final ItemType DATETIME = BuiltInAtomicType.DATE_TIME;
    private static final ItemType DECIMAL  = BuiltInAtomicType.DECIMAL;
    private static final ItemType DOUBLE   = BuiltInAtomicType.DOUBLE;
    private static final ItemType FLOAT    = BuiltInAtomicType.FLOAT;
    private static final ItemType INT      = BuiltInAtomicType.INT;
    private static final ItemType INTEGER  = BuiltInAtomicType.INTEGER;
    private static final ItemType LONG     = BuiltInAtomicType.LONG;
    private static final ItemType SHORT    = BuiltInAtomicType.SHORT;
    private static final ItemType STRING   = BuiltInAtomicType.STRING;

    // the empty sequence
    public static final SequenceType EMPTY_SEQUENCE = SequenceType.EMPTY_SEQUENCE;

    // singles
    public static final SequenceType SINGLE_ITEM     = SequenceType.SINGLE_ITEM;
    // atomic types
    public static final SequenceType SINGLE_BASE64   = make(SINGLE, BASE64);
    public static final SequenceType SINGLE_BOOLEAN  = SequenceType.SINGLE_BOOLEAN;
    public static final SequenceType SINGLE_BYTE     = SequenceType.SINGLE_BYTE;
    public static final SequenceType SINGLE_DATE     = make(SINGLE, DATE);
    public static final SequenceType SINGLE_DATETIME = make(SINGLE, DATETIME);
    public static final SequenceType SINGLE_DECIMAL  = make(SINGLE, DECIMAL);
    public static final SequenceType SINGLE_DOUBLE   = SequenceType.SINGLE_DOUBLE;
    public static final SequenceType SINGLE_FLOAT    = SequenceType.SINGLE_FLOAT;
    public static final SequenceType SINGLE_INT      = SequenceType.SINGLE_INT;
    public static final SequenceType SINGLE_INTEGER  = SequenceType.SINGLE_INTEGER;
    public static final SequenceType SINGLE_LONG     = SequenceType.SINGLE_LONG;
    public static final SequenceType SINGLE_SHORT    = SequenceType.SINGLE_SHORT;
    public static final SequenceType SINGLE_STRING   = SequenceType.SINGLE_STRING;

    // optionals
    public static final SequenceType OPTIONAL_ITEM     = SequenceType.OPTIONAL_ITEM;
    // atomic types
    public static final SequenceType OPTIONAL_BASE64   = make(OPTIONAL, BASE64);
    public static final SequenceType OPTIONAL_BOOLEAN  = SequenceType.OPTIONAL_BOOLEAN;
    public static final SequenceType OPTIONAL_BYTE     = SequenceType.OPTIONAL_BYTE;
    public static final SequenceType OPTIONAL_DATE     = make(OPTIONAL, DATE);
    public static final SequenceType OPTIONAL_DATETIME = SequenceType.OPTIONAL_DATE_TIME;
    public static final SequenceType OPTIONAL_DECIMAL  = SequenceType.OPTIONAL_DECIMAL;
    public static final SequenceType OPTIONAL_DOUBLE   = SequenceType.OPTIONAL_DOUBLE;
    public static final SequenceType OPTIONAL_FLOAT    = SequenceType.OPTIONAL_FLOAT;
    public static final SequenceType OPTIONAL_INT      = SequenceType.OPTIONAL_INT;
    public static final SequenceType OPTIONAL_INTEGER  = SequenceType.OPTIONAL_INTEGER;
    public static final SequenceType OPTIONAL_LONG     = SequenceType.OPTIONAL_LONG;
    public static final SequenceType OPTIONAL_SHORT    = SequenceType.OPTIONAL_SHORT;
    public static final SequenceType OPTIONAL_STRING   = SequenceType.OPTIONAL_STRING;
    // nodes
    // TODO: Add other node kinds, and document nodes for other arities...
    // (as well as document node tests with root element name...)
    public static final SequenceType OPTIONAL_DOCUMENT = SequenceType.OPTIONAL_DOCUMENT_NODE;

    // anys
    public static final SequenceType ANY_ITEM     = make(ANY, ITEM);
    // atomic types
    public static final SequenceType ANY_BASE64   = make(ANY, BASE64);
    public static final SequenceType ANY_BOOLEAN  = make(ANY, BOOLEAN);
    public static final SequenceType ANY_BYTE     = make(ANY, BYTE);
    public static final SequenceType ANY_DATE     = make(ANY, DATE);
    public static final SequenceType ANY_DATETIME = make(ANY, DATETIME);
    public static final SequenceType ANY_DECIMAL  = make(ANY, DECIMAL);
    public static final SequenceType ANY_DOUBLE   = make(ANY, DOUBLE);
    public static final SequenceType ANY_FLOAT    = make(ANY, FLOAT);
    public static final SequenceType ANY_INT      = make(ANY, INT);
    public static final SequenceType ANY_INTEGER  = make(ANY, INTEGER);
    public static final SequenceType ANY_LONG     = make(ANY, LONG);
    public static final SequenceType ANY_SHORT    = make(ANY, SHORT);
    public static final SequenceType ANY_STRING   = make(ANY, STRING);

    // severals
    public static final SequenceType SEVERAL_ITEM     = make(SEVERAL, ITEM);
    // atomic types
    public static final SequenceType SEVERAL_BASE64   = make(SEVERAL, BASE64);
    public static final SequenceType SEVERAL_BOOLEAN  = make(SEVERAL, BOOLEAN);
    public static final SequenceType SEVERAL_BYTE     = make(SEVERAL, BYTE);
    public static final SequenceType SEVERAL_DATE     = make(SEVERAL, DATE);
    public static final SequenceType SEVERAL_DATETIME = make(SEVERAL, DATETIME);
    public static final SequenceType SEVERAL_DECIMAL  = make(SEVERAL, DECIMAL);
    public static final SequenceType SEVERAL_DOUBLE   = make(SEVERAL, DOUBLE);
    public static final SequenceType SEVERAL_FLOAT    = make(SEVERAL, FLOAT);
    public static final SequenceType SEVERAL_INT      = make(SEVERAL, INT);
    public static final SequenceType SEVERAL_INTEGER  = make(SEVERAL, INTEGER);
    public static final SequenceType SEVERAL_LONG     = make(SEVERAL, LONG);
    public static final SequenceType SEVERAL_SHORT    = make(SEVERAL, SHORT);
    public static final SequenceType SEVERAL_STRING   = make(SEVERAL, STRING);

    /**
     * Create a list of types.
     */
    public static SequenceType[] types(SequenceType... args)
    {
        return args;
    }

    /**
     * Create a QName in the web:* namespace.
     */
    public static StructuredQName qname(String local)
    {
        final String uri    = ServlexConstants.WEBAPP_NS;
        final String prefix = ServlexConstants.WEBAPP_PREFIX;
        return new StructuredQName(prefix, uri, local);
    }

    /**
     * Create an "element(web:xxx)" type, in the web:* namespace.
     */
    public static SequenceType single_element(String local, Processor saxon)
    {
        return element(SINGLE, local, saxon);
    }

    /**
     * Create an "element(web:xxx)?" type, in the web:* namespace.
     */
    public static SequenceType optional_element(String local, Processor saxon)
    {
        return element(OPTIONAL, local, saxon);
    }

    /**
     * Create an "element(web:xxx)*" type, in the web:* namespace.
     */
    public static SequenceType any_element(String local, Processor saxon)
    {
        return element(ANY, local, saxon);
    }

    /**
     * Create an "element(web:xxx)+" type, in the web:* namespace.
     */
    public static SequenceType several_element(String local, Processor saxon)
    {
        return element(SEVERAL, local, saxon);
    }

    private static SequenceType element(int occurrence, String local, Processor saxon)
    {
        final int      kind   = Type.ELEMENT;
        final String   uri    = ServlexConstants.WEBAPP_NS;
        final NamePool pool   = saxon.getUnderlyingConfiguration().getNamePool();
        final ItemType itype  = new NameTest(kind, uri, local, pool);
        return SequenceType.makeSequenceType(itype, occurrence);
    }

    private static SequenceType make(int occurrence, ItemType type)
    {
        return SequenceType.makeSequenceType(type, occurrence);
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
