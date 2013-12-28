package org.expath.servlex.tools.regex;

import net.sf.saxon.Configuration;
import net.sf.saxon.z.IntRangeSet;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.charcode.XMLCharacterData;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class translates XML Schema regex syntax into JDK 1.5 regex syntax. This differs from the JDK 1.4
 * translator because JDK 1.5 handles non-BMP characters (wide characters) in places where JDK 1.4 does not,
 * for example in a range such as [X-Y]. This enables much of the code from the 1.4 translator to be
 * removed.
 * Author: James Clark, Thai Open Source Software Center Ltd. See statement at end of file.
 * Modified by Michael Kay (a) to integrate the code into Saxon, and (b) to support XPath additions
 * to the XML Schema regex syntax. This version also removes most of the complexities of handling non-BMP
 * characters, since JDK 1.5 handles these natively.
 */
public class JDK15RegexTranslator extends RegexTranslator {

    /**
     * Translates XML Schema and XPath regexes into <code>java.util.regex</code> regexes.
     *
     * @see java.util.regex.Pattern
     * @see <a href="http://www.w3.org/TR/xmlschema-2/#regexs">XML Schema Part 2</a>
     */

    public static final CharClass[] categoryCharClasses = new CharClass[RegexData.categories.length()];
    public static final CharClass[] subCategoryCharClasses = new CharClass[RegexData.subCategories.length() / 2];


    private static final CharClass DOT_SCHEMA =
            Complement.makeComplement(new Union(new CharClass[]{new SingleChar('\n'), new SingleChar('\r')}));

    private static final CharClass ESC_W = new Union(new CharClass[]{getSafeCategory('P'),
                                                                     getSafeCategory('Z'),
                                                                     getSafeCategory('C')});
    //was: new Property("P"), new Property("Z"), new Property("C") }

    private static CharClass getSafeCategory(char c) {
        try {
            return getCategoryCharClass(c);
        } catch (RegexSyntaxException err) {
            throw new AssertionError(err);
        }
    }

    private static final CharClass ESC_w = Complement.makeComplement(ESC_W);

    private static final CharClass ESC_s = new Union(new CharClass[]{
        new SingleChar(' '),
        new SingleChar('\n'),
        new SingleChar('\r'),
        new SingleChar('\t')
    });

    private static final CharClass ESC_S = Complement.makeComplement(ESC_s);

    //private static final CharClass ESC_i_10 = makeNameCharClass(XMLCharacterData.NAME_START_10_MASK);

    private static final CharClass ESC_i_11 = makeNameCharClass(XMLCharacterData.NAME_START_11_MASK);

    //private static final CharClass ESC_I_10 = new Complement(ESC_i_10);

    private static final CharClass ESC_I_11 = Complement.makeComplement(ESC_i_11);

    //private static final CharClass ESC_c_10 = makeNameCharClass(XMLCharacterData.NAME_10_MASK);

    private static final CharClass ESC_c_11 = makeNameCharClass(XMLCharacterData.NAME_11_MASK);

    //private static final CharClass ESC_C_10 = new Complement(ESC_c_10);

    private static final CharClass ESC_C_11 = Complement.makeComplement(ESC_c_11);

    private JDK15RegexTranslator() {

    }

