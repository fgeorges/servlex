package org.expath.servlex.tools.regex;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
//import net.sf.saxon.om.Axis;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Whitespace;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides knowledge of the names and contents of Unicode character blocks,
 * as referenced using the \p{IsXXXXX} construct in a regular expression. The underlying
 * data is in an XML resource file UnicodeBlocks.xml
 */
public class UnicodeBlocks {

    private static Map<String, JDK15RegexTranslator.CharClass> blocks = null;

    public static JDK15RegexTranslator.CharClass getBlock(String name) throws RegexSyntaxException {
        throw new UnsupportedOperationException("Not supported, relies on Saxon 9.4 class Axis...");
//        if (blocks == null) {
//            readBlocks(new Configuration());
//        }
//        JDK15RegexTranslator.CharClass cc = blocks.get(name);
//        if (cc != null) {
//            return cc;
//        }
//        cc = blocks.get(normalizeBlockName(name));
//        return cc;
    }

//    private static String normalizeBlockName(String name) {
//        FastStringBuffer fsb = new FastStringBuffer(name.length());
//        for (int i=0; i<name.length(); i++) {
//            final char c = name.charAt(i);
//            switch (c) {
//                case ' ': case '\t': case '\r': case '\n': case '_':
//                    // no action
//                    break;
//                default:
//                    fsb.append(c);
//            }
//        }
//        return fsb.toString();
//    }
//
//    private synchronized static void readBlocks(Configuration config) throws RegexSyntaxException {
//        blocks = new HashMap<String, JDK15RegexTranslator.CharClass>(250);
//        InputStream in = Configuration.locateResource("unicodeBlocks.xml", new ArrayList<String>(), new ArrayList<ClassLoader>());
//        if (in == null) {
//            throw new RegexSyntaxException("Unable to read unicodeBlocks.xml file");
//        }
//
//        ParseOptions options = new ParseOptions();
//        options.setSchemaValidationMode(Validation.SKIP);
//        options.setStripSpace(Whitespace.ALL);
//        DocumentInfo doc;
//        try {
//            doc = config.buildDocument(new StreamSource(in, "unicodeBlocks.xml"), options);
//        } catch (XPathException e) {
//            throw new RegexSyntaxException("Failed to process unicodeBlocks.xml: " + e.getMessage());
//        }
//
//        AxisIterator iter = doc.iterateAxis(Axis.DESCENDANT, new NameTest(Type.ELEMENT, "", "block", config.getNamePool()));
//        while (true) {
//            NodeInfo item = iter.next();
//            if (item == null) {
//                break;
//            }
//            String blockName = normalizeBlockName(item.getAttributeValue("", "name"));
//            JDK15RegexTranslator.CharClass range = null;
//            AxisIterator ranges = item.iterateAxis(Axis.CHILD, NodeKindTest.ELEMENT);
//            while (true) {
//                NodeInfo rangeElement = ranges.next();
//                if (rangeElement == null) {
//                    break;
//                }
//                int from = Integer.parseInt(rangeElement.getAttributeValue("", "from").substring(2), 16);
//                int to = Integer.parseInt(rangeElement.getAttributeValue("", "to").substring(2), 16);
//                JDK15RegexTranslator.CharClass cr = new JDK15RegexTranslator.CharRange(from, to);
//                if (range == null) {
//                    range = cr;
//                } else {
//                    range = new JDK15RegexTranslator.Union(range, cr);
//                }
//            }
//            blocks.put(blockName, range);
//        }
//
//    }
}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Saxonica Limited.
// Portions created by ___ are Copyright (C) ___. All rights reserved.
//
// Contributor(s):
//