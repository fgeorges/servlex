package org.expath.servlex.tools.regex;

import java.util.HashMap;
import java.util.Map;

/**
 * Non-instantiable class containing constant data definitions used by the various Regular Expression translators
 */
public class RegexData {

    public static final String categories = "LMNPZSC";
    public static final String subCategories = "LuLlLtLmLoMnMcMeNdNlNoPcPdPsPePiPfPoZsZlZpSmScSkSoCcCfCoCn";

    public static final char EOS = '\0';


    // The non-BMP characters in each category. Updated in 9.4 to Unicode 6.0.0

    public static Map<String, int[]> ASTRAL_CATEGORIES = new HashMap<String, int[]>(20);

    static {
        ASTRAL_CATEGORIES.put("Cf", new int[]{
                0x110bd, 0x110bd,
                0x1d173, 0x1d17a,
                0xe0001, 0xe0001,
                0xe0020, 0xe007f
        });
        ASTRAL_CATEGORIES.put("Co", new int[]{
                0xf0000, 0xffffd,
                0x100000, 0x10fffd
        });
        ASTRAL_CATEGORIES.put("Ll", new int[]{
                0x10428, 0x1044f,
                0x1d41a, 0x1d433,
                0x1d44e, 0x1d454,
                0x1d456, 0x1d467,
                0x1d482, 0x1d49b,
                0x1d4b6, 0x1d4b9,
                0x1d4bb, 0x1d4bb,
                0x1d4bd, 0x1d4c3,
                0x1d4c5, 0x1d4cf,
                0x1d4ea, 0x1d503,
                0x1d51e, 0x1d537,
                0x1d552, 0x1d56b,
                0x1d586, 0x1d59f,
                0x1d5ba, 0x1d5d3,
                0x1d5ee, 0x1d607,
                0x1d622, 0x1d63b,
                0x1d656, 0x1d66f,
                0x1d68a, 0x1d6a5,
                0x1d6c2, 0x1d6da,
                0x1d6dc, 0x1d6e1,
                0x1d6fc, 0x1d714,
                0x1d716, 0x1d71b,
                0x1d736, 0x1d74e,
                0x1d750, 0x1d755,
                0x1d770, 0x1d788,
                0x1d78a, 0x1d78f,
                0x1d7aa, 0x1d7c2,
                0x1d7c4, 0x1d7c9,
                0x1d7cb, 0x1d7cb
        });
        ASTRAL_CATEGORIES.put("Lo", new int[]{
                0x10000, 0x1000b,
                0x1000d, 0x10026,
                0x10028, 0x1003a,
                0x1003c, 0x1003d,
                0x1003f, 0x1004d,
                0x10050, 0x1005d,
                0x10080, 0x100fa,
                0x102a0, 0x102d0,
                0x10300, 0x1031e,
                0x10330, 0x10340,
                0x10342, 0x10349,
                0x10380, 0x1039d,
                0x103cb, 0x103cf,
                0x10450, 0x1049d,
                0x10800, 0x10805,
                0x10808, 0x10808,
                0x1080a, 0x10835,
                0x10837, 0x10838,
                0x1083c, 0x1083c,
                0x1083f, 0x10855,
                0x10900, 0x10915,
                0x10920, 0x10939,
                0x10a00, 0x10a00,
                0x10A10, 0x10A13,
                0x10A15, 0x10A17,
                0x10A19, 0x10A33,
                0x10A60, 0x10A7C,
                0x10B00, 0x10B35,
                0x10B40, 0x10B55,
                0x10B60, 0x10B72,
                0x10C00, 0x10C48,
                0x11003, 0x11037,
                0x11083, 0x110AF,
                0x12000, 0x1236E,
                0x13000, 0x1342E,
                0x16800, 0x16A38,
                0x1B000, 0x1B001,
                0x20000, 0x2a6d6,
                0x2a700, 0x2b734,
                0x2b740, 0x2b81d,
                0x2f800, 0x2fa1d
        });
        ASTRAL_CATEGORIES.put("Lu", new int[]{
                0x10400, 0x10427,
                0x1d400, 0x1d419,
                0x1d434, 0x1d44d,
                0x1d468, 0x1d481,
                0x1d49c, 0x1d49c,
                0x1d49e, 0x1d49f,
                0x1d4a2, 0x1d4a2,
                0x1d4a5, 0x1d4a6,
                0x1d4a9, 0x1d4ac,
                0x1d4ae, 0x1d4b5,
                0x1d4d0, 0x1d4e9,
                0x1d504, 0x1d505,
                0x1d507, 0x1d50a,
                0x1d50d, 0x1d514,
                0x1d516, 0x1d51c,
                0x1d538, 0x1d539,
                0x1d53b, 0x1d53e,
                0x1d540, 0x1d544,
                0x1d546, 0x1d546,
                0x1d54a, 0x1d550,
                0x1d56c, 0x1d585,
                0x1d5a0, 0x1d5b9,
                0x1d5d4, 0x1d5ed,
                0x1d608, 0x1d621,
                0x1d63c, 0x1d655,
                0x1d670, 0x1d689,
                0x1d6a8, 0x1d6c0,
                0x1d6e2, 0x1d6fa,
                0x1d71c, 0x1d734,
                0x1d756, 0x1d76e,
                0x1d790, 0x1d7a8,
                0x1d7ca, 0x1d7ca
        });
        ASTRAL_CATEGORIES.put("Mc", new int[]{
                0x11000, 0x11000,
                0x11002, 0x11002,
                0x11082, 0x11082,
                0x110B0, 0x110B2,
                0x110B7, 0x110B8,
                0x1d165, 0x1d166,
                0x1d16d, 0x1d172
        });
        ASTRAL_CATEGORIES.put("Mn", new int[]{
                0x101FD, 0x101FD,
                0x10A01, 0x10A03,
                0x10A05, 0x10A06,
                0x10A0C, 0x10A0F,
                0x10A38, 0x10A3A,
                0x10A3F, 0x10A3F,
                0x11001, 0x11001,
                0x11038, 0x11046,
                0x11080, 0x11081,
                0x110B3, 0x110B6,
                0x110B9, 0x110BA,

                0x1d167, 0x1d169,
                0x1d17b, 0x1d182,
                0x1d185, 0x1d18b,
                0x1d1aa, 0x1d1ad,
                0x1D242, 0x1D244,
                0xe0100, 0xe01ef
        });

        ASTRAL_CATEGORIES.put("Nd", new int[]{
                0x104a0, 0x104a9,
                0x11066, 0x1106f,
                0x1d7ce, 0x1d7ff
        });
        ASTRAL_CATEGORIES.put("Nl", new int[]{
                0x10140, 0x10174,
                0x10341, 0x10341,
                0x1034a, 0x1034a,
                0x103D1, 0x103D5,
                0x12400, 0x12462,
        });
        ASTRAL_CATEGORIES.put("No", new int[]{
                0x10107, 0x10133,
                0x10175, 0x10178,
                0x1018A, 0x1018A,
                0x10320, 0x10323,
                0x10858, 0x1085F,
                0x10916, 0x1091B,
                0x10A40, 0x10A47,
                0x10A7D, 0x10A7E,
                0x10B58, 0x10B5F,
                0x10B78, 0x10B7F,
                0x10E60, 0x10E7E,
                0x11052, 0x11065,
                0x1D360, 0x1D371,
                0x1F100, 0x1F10A,
        });
        ASTRAL_CATEGORIES.put("Po", new int[]{
                0x10100, 0x10101,
                0x1039f, 0x1039f,
                0x103D0, 0x103D0,
                0x10857, 0x10857,
                0x1091F, 0x1091F,
                0x1093F, 0x1093F,
                0x10A50, 0x10A58,
                0x10A7F, 0x10A7F,
                0x10B39, 0x10B3F,
                0x11047, 0x1104D,
                0x110BB, 0x110BC,
                0x110BE, 0x110C1,
                0x12470, 0x12473,
        });


        ASTRAL_CATEGORIES.put("So", new int[]{
                0x10102, 0x10102,
                0x10137, 0x1013f,
                0x10179, 0x10189,
                0x10190, 0x1019B,
                0x101D0, 0x101FC,
                0x1d000, 0x1d0f5,
                0x1d100, 0x1d126,
                0x1d129, 0x1d164,
                0x1d16a, 0x1d16c,
                0x1d183, 0x1d184,
                0x1d18c, 0x1d1a9,
                0x1d1ae, 0x1d1dd,
                0x1D200, 0x1D241,
                0x1D245, 0x1D245,
                0x1d300, 0x1d356,
                0x1F000, 0x1F02B,
                0x1F030, 0x1F093,
                0x1F0A0, 0x1F0AE,
                0x1F0B1, 0x1F0BE,
                0x1F0C1, 0x1F0CF,
                0x1F0D1, 0x1F0DF,
                0x1F110, 0x1F12E,
                0x1F130, 0x1F169,
                0x1F170, 0x1F19A,
                0x1F1E6, 0x1F202,
                0x1F210, 0x1F23A,
                0x1F240, 0x1F248,
                0x1F250, 0x1F251,
                0x1F300, 0x1F320,
                0x1F330, 0x1F335,
                0x1F337, 0x1F37C,
                0x1F380, 0x1F393,
                0x1F3A0, 0x1F3C4,
                0x1F3C6, 0x1F3CA,
                0x1F3E0, 0x1F3F0,
                0x1F400, 0x1F43E,
                0x1F440, 0x1F440,
                0x1F442, 0x1F4F7,
                0x1F4F9, 0x1F4FC,
                0x1F500, 0x1F53D,
                0x1F550, 0x1F567,
                0x1F5FB, 0x1F5FF,
                0x1F601, 0x1F610,
                0x1F612, 0x1F614,
                0x1F616, 0x1F616,
                0x1F618, 0x1F618,
                0x1F61A, 0x1F61A,
                0x1F61C, 0x1F61E,
                0x1F620, 0x1F625,
                0x1F628, 0x1F62B,
                0x1F62D, 0x1F62D,
                0x1F630, 0x1F633,
                0x1F635, 0x1F640,
                0x1F645, 0x1F64F,
                0x1F680, 0x1F6C5,
                0x1F700, 0x1F773

        });
        ASTRAL_CATEGORIES.put("Sm", new int[]{
                0x1d6c1, 0x1d6c1,
                0x1d6db, 0x1d6db,
                0x1d6fb, 0x1d6fb,
                0x1d715, 0x1d715,
                0x1d735, 0x1d735,
                0x1d74f, 0x1d74f,
                0x1d76f, 0x1d76f,
                0x1d789, 0x1d789,
                0x1d7a9, 0x1d7a9,
                0x1d7c3, 0x1d7c3
        });
    }