    /**
     * Translates a regular expression in the syntax of XML Schemas Part 2 into a regular
     * expression in the syntax of <code>java.util.regex.Pattern</code>.  The translation
     * assumes that the string to be matched against the regex uses surrogate pairs correctly.
     * If the string comes from XML content, a conforming XML parser will automatically
     * check this; if the string comes from elsewhere, it may be necessary to check
     * surrogate usage before matching.
     *
     * @param regExp a String containing a regular expression in the syntax of XML Schemas Part 2
     * @param options bit-wise option settings
     * @param flagbits Java bit-wise options settings based on supplied flags
     * @param warnings  a list to contain any warnings generated. If no list is supplied, this indicates
     * that the caller is not interested in knowing about any warnings.
     * @return a JDK 1.5 regular expression
     * @throws RegexSyntaxException if <code>regexp</code> is not a regular expression in the
     *                              syntax of XML Schemas Part 2, or XPath 2.0, as appropriate
     * @see java.util.regex.Pattern
     * @see <a href="http://www.w3.org/TR/xmlschema-2/#regexs">XML Schema Part 2</a>
     */
    public static String translate(CharSequence regExp, int options, int flagbits, /*@Nullable*/ List<RegexSyntaxException> warnings)
            throws RegexSyntaxException {

        //System.err.println("Input regex: " + regexp);
        JDK15RegexTranslator tr = new JDK15RegexTranslator();
        tr.regExp = regExp;
        tr.length = regExp.length();
        tr.xmlVersion = (options & RegularExpression.XML11) != 0 ? Configuration.XML11 : Configuration.XML10;
        tr.xsdVersion = (options & RegularExpression.XSD11) != 0 ? Configuration.XSD11 : Configuration.XSD10;
        tr.isXPath = (options & (RegularExpression.XPATH20|RegularExpression.XPATH30)) != 0;
        tr.isXPath30 = (options & RegularExpression.XPATH30) != 0;
        tr.ignoreWhitespace = (flagbits & Pattern.COMMENTS) != 0;
        tr.caseBlind = (flagbits & Pattern.CASE_INSENSITIVE) != 0;
        tr.warnings = (warnings == null ? new ArrayList<RegexSyntaxException>() : warnings);
        tr.advance();
        tr.translateTop();
        //System.err.println("Output regex: " + tr.result.toString());
        return tr.result.toString();
    }



    protected static abstract class CharClass {

        protected CharClass() {
        }

        abstract void output(FastStringBuffer buf);

        abstract void outputComplement(FastStringBuffer buf);


        int getSingleChar() {
            return -1;
        }

    }

    static abstract class SimpleCharClass extends CharClass {
        SimpleCharClass() {

        }

        void output(FastStringBuffer buf) {
            buf.append('[');
            inClassOutput(buf);
            buf.append(']');
        }

        void outputComplement(FastStringBuffer buf) {
            buf.append("[^");
            inClassOutput(buf);
            buf.append(']');
        }

        abstract void inClassOutput(FastStringBuffer buf);
    }

    static class SingleChar extends SimpleCharClass {
        private final int c;
        private boolean isEscaped = false;

        SingleChar(int c) {
            this.c = c;
        }

        SingleChar(int c, boolean isEscaped) {
            this.c = c;
            this.isEscaped = isEscaped;
        }

        int getSingleChar() {
            return c;
        }

        void output(FastStringBuffer buf) {
            inClassOutput(buf);
        }

        void inClassOutput(FastStringBuffer buf) {
            if (isJavaMetaChar(c)) {
                buf.append('\\');
                buf.append((char) c);
            } else {
                switch (c) {
                    case '\r':
                        buf.append("\\r");
                        break;
                    case '\n':
                        buf.append("\\n");
                        break;
                    case '\t':
                        buf.append("\\t");
                        break;
                    case ' ':
                        buf.append("\\x20");
                        break;
                    default:
                        buf.appendWideChar(c);
                }
            }
        }
    }


    static class Empty extends SimpleCharClass {
        private static final Empty instance = new Empty();

        private Empty() {

        }

        static Empty getInstance() {
            return instance;
        }

        void output(FastStringBuffer buf) {
            buf.append("\\x00");        // no character matches
        }

        void outputComplement(FastStringBuffer buf) {
            buf.append("[^\\x00]");    // every character matches
        }

        void inClassOutput(FastStringBuffer buf) {
            throw new RuntimeException("BMP output botch");
        }

    }

    protected static class CharRange extends SimpleCharClass {
        private final int lower;
        private final int upper;

        CharRange(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
        }

        void inClassOutput(FastStringBuffer buf) {
            if (isJavaMetaChar(lower)) {
                buf.append('\\');
            }
            buf.appendWideChar(lower);
            buf.append('-');
            if (isJavaMetaChar(upper)) {
                buf.append('\\');
            }
            buf.appendWideChar(upper);
        }

    }

    static class Property extends SimpleCharClass {
        private final String name;

        Property(String name) {
            this.name = name;
        }

        void inClassOutput(FastStringBuffer buf) {
            buf.append("\\p{");
            buf.append(name);
            buf.append('}');
        }

        void outputComplement(FastStringBuffer buf) {
            buf.append("\\P{");
            buf.append(name);
            buf.append('}');
        }

        // TODO: Since JDK 1.5, the code should in theory be able to use Java character classes directly, without expansion.
        // However, we hit a JDK bug: \P{Lu} matches the low-surrogate byte of any non-BMP character that is in class \p{Lu}.
        // Reported 2010-01-15. Various attempts to circumvent the bug failed; the code that we're using seems as good as any.
    }

