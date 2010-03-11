package com.solsort.mobile;

class LightScriptOpCodes {
    public static final int NONE = 127;
    public static final int TRUE = 0;
    public static final int FALSE = 1;
    public static final int UNDEFINED = 2;
    public static final int NULL = 3;
    public static final int PAREN = 4;
    public static final int LIST_LITERAL = 5;
    public static final int CURLY = 6;
    public static final int VAR = 7;
    public static final int BUILD_FUNCTION = 8;
    public static final int IF = 9;
    public static final int WHILE = 10;
    public static final int CALL_FUNCTION = 11;
    public static final int AND = 12;
    public static final int OR = 13;
    public static final int ELSE = 14;
    public static final int SET = 15;
    public static final int IDENT = 16;
    public static final int BLOCK = 17;
    public static final int SEP = 18;
    public static final int IN = 19;
    public static final int FOR = 20;
    public static final int END = 21;
    public static final int CATCH = 22;
    public static final int DO = 23;
    public static final int INC = 24;
    public static final int DEC = 25;
    public static final int ADD = 26;
    public static final int EQUALS = 27;
    public static final int LESS = 29;
    public static final int LESS_EQUAL = 30;
    public static final int LITERAL = 31;
    public static final int MUL = 32;
    public static final int NEG = 33;
    public static final int NOT = 34;
    public static final int NOT_EQUALS = 35;
    public static final int REM = 37;
    public static final int RETURN = 38;
    public static final int SHIFT_RIGHT_ARITHMETIC = 39;
    public static final int SUB = 40;
    public static final int SUBSCRIPT = 41;
    public static final int THIS = 42;
    public static final int THROW = 43;
    public static final int TRY = 44;
    public static final int UNTRY = 45;
    public static final int BOX_IT = 46;
    public static final int BUILD_FN = 47;
    public static final int CALL_FN = 48;
    public static final int DROP = 49;
    public static final int DUP = 50;
    public static final int GET_BOXED = 52;
    public static final int GET_BOXED_CLOSURE = 53;
    public static final int GET_CLOSURE = 54;
    public static final int GET_LOCAL = 55;
    public static final int INC_SP = 56;
    public static final int JUMP = 57;
    public static final int JUMP_IF_FALSE = 58;
    public static final int JUMP_IF_TRUE = 59;
    public static final int NEW_DICT = 60;
    public static final int NEW_LIST = 61;
    public static final int NEXT = 62;
    public static final int PUSH = 64;
    public static final int PUT = 65;
    public static final int SAVE_PC = 66;
    public static final int SET_BOXED = 67;
    public static final int SET_CLOSURE = 68;
    public static final int SET_LOCAL = 69;
    public static final int SET_THIS = 70;
    public static final int SWAP = 71;
    public static final int DIV = 72;
    public static final int NEW_ITER = 73;
    public static final int JUMP_IF_UNDEFINED = 74;
    public static final int DELETE = 75;
    public static final int NEW = 76;
    public static final int GLOBAL = 77;
    public static final int SHIFT_RIGHT = 78;
    public static final int SHIFT_LEFT = 79;
    public static final int BITWISE_OR = 81;
    public static final int BITWISE_XOR = 82;
    public static final int BITWISE_AND = 83;
    public static final int BITWISE_NOT = 84;

    private static final String[] idNames = {
        "", "", "", "", "PAREN", "LIST_LITERAL", "CURLY", "VAR",
        "BUILD_FUNCTION", "IF", "WHILE", "CALL_FUNCTION", "AND",
        "OR", "ELSE", "SET", "IDENT", "BLOCK", "SEP", "IN", "FOR",
        "END", "CATCH", "DO", "INC", "DEC", "ADD", "EQUALS",
        "NOT_USED_ANYMORE", "LESS", "LESS_EQUAL", "LITERAL", "MUL", "NEG",
        "NOT", "NOT_EQUALS", "NOT_USED_ANYMORE", "REM", "RETURN", ">>",
        "SUB", "SUBSCRIPT", "THIS", "THROW", "TRY", "UNTRY", "BOX_IT",
        "BUILD_FN", "CALL_FN", "DROP", "DUP", "NOT_USED_ANYMORE",
        "GET_BOXED", "GET_BOXED_CLOSURE", "GET_CLOSURE", "GET_LOCAL",
        "INC_SP", "JUMP", "JUMP_IF_FALSE", "JUMP_IF_TRUE", "NEW_DICT",
        "NEW_LIST", "NEXT", "NOT_USED_ANYMORE", "PUSH", "PUT", "SAVE_PC",
        "SET_BOXED", "SET_CLOSURE", "SET_LOCAL", "SET_THIS", "SWAP",
        "DIV", "NEW_ITER", "JUMP_IF_UNDEFINED", "DELETE", "NEW", "GLOBAL",
        "SHIFT_RIGHT", "SHIFT_LEFT", "BITWISE_OR", "BITWISE_XOR", "BITWISE_AND",
        "OpCodes.BITWISE_NOT"
    };

    /** LightScriptFunction that maps from ID to a string representation of the ID,
     * robust for integers which is not IDs */
    public static String idName(int id) {
        return "" + id + ((id > 0 && id < idNames.length) ? idNames[id] : "");
    }

    /** A toString, that also works nicely on arrays, and LightScript code */
    public static String stringify(Object o) {
            if (o == null) {
                return "null";
            } else if (o instanceof Object[]) {
                StringBuffer sb = new StringBuffer();
                Object[] os = (Object[]) o;
                sb.append("[");
                if (os.length > 0 && os[0] instanceof Integer) {
                    int id = ((Integer) os[0]).intValue();
                    sb.append(idName(id));
                } else if (os.length > 0) {
                    sb.append(os[0]);
                }
                for (int i = 1; i < os.length; i++) {
                    sb.append(" " + stringify(os[i]));
                }
                sb.append("]");
                return sb.toString();
            } else if (o instanceof LightScriptCode) {
                LightScriptCode c = (LightScriptCode) o;
                StringBuffer sb = new StringBuffer();
                sb.append("closure" + c.argc + "{\n\tcode:");
                for (int i = 0; i < c.code.length; i++) {
                    sb.append(" ");
                    sb.append(idName(c.code[i]));
                }
                sb.append("\n\tclosure:");
                for (int i = 0; i < c.closure.length; i++) {
                    sb.append(" " + i + ":");
                    sb.append(stringify(c.closure[i]));
                }
                sb.append("\n\tconstPool:");
                for (int i = 0; i < c.constPool.length; i++) {
                    sb.append(" " + i + ":");
                    sb.append(stringify(c.constPool[i]));
                }
                sb.append("\n}");
                return sb.toString();
            } else {
                return o.toString();
            }
    }
    /*`\subsection{StdLibity classes}'*/
    /*`\subsubsection{StdLib}'*/

}