    // end of generated code

// This file was automatically generated by NamingExceptionsGen
// class NamingExceptions {

//    public static final String NMSTRT_INCLUDES =
//            "\u003A\u005F\u02BB\u02BC\u02BD\u02BE\u02BF\u02C0\u02C1\u0559" +
//            "\u06E5\u06E6\u212E";
//    public static final String NMSTRT_EXCLUDE_RANGES =
//            "\u00AA\u00BA\u0132\u0133\u013F\u0140\u0149\u0149\u017F\u017F" +
//            "\u01C4\u01CC\u01F1\u01F3\u01F6\u01F9\u0218\u0233\u02A9\u02AD" +
//            "\u03D7\u03D7\u03DB\u03DB\u03DD\u03DD\u03DF\u03DF\u03E1\u03E1" +
//            "\u0400\u0400\u040D\u040D\u0450\u0450\u045D\u045D\u048C\u048F" +
//            "\u04EC\u04ED\u0587\u0587\u06B8\u06B9\u06BF\u06BF\u06CF\u06CF" +
//            "\u06FA\u07A5\u0950\u0950\u0AD0\u0AD0\u0D85\u0DC6\u0E2F\u0E2F" +
//            "\u0EAF\u0EAF\u0EDC\u0F00\u0F6A\u1055\u1101\u1101\u1104\u1104" +
//            "\u1108\u1108\u110A\u110A\u110D\u110D\u1113\u113B\u113D\u113D" +
//            "\u113F\u113F\u1141\u114B\u114D\u114D\u114F\u114F\u1151\u1153" +
//            "\u1156\u1158\u1162\u1162\u1164\u1164\u1166\u1166\u1168\u1168" +
//            "\u116A\u116C\u116F\u1171\u1174\u1174\u1176\u119D\u119F\u11A2" +
//            "\u11A9\u11AA\u11AC\u11AD\u11B0\u11B6\u11B9\u11B9\u11BB\u11BB" +
//            "\u11C3\u11EA\u11EC\u11EF\u11F1\u11F8\u1200\u18A8\u207F\u2124" +
//            "\u2128\u2128\u212C\u212D\u212F\u217F\u2183\u3006\u3038\u303A" +
//            "\u3131\u4DB5\uA000\uA48C\uF900\uFFDC";
//    public static final String NMSTRT_CATEGORIES = "LlLuLoLtNl";
//    public static final String NMCHAR_INCLUDES =
//            "\u002D\u002E\u003A\u005F\u00B7\u0387\u06dd\u212E"; // MHK: added 06dd
//    public static final String NMCHAR_EXCLUDE_RANGES =
//            "\u00AA\u00B5\u00BA\u00BA\u0132\u0133\u013F\u0140\u0149\u0149" +
//            "\u017F\u017F\u01C4\u01CC\u01F1\u01F3\u01F6\u01F9\u0218\u0233" +
//            "\u02A9\u02B8\u02E0\u02EE\u0346\u034E\u0362\u037A\u03D7\u03D7" +
//            "\u03DB\u03DB\u03DD\u03DD\u03DF\u03DF\u03E1\u03E1\u0400\u0400" +
//            "\u040D\u040D\u0450\u0450\u045D\u045D\u0488\u048F\u04EC\u04ED" +
//            "\u0587\u0587\u0653\u0655\u06B8\u06B9\u06BF\u06BF\u06CF\u06CF" +
//            "\u06FA\u07B0\u0950\u0950\u0AD0\u0AD0\u0D82\u0DF3\u0E2F\u0E2F" +
//            "\u0EAF\u0EAF\u0EDC\u0F00\u0F6A\u0F6A\u0F96\u0F96\u0FAE\u0FB0" +
//            "\u0FB8\u0FB8\u0FBA\u1059\u1101\u1101\u1104\u1104\u1108\u1108" +
//            "\u110A\u110A\u110D\u110D\u1113\u113B\u113D\u113D\u113F\u113F" +
//            "\u1141\u114B\u114D\u114D\u114F\u114F\u1151\u1153\u1156\u1158" +
//            "\u1162\u1162\u1164\u1164\u1166\u1166\u1168\u1168\u116A\u116C" +
//            "\u116F\u1171\u1174\u1174\u1176\u119D\u119F\u11A2\u11A9\u11AA" +
//            "\u11AC\u11AD\u11B0\u11B6\u11B9\u11B9\u11BB\u11BB\u11C3\u11EA" +
//            "\u11EC\u11EF\u11F1\u11F8\u1200\u18A9\u207F\u207F\u20DD\u20E0" +
//            "\u20E2\u2124\u2128\u2128\u212C\u212D\u212F\u217F\u2183\u2183" +
//            "\u3006\u3006\u3038\u303A\u3131\u4DB5\uA000\uA48C\uF900\uFFDC";
//    public static final String NMCHAR_CATEGORIES = "LlLuLoLtNlMcMeMnLmNd";
// end of generated code

    public static final char UNICODE_3_1_ADD_Lu = '\u03F4';   // added in 3.1
    public static final char UNICODE_3_1_ADD_Ll = '\u03F5';   // added in 3.1
    // 3 characters changed from No to Nl between 3.0 and 3.1
    public static final char UNICODE_3_1_CHANGE_No_to_Nl_MIN = '\u16EE';
    public static final char UNICODE_3_1_CHANGE_No_to_Nl_MAX = '\u16F0';
    public static final String CATEGORY_Pi = "\u00AB\u2018\u201B\u201C\u201F\u2039"; // Java doesn't know about category Pi
    /*@NotNull*/
    public static final String CATEGORY_Pf = "\u00BB\u2019\u201D\u203A"; // Java doesn't know about category Pf

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