    static class Subtraction extends CharClass {
        private final CharClass cc1;
        private final CharClass cc2;

        Subtraction(CharClass cc1, CharClass cc2) {
            // min corresponds to intersection
            // complement corresponds to negation
            this.cc1 = cc1;
            this.cc2 = cc2;
        }

        void output(FastStringBuffer buf) {
            buf.append('[');
            cc1.output(buf);
            buf.append("&&");
            cc2.outputComplement(buf);
            buf.append(']');
        }

        void outputComplement(FastStringBuffer buf) {
            buf.append('[');
            cc1.outputComplement(buf);
            cc2.output(buf);
            buf.append(']');
        }
    }

    static class Union extends CharClass {
        private final List<? extends CharClass> members;


        Union(CharClass[] v) {
            this(toList(v));
        }

        Union(CharClass a, CharClass b) {
            this(new CharClass[]{a, b});
        }

        private static List toList(CharClass[] v) {
            List<CharClass> members = new ArrayList<CharClass>(5);
            members.addAll(Arrays.asList(v));
            return members;
        }

        Union(List<? extends CharClass> members) {
            this.members = members;
        }

        void output(FastStringBuffer buf) {
//            if (suppressCaseBlindness) {
//                buf.append("(?-i:");
//            }
            buf.append('[');
            for (int i = 0, len = members.size(); i < len; i++) {
                members.get(i).output(buf);
            }
            buf.append(']');
//            if (suppressCaseBlindness) {
//                buf.append(")");
//            }
        }

        void outputComplement(FastStringBuffer buf) {
            boolean first = true;
            for (CharClass cc : members) {
                if (cc instanceof SimpleCharClass) {
                    if (first) {
                        buf.append("[^");
                        first = false;
                    }
                    ((SimpleCharClass) cc).inClassOutput(buf);
                }
            }
            for (CharClass cc : members) {
                if (!(cc instanceof SimpleCharClass)) {
                    if (first) {
                        buf.append('[');
                        first = false;
                    } else {
                        buf.append("&&");
                    }
                    cc.outputComplement(buf);
                }
            }
            if (first) {
                // empty union, so the complement is everything
                buf.append("[\u0001-");
                buf.appendWideChar(UTF16CharacterSet.NONBMP_MAX);
                buf.append("]");
            } else {
                buf.append(']');
            }
        }
    }

    static class BackReference extends CharClass {
        private final int n;
        private boolean caseBlind = false;


        BackReference(int n, boolean caseBlind) {
            this.n = n;
            this.caseBlind = caseBlind;
        }

        void output(FastStringBuffer buf) {
            inClassOutput(buf);
        }

        void outputComplement(FastStringBuffer buf) {
            inClassOutput(buf);
        }

        void inClassOutput(FastStringBuffer buf) {
            // terminate the back-reference with a syntactic separator
            buf.append("(?" + (caseBlind ? "i" : "") + ":\\" + n + ")");
        }
    }


    static class Complement extends CharClass {
        private final CharClass cc;

        private Complement(CharClass cc) {
            this.cc = cc;
        }

        static CharClass makeComplement(CharClass cc) {
            if (cc instanceof Complement) {
                return ((Complement)cc).cc;
            } else {
                return new Complement(cc);
            }
        }

        void output(FastStringBuffer buf) {
            cc.outputComplement(buf);
        }

        void outputComplement(FastStringBuffer buf) {
            cc.output(buf);
        }
    }

    protected boolean translateAtom() throws RegexSyntaxException {
        switch (curChar) {
            case RegexData.EOS:
                if (!eos)
                    break;
                // else fall through
            case '?':
            case '*':
            case '+':
            case ')':
            case '{':
            case '}':
            case '|':
            case ']':
                return false;
            case '(':
                copyCurChar();
                if (isXPath30 && curChar == '?') {
                    copyCurChar();
                    expect(':');
                    copyCurChar();
                    translateRegExp();
                    expect(')');
                    copyCurChar();
                    return true;
                } else {
                    int thisCapture = ++currentCapture;
                    translateRegExp();
                    expect(')');
                    captures.add(thisCapture);
                    copyCurChar();
                    return true;
                }
            case '\\':
                advance();
                parseEsc().output(result);
                return true;
            case '[':
                inCharClassExpr = true;
                advance();
                parseCharClassExpr().output(result);
                return true;
            case '.':
                if (isXPath) {
                    // under XPath, "." has the same meaning as in JDK 1.5
                    break;
                } else {
                    // under XMLSchema, "." means anything except \n or \r, which is different from the XPath/JDK rule
                    DOT_SCHEMA.output(result);
                    advance();
                    return true;
                }
            case '$':
            case '^':
                if (isXPath) {
                    copyCurChar();
                    return true;
                }
                result.append('\\');
                break;
            default:
                if (caseBlind) {
                    int thisChar = absorbSurrogatePair();
                    int[] variants = CaseVariants.getCaseVariants(thisChar);
                    if (variants.length > 0) {
                        CharClass[] chars = new CharClass[variants.length+1];
                        chars[0] = new SingleChar(thisChar);
                        for (int i=0; i<variants.length; i++) {
                            chars[i+1] = new SingleChar(variants[i]);
                        }
                        Union union = new Union(chars);
                        union.output(result);
                        advance();
                        return true;
                    }
                    // else fall through
                }
                // else fall through
        }
        copyCurChar();
        return true;
    }

    private static CharClass makeNameCharClass(byte mask) {
        List<CharClass> ranges = new ArrayList<CharClass>();
        // Add colon to the set of characters matched
        ranges.add(new SingleChar(':'));
        // Plus all the characters from the NCName tables
        IntRangeSet members = XMLCharacterData.getCategory(mask);
        int used = members.getNumberOfRanges();
        int[] startPoints = members.getStartPoints();
        int[] endPoints = members.getEndPoints();
        for (int i=0; i<used; i++) {
            if (startPoints[i] == endPoints[i]) {
                ranges.add(new SingleChar(startPoints[i]));
            } else {
                ranges.add(new CharRange(startPoints[i], endPoints[i]));
            }
        }
        return new Union(ranges);
    }


    private CharClass parseEsc() throws RegexSyntaxException {
        switch (curChar) {
            case 'n':
                advance();
                return new SingleChar('\n', true);
            case 'r':
                advance();
                return new SingleChar('\r', true);
            case 't':
                advance();
                return new SingleChar('\t', true);
            case '\\':
            case '|':
            case '.':
            case '-':
            case '^':
            case '?':
            case '*':
            case '+':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
                break;
            case 's':
                advance();
                return ESC_s;
            case 'S':
                advance();
                return ESC_S;
            case 'i':
                advance();
                //return (xmlVersion == Configuration.XML10 ? ESC_i_10 : ESC_i_11);
                return ESC_i_11; // also applies to XML 1.0 5e
            case 'I':
                advance();
                //return (xmlVersion == Configuration.XML10 ? ESC_I_10 : ESC_I_11);
                return ESC_I_11;
            case 'c':
                advance();
                //return (xmlVersion == Configuration.XML10 ? ESC_c_10 : ESC_c_11);
                return ESC_c_11;
            case 'C':
                advance();
                //return (xmlVersion == Configuration.XML10 ? ESC_C_10 : ESC_C_11);
                return ESC_C_11;
            case 'd':
                advance();
                return getSubCategoryCharClass("Nd");
            case 'D':
                advance();
                return Complement.makeComplement(getSubCategoryCharClass("Nd"));
            case 'w':
                advance();
                return ESC_w;
            case 'W':
                advance();
                return ESC_W;
            case 'p':
                advance();
                return parseProp(false);
            case 'P':
                advance();
                return Complement.makeComplement(parseProp(true));
            case '0':
                throw makeException("backreference cannot start with digit zero");
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                if (isXPath) {
                    if (inCharClassExpr) {
                        throw makeException("back-reference not allowed within []");
                    }
                    int backRef = (curChar - '0');
                    while (true) {
                        advance();
                        int c1 = "0123456789".indexOf(curChar);
                        if (c1 < 0) {
                            break;
                        } else {
                            int backRef2 = backRef * 10 + c1;
                            if (backRef2 > currentCapture) {
                                break;
                            } else {
                                backRef = backRef2;
                            }
                        }

                    }
                    if (!captures.contains(backRef)) {
                        String explanation = (backRef > currentCapture ? "(no such group)" : "(group not yet closed)");
                        throw makeException("invalid backreference \\" + backRef + " " + explanation);
                    }
                    return new BackReference(backRef, caseBlind);
                } else {
                    throw makeException("digit not allowed after \\");
                }
            case '$':
                if (isXPath) {
                    break;
                }
                // otherwise fall through
            default:
                throw makeException("invalid escape sequence");
        }
        CharClass tem = new SingleChar(curChar, true);
        advance();
        return tem;
    }

    private CharClass parseProp(boolean complement) throws RegexSyntaxException {
        expect('{');
        int start = pos;
        for (; ;) {
            advance();
            if (curChar == '}')
                break;
            if (!isAsciiAlnum(curChar) && curChar != '-')
                expect('}');
        }
        CharSequence propertyNameCS = regExp.subSequence(start, pos - 1);
        if (ignoreWhitespace && !inCharClassExpr) {
            propertyNameCS = Whitespace.removeAllWhitespace(propertyNameCS);
        }
        String propertyName = propertyNameCS.toString();
        advance();
        switch (propertyName.length()) {
            case 0:
                throw makeException("empty property name");
            case 2:
                return getSubCategoryCharClass(propertyName);
            case 1:
                return getCategoryCharClass(propertyName.charAt(0));
            default:
                if (propertyName.startsWith("Is")) {
                    String blockName = propertyName.substring(2);
                    CharClass cc = UnicodeBlocks.getBlock(blockName);
                    if (cc != null) {
                        return cc;
                    } else {
                        // unrecognized block name: in XSD 1.1 both \p and \P match everything
                        if (xsdVersion == Configuration.XSD11) {
                            //noinspection ThrowableResultOfMethodCallIgnored
                            warnings.add(makeException("invalid block name", blockName));
                            if (complement) {
                                return Empty.getInstance();
                            } else {
                                return Complement.makeComplement(Empty.getInstance());
                            }
                        } else {
                            throw makeException("invalid block name", blockName);
                        }
                    }
                } else {
                    break;
                }
        }
        throw makeException("invalid property name", propertyName);
    }

    private CharClass parseCharClassExpr() throws RegexSyntaxException {
        boolean compl;
        if (curChar == '^') {
            advance();
            compl = true;
        } else {
            compl = false;
        }
        List<CharClass> members = new ArrayList<CharClass>(10);
        //boolean firstOrLast = true;
        do {
            CharClass lower = parseCharClassEscOrXmlChar();
            members.add(lower);
            if (curChar == ']' || eos) {
                addCaseVariant(lower, members);
                break;
            }
            //firstOrLast = isLastInGroup();
            if (curChar == '-') {
                char next = regExp.charAt(pos);
                if (next == '[') {
                    // hyphen denotes subtraction
                    addCaseVariant(lower, members);
                    advance();
                    break;
                } else if (next == ']') {
                    // XSD 1.1 states that hyphen must be followed by another character (so [+-] is disallowed)
                    // We enforce this rule only if using XPath 3.0 mode, as it breaks existing schemas
                    if (isXPath30) {
                        throw makeException("hyphen in a character range must be followed by a single character");
                    } else {
                        // hyphen denotes a regular character - no need to do anything
                        addCaseVariant(lower, members);
                    }
                } else {
                    // hyphen denotes a character range
                    advance();
                    CharClass upper = parseCharClassEscOrXmlChar();
                    if (lower.getSingleChar() < 0 || upper.getSingleChar() < 0) {
                        throw makeException("the ends of a range must be single characters");
                    }
                    if (lower.getSingleChar() > upper.getSingleChar()) {
                        throw makeException("invalid range (start > end)");
                    }
                    if (lower instanceof SingleChar && lower.getSingleChar() == '-' && !((SingleChar)lower).isEscaped) {
                        throw makeException("range cannot start with unescaped hyphen");
                    }
                    if (upper instanceof SingleChar && upper.getSingleChar() == '-' && !((SingleChar)upper).isEscaped) {
                        throw makeException("range cannot end with unescaped hyphen");
                    }
                    members.set(members.size() - 1,
                            new CharRange(lower.getSingleChar(), upper.getSingleChar()));
                    if (caseBlind) {
                        // Special-case A-Z and a-z
                        if (lower.getSingleChar() == 'a' && upper.getSingleChar() == 'z') {
                            members.add(new CharRange('A', 'Z'));
                            for (int v=0; v<CaseVariants.ROMAN_VARIANTS.length; v++) {
                                members.add(new SingleChar(CaseVariants.ROMAN_VARIANTS[v]));
                            }
                        } else if (lower.getSingleChar() == 'A' && upper.getSingleChar() == 'Z') {
                            members.add(new CharRange('a', 'z'));
                            for (int v=0; v<CaseVariants.ROMAN_VARIANTS.length; v++) {
                                members.add(new SingleChar(CaseVariants.ROMAN_VARIANTS[v]));
                            }
                        } else {
                            for (int k = lower.getSingleChar(); k <= upper.getSingleChar(); k++) {
                                int[] variants = CaseVariants.getCaseVariants(k);
                                for (int variant : variants) {
                                    members.add(new SingleChar(variant));
                                }
                            }
                        }
                    }
                    // look for a subtraction
                    if (curChar == '-' && regExp.charAt(pos) == '[') {
                        advance();
                        //expect('[');
                        break;
                    }
                }
            } else {
                addCaseVariant(lower, members);
            }
        } while (curChar != ']');
        if (eos) {
            expect(']');
        }
        CharClass result;
        if (members.size() == 1)
            result = members.get(0);
        else
            result = new Union(members);
        if (compl)
            result = Complement.makeComplement(result);
        if (curChar == '[') {
            advance();
            result = new Subtraction(result, parseCharClassExpr());
            expect(']');
        }
        inCharClassExpr = false;
        advance();
        return result;
    }

    private void addCaseVariant(CharClass lower, List<CharClass> members) {
        if (caseBlind) {
            int[] variants = CaseVariants.getCaseVariants(lower.getSingleChar());
            for (int variant : variants) {
                members.add(new SingleChar(variant));
            }
        }
    }

    private CharClass parseCharClassEscOrXmlChar() throws RegexSyntaxException {
        switch (curChar) {
            case RegexData.EOS:
                if (eos)
                    expect(']');
                break;
            case '\\':
                advance();
                return parseEsc();
            case '[':
            case ']':
                throw makeException("character must be escaped", new String(new char[]{curChar}));
            case '-':
                break;
        }
        CharClass tem = new SingleChar(absorbSurrogatePair());
        advance();
        return tem;
    }


    private static synchronized CharClass getCategoryCharClass(char category) throws RegexSyntaxException {
        List<CharClass> ranges = new ArrayList<CharClass>(10);
        for (String sub : Categories.CATEGORIES.keySet()) {
            if (sub.charAt(0) == category) {
                ranges.add(getSubCategoryCharClass(sub));
            }
        }
        if (ranges.isEmpty()) {
            throw new RegexSyntaxException("Unknown category " + category);
        }
        return new Union(ranges);
    }



    private static CharClass getSubCategoryCharClass(String category) throws RegexSyntaxException {
        int[] codes = Categories.CATEGORIES.get(category);
        if (codes == null) {
            throw new RegexSyntaxException("Unknown category " + category);
        }
        List<CharClass> ranges = new ArrayList<CharClass>(codes.length/2);
        for (int i=0; i<codes.length; i+=2) {
            int start = codes[i];
            int end = codes[i+1];
            if (start == end) {
                ranges.add(new SingleChar(start));
            } else {
                ranges.add(new CharRange(start, end));
            }
        }
        return new Union(ranges);
    }



    /**
     * Main method for testing. Outputs to System.err the Java translation of a supplied
     * regular expression
     * @param args command line arguments
     *        arg[0] a regular expression
     *        arg[1] = xpath to invoke the XPath rules
     * @throws RegexSyntaxException if the regex is invalid
     */

    public static void main(String[] args) throws RegexSyntaxException {
        String s = translate(args[0], RegularExpression.XML11|RegularExpression.XPATH20|RegularExpression.XPATH30, 0, null);
        System.err.println(StringValue.diagnosticDisplay(s));
        try {
            Pattern.compile(s);
        } catch (Exception err) {
            System.err.println("Error: " + err.getMessage());
        }
        System.err.println();
    }


//}


}

/*
Copyright (c) 2001-2003 Thai Open Source Software Center Ltd
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

    Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in
    the documentation and/or other materials provided with the
    distribution.

    Neither the name of the Thai Open Source Software Center Ltd nor
    the names of its contributors may be used to endorse or promote
    products derived from this software without specific prior written
    permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file except changes marked.
//
// The Initial Developer of the Original Code is James Clark
//
// Portions created by Saxonica Limited are Copyright (C) Saxonica Limited 2011. All Rights Reserved.
//
// Contributor(s): Saxonica Limited
//

