
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

// class LightScript
// - Constants
// - Utility functions
// - Utility classes and structs
// - Properties and constructor
// - Tokeniser
// - Parser
// - Compiler
// - Virtual machine
class LightScript {

    public Closure nextClosure() {
        Object[] os = parse(0);
        varsClosure = varsUsed;
        return compile(os);
    }

    ////////////////////////
    ////// Constants //////
    //////////////////////
    /////////////////////
    ////////////////////
    ///////////////////
    //////////////////
    private static final char ID_PAREN = 1;
    private static final char ID_LIST_LITERAL = 2;
    private static final char ID_CURLY = 3;
    private static final char ID_VAR = 4;
    private static final char ID_RETURN = 5;
    private static final char ID_NOT = 6;
    private static final char ID_BUILD_FUNCTION = 7;
    private static final char ID_IF = 8;
    private static final char ID_WHILE = 9;
    private static final char ID_LITERAL = 10;
    private static final char ID_CALL_FUNCTION = 11;
    private static final char ID_SUBSCRIPT = 12;
    private static final char ID_MUL = 13;
    private static final char ID_REM = 14;
    private static final char ID_ADD = 15;
    private static final char ID_SUB = 16;
    private static final char ID_NEG = 17;
    private static final char ID_EQUALS = 18;
    private static final char ID_NOT_EQUALS = 19;
    private static final char ID_LESS_EQUALS = 20;
    private static final char ID_LESS = 21;
    private static final char ID_AND = 22;
    private static final char ID_OR = 23;
    private static final char ID_ELSE = 24;
    private static final char ID_SET = 25;
    private static final char ID_IDENT = 26;
    private static final char ID_ENSURE_STACKSPACE = 27;
    private static final char ID_INC_SP = 28;
    private static final char ID_XXX1 = 29;
    private static final char ID_SAVE_PC = 30;
    private static final char ID_CALL_FN = 31;
    private static final char ID_BUILD_FN = 32;
    private static final char ID_SET_BOXED = 33;
    private static final char ID_SET_LOCAL = 34;
    private static final char ID_SET_CLOSURE = 35;
    private static final char ID_GET_BOXED = 36;
    private static final char ID_GET_LOCAL = 37;
    private static final char ID_GET_CLOSURE = 38;
    // get a value from the closure without unboxing it
    private static final char ID_GET_BOXED_CLOSURE = 39;
    // private static final char ID_GET_LITERAL = 40;
    // box a value
    private static final char ID_BOX_IT = 41;
    private static final char ID_PRINT = 42;
    private static final char ID_DROP = 43;
    private static final char ID_PUSH_NIL = 44;
    private static final char ID_XXX2 = 45;
    private static final char ID_XXX3 = 46;
    private static final char ID_XXX4 = 47;
    private static final char ID_XXX5 = 48;
    private static final char ID_DIV = 49;
    private static final char ID_XXX6 = 50;
    private static final char ID_IS_INT = 51;
    private static final char ID_IS_STR = 52;
    private static final char ID_IS_LIST = 53;
    private static final char ID_IS_DICT = 54;
    private static final char ID_IS_ITER = 55;
    private static final char ID_EQUAL = 56;
    private static final char ID_IS_EMPTY = 57;
    private static final char ID_PUT = 58;
    private static final char ID_GET = 59;
    private static final char ID_RAND = 60;
    private static final char ID_SIZE = 61;
    private static final char ID_XXX7 = 62;
    private static final char ID_LESSEQUAL = 63;
    private static final char ID_SUBSTR = 64;
    private static final char ID_RESIZE = 65;
    private static final char ID_PUSH = 66;
    private static final char ID_POP = 67;
    private static final char ID_KEYS = 68;
    private static final char ID_VALUES = 69;
    private static final char ID_NEXT = 70;
    private static final char ID_ASSERT = 71;
    private static final char ID_JUMP = 72;
    private static final char ID_JUMP_IF_TRUE = 73;
    private static final char ID_DUP = 74;
    private static final char ID_NEW_LIST = 75;
    private static final char ID_NEW_DICT = 76;
    private static final char ID_NEW_STRINGBUFFER = 77;
    private static final char ID_STR_APPEND = 78;
    private static final char ID_TO_STRING = 79;
    private static final char ID_SWAP = 80;
    private static final char ID_BLOCK = 81;
    private static final char ID_SEP = 82;
    private static final char ID_IN = 83;
    private static final char ID_JUMP_IF_FALSE = 84;

    private static final Object[] END_TOKEN = {"(end)"};
    private static final Object[] SEP_TOKEN = { new Integer(ID_SEP)};
    private static final Boolean TRUE = new Boolean(true);

    private static final String[] idNames = {"", "PAREN", "LIST_LITERAL",
        "CURLY", "VAR", "RETURN", "NOT", "FUNCTION", "IF", "WHILE",
        "LITERAL", "CALL_FUNCTION", "SUBSCRIPT", "MUL", "REM", "ADD", 
        "SUB", "NEG", "EQUALS", "NOT_EQUALS", "LEQ", "LESS", "AND", "OR", 
        "ELSE", "SET", "IDENT", "ENSURE_STACKSPACE", "INC_SP", 
        "xxx", "SAVE_PC", "CALL_FN", "BUILD_FN", "SET_BOXED", 
        "SET_LOCAL", "SET_CLOSURE", "GET_BOXED", "GET_LOCAL", 
        "GET_CLOSURE", "GET_BOXED_CLOSURE", "xxx", "BOX_IT", 
        "PRINT", "DROP", "PUSH_NIL", "xxx", "xxx", "xxx", "xxx", "DIV", 
        "xxx", "IS_INT", "IS_STR", "IS_LIST", "IS_DICT", "IS_ITER", 
        "EQUAL", "IS_EMPTY", "PUT", "GET", "RAND", "SIZE", "xxx", 
        "LESSEQUAL", "SUBSTR", "RESIZE", "PUSH", "POP", "KEYS", 
        "VALUES", "NEXT", "ASSERT", "JUMP", "JUMP_IF_TRUE", "DUP", 
        "NEW_LIST", "NEW_DICT", "NEW_STRINGBUFFER", "STR_APPEND", 
        "TO_STRING", "SWAP", "BLOCK", "SEP", "IN", "JUMP_IF_FALSE"
    };

    // size of the return frame
    private static final char RET_FRAME_SIZE = 3;

    /*
    // Syntax tree
    private static final int AST_BUILTIN_FUNCTION = 0x100;
    private static final int AST_DO = 0;
    private static final int AST_SET = 1;
    private static final int AST_IF = AST_SET + 1;
    private static final int AST_AND = AST_IF + 1;
    private static final int AST_OR = AST_AND + 1;
    private static final int AST_FOREACH = AST_OR + 1;
    private static final int AST_WHILE = AST_FOREACH + 1;
    private static final int AST_STRINGJOIN = AST_WHILE + 1;
    private static final int AST_LIST = AST_STRINGJOIN + 1;
    private static final int AST_DICT = AST_LIST + 1;
    // Opcode mask, to extract opcode from AST_FUNCTIION type
    private static final int MASK_OP = 0xFF;
    private static final String[] fnNames = {"not", "+", "-", "*", "/", "%", "is-integer", "is-string", "is-list", "is-dictionary", "is-iterator", "equals", "is-empty", "put", "get", "random", "size", "<", "<=", "substring", "resize", "push", "pop", "keys", "values", "get-next", "log", "assert"};
    private static final int[] fnArity = {1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 3, 2, 1, 1, 2, 2, 3, 2, 2, 1, 1, 1, 1, 1, 2};
    private static final char[] fnTypes = {ID_NOT, ID_ADD, ID_SUB, ID_MUL, ID_DIV, ID_REM, ID_IS_INT, ID_IS_STR, ID_IS_LIST, ID_IS_DICT, ID_IS_ITER, ID_EQUAL, ID_IS_EMPTY, ID_PUT, ID_GET, ID_RAND, ID_SIZE, ID_LESS, ID_LESSEQUAL, ID_SUBSTR, ID_RESIZE, ID_PUSH, ID_POP, ID_KEYS, ID_VALUES, ID_NEXT, ID_LOG, ID_ASSERT};
    private static final String[] builtinNames = {"set", "if", "and", "or", "foreach", "while", "do", "stringjoin", "list", "dict"};
    private static final int[] builtinTypes = {AST_SET, AST_IF, AST_AND, AST_OR, AST_FOREACH, AST_WHILE, AST_DO, AST_STRINGJOIN, AST_LIST, AST_DICT};
    */

    ////////////////////////
    // Utility functions //
    //////////////////////
    /////////////////////
    ////////////////////
    ///////////////////
    //////////////////
    private static String idName(int id) {
        return "" + id + ((id > 0 && id < idNames.length) ? idNames[id] : "");
    }
    private static String stringify(Object o) {
        if (o == null) {
            return "null";
        } else if (o instanceof Object[]) {
            StringBuffer sb = new StringBuffer();
            Object[] os = (Object[]) o;
            sb.append("[");
            if (os.length > 0 && os[0] instanceof Integer) {
                int id = ((Integer) os[0]).intValue();
                sb.append(idName(id));
            } else if(os.length > 0) {
                sb.append(os[0]);
            }
            for (int i = 1; i < os.length; i++) {
                sb.append(" " + stringify(os[i]));
            }
            sb.append("]");
            return sb.toString();
        } else {
            return o.toString();
        }
    }

    private static Object[] v(int id, Object o) {
        Object[] result = {new Integer(id), o};
        return result;
    }

    private static Object[] v(int id, Object o1, Object o2) {
        Object[] result = {new Integer(id), o1, o2};
        return result;
    }

    private static Object[] v(int id, Object o1, Object o2, Object o3) {
        Object[] result = {new Integer(id), o1, o2, o3};
        return result;
    }

    private static int stackAdd(Stack s, Object val) {
        int pos = s.indexOf(val);
        if (pos == -1) {
            pos = s.size();
            s.push(val);
        }
        return pos;
    }

    //////////////////////
    // Utility classes //
    ////////////////////
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
    /*
    private static class Literal {

        public Object value;

        public Literal(Object value) {
            this.value = value;
        }
    }
    */

    /**
     * Analysis of variables in a function being compiled,
     * updated during the parsing.
     */
    public static class Closure {

        public byte[] code;
        public int argc;
        public Object[] closure;
        public Object[] constPool;

        public Closure(int argc, StringBuffer code, Stack constPool, Stack closure) {
            this.argc = argc;

            this.code = new byte[code.length()];
            for (int i = 0; i < this.code.length; i++) {
                this.code[i] = (byte) code.charAt(i);
            }

            this.constPool = new Object[constPool.size()];
            for (int i = 0; i < this.constPool.length; i++) {
                this.constPool[i] = constPool.elementAt(i);
            }

            this.closure = new Object[closure.size()];
            for (int i = 0; i < this.closure.length; i++) {
                this.closure[i] = closure.elementAt(i);
            }
        }

        public Closure(Closure cl) {
            this.argc = cl.argc;
            this.code = cl.code;
            this.constPool = cl.constPool;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("closure" + argc + "{\n\tcode:");
            for (int i = 0; i < code.length; i++) {
                sb.append(" ");
                sb.append(idName(code[i]));
            }
            sb.append("\n\tclosure:");
            for (int i = 0; i < closure.length; i++) {
                sb.append(" " + i + ":");
                sb.append(stringify(closure[i]));
            }
            sb.append("\n\tconstPool:");
            for (int i = 0; i < constPool.length; i++) {
                sb.append(" " + i + ":");
                sb.append(stringify(constPool[i]));
            }
            sb.append("\n}");
            return sb.toString();
        }
    }

    //////////////////////
    /// Variables and ///
    /// constructor  ///
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
    private InputStream is;
    private int c;
    private StringBuffer sb;
    private Token token;
    private Stack varsUsed;
    private Stack varsBoxed;
    private Stack varsLocals;
    private Stack varsClosure;
    private int varsArgc;

    public LightScript(InputStream is) {
        this.is = is;
        sb = new StringBuffer();
        c = ' ';
        token = null;
        varsUsed = new Stack();
        varsBoxed = new Stack();
        varsLocals = new Stack();
        varsArgc = 0;
        next();
    }

    //////////////////////
    //// Tokeniser //////
    ////////////////////
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
    private Object getToken() {
        return token;
    }

    private void nextc() {
        try {
            c = is.read();
        } catch (Exception e) {
            c = -1;
        }
    }

    private void pushc() {
        sb.append((char) c);
        nextc();
    }

    private boolean isNum() {
        return '0' <= c && c <= '9';
    }

    private boolean isAlphaNum() {
        return isNum() || c == '_' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    private boolean isSymb() {
        return c == '=' || c == '!' || c == '<' || c == '&' || c == '|';
    }

    private boolean next() {
        sb.setLength(0);

        // skip whitespaces
        while (c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '/') {
            // comments
            if (c == '/') {
                nextc();
                if (c == '/') {
                    while (c != '\n' && c != -1) {
                        nextc();
                    }
                } else {
                    token = new Token(false, "/");
                    return true;
                }
            }
            nextc();
        }

        // End of file
        if (c == -1) {
            token = new Token(false, null);
            return false;

        // String
        } else if (c == '"') {
            nextc();
            while (c != -1 && c != '"') {
                if (c == '\\') {
                    nextc();
                    if (c == 'n') {
                        c = '\n';
                    }
                }
                pushc();
            }
            nextc();
            token = new Token(true, sb.toString());
            return true;

        // Number
        } else if (isNum()) {
            do {
                pushc();
            } while (isNum());
            token = new Token(true, Integer.valueOf(sb.toString()));
            return true;

        // Identifier
        } else if (isAlphaNum()) {
            do {
                pushc();
            } while (isAlphaNum());

        // Long symbol !== , ===, <= , &&, ...
        } else if (isSymb()) {
            do {
                pushc();
            } while (isSymb());

        // Single symbol
        } else {
            pushc();
        }
        token = new Token(false, sb.toString());
        return true;
    }

    //////////////////////
    ////// Parser ///////
    ////////////////////
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
    private Object[] parse(int rbp) {
        Token t = token;
        next();
        Object[] left = t.nud();
        while (rbp < token.bp && !t.sep) {
            t = token;
            next();
            left = t.led(left);
        }
        return left;
    }

    private class Token {

        private Object val;
        private int nudFn;
        private int ledFn;
        private int nudId;
        private int ledId;
        public boolean sep;
        public int bp;
        private static final int NUD_ID = 0;
        private static final int NUD_LITERAL = 1;
        private static final int NUD_END = 2;
        private static final int NUD_SEP = 3;
        private static final int NUD_LIST = 4;
        private static final int NUD_PREFIX = 5;
        private static final int NUD_PREFIX2 = 6;
        private static final int LED_INFIX = 7;
        private static final int LED_INFIXR = 8;
        private static final int LED_INFIX_LIST = 9;
        private static final int NUD_FUNCTION = 10;
        private static final int NUD_VAR = 11;
        private static final int LED_DOT = 12;

        private Object[] readList(Stack s) {
            Object[] p = parse(0);
            while (p != END_TOKEN) {
                s.push(p);
                p = parse(0);
            }

            Object[] result = new Object[s.size()];
            s.copyInto(result);
            return result;
        }

        public Object[] nud() {
            switch (nudFn) {
                case NUD_ID:
                    stackAdd(varsUsed, val);
                    return v(ID_IDENT, val);
                case NUD_LITERAL:
                    return v(ID_LITERAL, val);
                case NUD_END:
                    return END_TOKEN;
                case NUD_SEP:
                    return SEP_TOKEN;
                case NUD_LIST: {
                    Stack s = new Stack();
                    s.push(new Integer(nudId));
                    return readList(s);
                }
                case NUD_PREFIX:
                    return v(nudId, parse(0));
                case NUD_PREFIX2:
                    return v(nudId, parse(0), parse(0));
                case NUD_FUNCTION: {
                    // The functio nud is a bit more complex than 
                    // the others because variable-use-analysis is done
                    // during the parsing.

                    // save statistics for previous function
                    Stack prevUsed = varsUsed;
                    Stack prevBoxed = varsBoxed;
                    Stack prevLocals = varsLocals;
                    int prevArgc = varsArgc;

                    // create new statistics
                    varsUsed = new Stack();
                    varsBoxed = new Stack();
                    varsLocals = new Stack();

                    // parse arguments
                    Object[] args = parse(0);

                    // add function arguments to statistics
                    varsArgc = args.length - 1;
                    if (((Integer) args[0]).intValue() != ID_PAREN) {
                        throw new Error("parameter not variable name" + stringify(args));
                    }
                    for (int i = 1; i < args.length; i++) {
                        Object[] os = (Object[]) args[i];
                        if (((Integer) os[0]).intValue() != ID_IDENT) {
                            throw new Error("parameter not variable name" + stringify(args));
                        }
                        varsLocals.push(os[1]);
                    }

                    // parse the body of the function
                    // notice that this may update vars{Used|Boxed|Locals}
                    Object[] body = parse(0);

                    // non-local variables are boxed into the closure
                    for (int i = 0; i < varsUsed.size(); i++) {
                        Object o = varsUsed.elementAt(i);
                        if (!varsLocals.contains(o)) {
                            stackAdd(varsBoxed, o);
                        }
                    }

                    //  find the variables in the closure
                    // and add that they need to be boxed at parent.
                    varsClosure = new Stack();
                    for (int i = 0; i < varsBoxed.size(); i++) {
                        Object o = varsBoxed.elementAt(i);
                        if (!varsLocals.contains(o)) {
                            stackAdd(prevBoxed, o);
                            stackAdd(varsClosure, o);
                        }
                    }
                    Object[] result = v(nudId, compile(body));
                    varsClosure = null;

                    // restore variable statistics
                    // notice that varsClosure is not needed,
                    // as it is calculated before the compile,
                    // and not updated/used other places
                    varsUsed = prevUsed;
                    varsBoxed = prevBoxed;
                    varsLocals = prevLocals;
                    varsArgc = prevArgc;
                    return result;
                }
                case NUD_VAR:
                    Object[] expr = parse(0);
                    int type = ((Integer) expr[0]).intValue();
                    if (type == ID_IDENT) {
                        stackAdd(varsLocals, expr[1]);
                    } else {
                        Object[] expr2 = (Object[]) expr[1];
                        if (type == ID_SET && ((Integer) expr2[0]).intValue() == ID_IDENT) {
                            stackAdd(varsLocals, expr2[1]);
                        } else {
                            throw new Error("Error in var");
                        }
                    }
                    return v(nudId, expr);
                default:
                    throw new Error("Unknown nud: " + nudFn);
            }
        }

        public Object[] led(Object left) {
            switch (ledFn) {
                    
                case LED_INFIX:
                    return v(ledId, left, parse(bp));
                case LED_INFIXR:
                    return v(ledId, left, parse(bp - 1));
                case LED_INFIX_LIST: {
                    Stack s = new Stack();
                    s.push(new Integer(ledId));
                    s.push(left);
                    return readList(s);
                }
                case LED_DOT: {
                    Object[] right = parse(bp);
                    right[0] = new Integer(ID_LITERAL);
                    return v(ledId, left, right);
                }
                default:
                    throw new Error("Unknown led: " + ledFn);
            }
        }

        public Token(boolean isLiteral, Object val) {
            this.val = val;
            sep = false;
            bp = 0;
            nudFn = 0;
            ledFn = 0;
            nudId = 0;
            ledId = 0;

            if (isLiteral) {
                nudFn = NUD_LITERAL;

            } else if (val == null || "]".equals(val) || ")".equals(val) || "}".equals(val)) {
                nudFn = NUD_END;
                sep = true;

            } else if (".".equals(val)) {
                bp = 700;
                ledFn = LED_DOT;
                ledId = ID_SUBSCRIPT;

            } else if ("(".equals(val)) {
                bp = 600;
                ledFn = LED_INFIX_LIST;
                ledId = ID_CALL_FUNCTION;
                nudFn = NUD_LIST;
                nudId = ID_PAREN;

            } else if ("[".equals(val)) {
                bp = 600;
                ledFn = LED_INFIX_LIST;
                ledId = ID_SUBSCRIPT;
                nudFn = NUD_LIST;
                nudId = ID_LIST_LITERAL;

            } else if ("*".equals(val)) {
                bp = 500;
                ledFn = LED_INFIX;
                ledId = ID_MUL;

            } else if ("%".equals(val)) {
                bp = 500;
                ledFn = LED_INFIX;
                ledId = ID_REM;

            } else if ("+".equals(val)) {
                bp = 400;
                ledFn = LED_INFIX;
                ledId = ID_ADD;

            } else if ("-".equals(val)) {
                bp = 400;
                ledFn = LED_INFIX;
                ledId = ID_SUB;
                nudFn = NUD_PREFIX;
                nudId = ID_NEG;

            } else if ("===".equals(val)) {
                bp = 300;
                ledFn = LED_INFIX;
                ledId = ID_EQUALS;

            } else if ("!==".equals(val)) {
                bp = 300;
                ledFn = LED_INFIX;
                ledId = ID_NOT_EQUALS;

            } else if ("<=".equals(val)) {
                bp = 300;
                ledFn = LED_INFIX;
                ledId = ID_LESS_EQUALS;

            } else if ("<".equals(val)) {
                bp = 300;
                ledFn = LED_INFIX;
                ledId = ID_LESS;

            } else if ("&&".equals(val)) {
                bp = 200;
                ledFn = LED_INFIXR;
                ledId = ID_AND;

            } else if ("||".equals(val)) {
                bp = 200;
                ledFn = LED_INFIXR;
                ledId = ID_OR;

            } else if ("else".equals(val)) {
                bp = 200;
                ledFn = LED_INFIXR;
                ledId = ID_ELSE;

            } else if ("in".equals(val)) {
                bp = 200;
                ledFn = LED_INFIX;
                ledId = ID_IN;

            } else if ("=".equals(val)) {
                bp = 100;
                ledFn = LED_INFIX;
                ledId = ID_SET;

            } else if (":".equals(val) || ";".equals(val) || ",".equals(val)) {
                nudFn = NUD_SEP;
                sep = true;

            } else if ("{".equals(val)) {
                nudFn = NUD_LIST;
                nudId = ID_CURLY;

            } else if ("var".equals(val)) {
                nudFn = NUD_VAR;
                nudId = ID_VAR;

            } else if ("return".equals(val)) {
                nudFn = NUD_PREFIX;
                nudId = ID_RETURN;

            } else if ("!".equals(val)) {
                nudFn = NUD_PREFIX;
                nudId = ID_NOT;

            } else if ("print".equals(val)) {
                nudFn = NUD_PREFIX;
                nudId = ID_PRINT;

            } else if ("function".equals(val)) {
                nudFn = NUD_FUNCTION;
                nudId = ID_BUILD_FUNCTION;

            } else if ("if".equals(val)) {
                nudFn = NUD_PREFIX2;
                nudId = ID_IF;

            } else if ("while".equals(val)) {
                nudFn = NUD_PREFIX2;
                nudId = ID_WHILE;

            } else if ("undefined".equals(val) || "null".equals(val) || "false".equals(val)) {
                this.val = null;
                nudFn = NUD_LITERAL;

            } else if ("true".equals(val)) {
                val = TRUE;
                nudFn = NUD_LITERAL;
            }

        }

        public String toString() {
            return val.toString();
        }
    }
    //////////////////////
    ///// Compiler //////
    ////////////////////
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
    private Stack constPool;
    private int maxDepth;
    private int depth;
    private StringBuffer code;

    private void pushShort(int i) {
        code.append((char) ((i >> 8) & 0xff));
        code.append((char) (i & 0xff));
    }

    private void setShort(int pos, int i) {
        code.setCharAt(pos - 2, (char) ((i >> 8) & 0xff));
        code.setCharAt(pos - 1, (char) (i & 0xff));
    }

    private void addDepth(int i) {
        depth += i;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    private void assertLength(Object[] list, int len) {
        if (list.length != len) {
            throw new Error("Wrong number of parameters:" + stringify(list));
        }
    }

    private void emit(int opcode) {
        code.append((char)opcode);
    }

    private int constPoolId(Object o) {
        int pos = constPool.indexOf(o);
        if (pos < 0) {
            pos = constPool.size();
            constPool.push(o);
        }
        return pos;
    }

    private void curlyToBlock(Object oexpr) {
        Object[] expr = (Object[]) oexpr;
        if(((Integer)expr[0]).intValue() == ID_CURLY) {
            expr[0] = new Integer(ID_BLOCK);
        }
    }

    private Closure compile(Object[] body) {
        constPool = new Stack();
        code = new StringBuffer();

        // make sure that there are sufficient stack space for the function
        code.append(ID_ENSURE_STACKSPACE);
        pushShort(0);
        int spacePos = code.length();

        // allocate space for local vars
        maxDepth = depth = varsLocals.size();
        int framesize = depth - varsArgc;
        while (framesize >= 127) {
            code.append(ID_INC_SP);
            code.append((char) 127);
            framesize -= 127;
        }
        if (framesize > 0) {
            code.append(ID_INC_SP);
            code.append((char) framesize);
        }

        // box boxed values in frame
        for(int i = 0; i < varsBoxed.size(); i++) {
            int pos = varsLocals.indexOf(varsBoxed.elementAt(i));
            if(pos != -1) {
                code.append(ID_BOX_IT);
                pushShort(depth - pos - 1);
            }
        }

        // compile
        curlyToBlock(body);
        compile(body, true);

        // emit return code, including current stack depth to drop
        code.append(ID_RETURN);
        pushShort(depth);

        // patch amount of stack space needed
        maxDepth -= varsArgc;
        setShort(spacePos, maxDepth);

        Closure result = new Closure(varsArgc, code, constPool, varsClosure);
        code = null;
        constPool = null;
        //System.out.println(stringify(body));
        //System.out.println(varsLocals);
        //System.out.println(varsBoxed);
        //System.out.println(varsClosure);
        return result;
    }

    private int childType(Object[] expr, int i) {
                return ((Integer)((Object[])expr[i])[0]).intValue();
    }

    private void compile(Object rawexpr, boolean yieldResult) {
        boolean hasResult;
        Object[] expr = (Object[]) rawexpr;
        int id = ((Integer)expr[0]).intValue();
        switch(id) { 
            case ID_ADD: case ID_MUL: case ID_REM: case ID_SUB: 
            case ID_EQUALS: case ID_NOT_EQUALS: 
            case ID_SUBSCRIPT:
            case ID_LESS_EQUALS: case ID_LESS: {
                compile(expr[1], true);
                compile(expr[2], true);
                emit(id);
                addDepth(-1);
                hasResult = true;
                break;
            } 
            case ID_PRINT: case ID_NOT: case ID_NEG: {
                compile(expr[1], true);
                emit(id);
                hasResult = true;
                break;
            } case ID_LITERAL: {
                emit(id);
                pushShort(constPoolId(expr[1]));
                hasResult = true;
                addDepth(1);
                break;
            } case ID_BLOCK: {
                for(int i = 1; i < expr.length; i++) {
                    compile(expr[i], false);
                }
                hasResult = false;
                break;
            } case ID_RETURN: {
                compile(expr[1], true);
                emit(ID_RETURN);
                pushShort(depth);
                addDepth(-1);
                hasResult = false;
                break;
            } case ID_IDENT: {
                String name = (String) expr[1];
                int pos = varsClosure.indexOf(name);
                if (pos >= 0) {
                    emit(ID_GET_CLOSURE);
                    pushShort(pos);
                } else {
                    pos = varsLocals.indexOf(name);
                    if (pos == -1) {
                        throw new Error("Unfound var: " + stringify(expr));
                    }
                    if (varsBoxed.contains(name)) {
                        emit(ID_GET_BOXED);
                    } else {
                        emit(ID_GET_LOCAL);
                    }
                    pushShort(depth - pos - 1);
                }
                addDepth(1);
                hasResult = true;
                break;
            } case ID_VAR: {
                int id2 = childType(expr, 1);
                if(id2 == ID_IDENT) {
                    hasResult = false;
                } else if(id2 == ID_SET) {
                    compile(expr[1], yieldResult);
                    hasResult = yieldResult;
                } else {
                    throw new Error("Error in var statement: " + stringify(expr));
                }
                break;
            } case ID_SET: {
                            assertLength(expr, 3);
                            int targetType = childType(expr, 1);
                            if(targetType == ID_IDENT) {
                            String name = (String) ((Object[]) expr[1])[1];
                            compile(expr[2], true);
                            int pos = varsClosure.indexOf(name);
                            if (pos >= 0) {
                                emit(ID_SET_CLOSURE);
                                pushShort(pos);
                            } else {
                                pos = varsLocals.indexOf(name);
                                if (varsBoxed.contains(name)) {
                                    emit(ID_SET_BOXED);
                                } else {
                                    emit(ID_SET_LOCAL);
                                }
                                pushShort(depth - pos - 1);
                            }
                            hasResult = true;
                            } else {
                                throw new Error("Uncompilable assignment operator: " + stringify(expr));
                            }
                            break;
            } case ID_PAREN: {
                if(expr.length != 2) {
                    throw new Error("Unexpected content of parenthesis: " + stringify(expr));
                }
                compile(expr[1], yieldResult);
                hasResult = yieldResult;
                break;
            } case ID_CALL_FUNCTION: {
                // save program counter
                emit(ID_SAVE_PC);
                addDepth(RET_FRAME_SIZE);

                // find function and evaluate parameters
                for (int i = 1; i < expr.length; i++) {
                    compile(expr[i], true);
                }

                // call the function
                emit(ID_CALL_FN);
                if (expr.length > 129) {
                    throw new Error("too many parameters");
                }
                emit(expr.length - 2);
                addDepth(2 - expr.length - RET_FRAME_SIZE);
                hasResult = true;
                break;
            } case ID_BUILD_FUNCTION: {
                Object[] vars = ((Closure) expr[1]).closure;
                for (int i = 0; i < vars.length; i++) {
                String name = (String) vars[i];
                if (varsBoxed.contains(name)) {
                    code.append(ID_GET_LOCAL);
                    pushShort(depth - varsLocals.indexOf(name) - 1);
                } else {
                    code.append(ID_GET_BOXED_CLOSURE);
                    pushShort(varsClosure.indexOf(name));
                }
                addDepth(1);
            }
            emit(ID_LITERAL);
            pushShort(constPoolId(expr[1]));
            addDepth(1);
            emit(ID_BUILD_FN);
            pushShort(vars.length);
            addDepth(-vars.length);
            hasResult = true;
            break;

            } case ID_IF: {
                int subtype = childType(expr, 2);

                if(subtype == ID_ELSE) {
                            Object[] branch = (Object[])expr[2];

                            //    code for condition
                            //    jump_if_true -> label1
                            //    code for branch2
                            //    jump -> label2
                            // label1:
                            //    code for branch1
                            // label2:

                            int pos0, pos1, len;
                            // compile condition
                            compile(expr[1],true);

                            code.append(ID_JUMP_IF_TRUE);
                            pushShort(0);
                            pos0 = code.length();
                            addDepth(-1);

                            curlyToBlock(branch[2]);
                            compile(branch[2], yieldResult);

                            code.append(ID_JUMP);
                            pushShort(0);
                            pos1 = code.length();
                            len = pos1 - pos0;
                            setShort(pos0, len);

                            addDepth(yieldResult ? -1 : 0);

                            curlyToBlock(branch[1]);
                            compile(branch[1], yieldResult);

                            len = code.length() - pos1;
                            setShort(pos1, len);
                            hasResult = yieldResult;
                            break;
                } else {
                    throw new Error("unsupported if: " + stringify(expr));
                }
            } case ID_SEP: {
                hasResult = false;
                break;
            } case ID_AND: {
                            assertLength(expr, 3);
                            int pos0, pos1, len;

                            compile(expr[1], true);

                            emit(ID_JUMP_IF_TRUE);
                            pushShort(0);
                            pos0 = code.length();
                            addDepth(-1);

                            emit(ID_PUSH_NIL);
                            addDepth(1);

                            emit(ID_JUMP);
                            pushShort(0);
                            pos1 = code.length();
                            len = pos1 - pos0;
                            setShort(pos0, len);
                            addDepth(-1);

                            compile(expr[2], true);
                            len = code.length() - pos1;
                            setShort(pos1, len);
                            hasResult = true;
                            break;
            } case ID_OR: {
                            assertLength(expr, 3);
                            int pos0, pos1, len;

                            compile(expr[1], true);

                            emit(ID_DUP);
                            addDepth(1);

                            emit(ID_JUMP_IF_TRUE);
                            pushShort(0);
                            pos0 = code.length();
                            addDepth(-1);

                            emit(ID_DROP);
                            addDepth(-1);

                            compile(expr[2], true);

                            pos1 = code.length();
                            len = pos1 - pos0;
                            setShort(pos0, len);

                            hasResult = true;

                            break;
            } case ID_LIST_LITERAL: {
                            code.append(ID_NEW_LIST);
                            addDepth(1);

                            for (int i = 1; i < expr.length; i++) {
                                if(childType(expr, i) != ID_SEP) {
                                    compile(expr[i], true);
                                    code.append(ID_PUSH);
                                    addDepth(-1);
                                }
                            }
                            hasResult = true;

                            break;
            } case ID_CURLY: {
                            code.append(ID_NEW_DICT);
                            addDepth(1);

                            int i = 0;
                            while(i < expr.length - 3) {
                                do {
                                    ++i;
                                } while(childType(expr, i) == ID_SEP);
                                compile(expr[i], true);
                                do {
                                    ++i;
                                } while(childType(expr, i) == ID_SEP) ;
                                compile(expr[i], true);
                                code.append(ID_PUT);
                                addDepth(-2);
                            }
                            hasResult = true;

                            break;
            } case ID_WHILE: {
                            // top:
                            //   code for condition
                            //   jump if false -> labelExit
                            //   code for stmt1
                            //   drop
                            //   ...
                            //   code for stmtn
                            //   drop
                            //   jump -> labelTop;
                            // labelExit:

                            int pos0, pos1, len;
                            pos0 = code.length();

                            compile(expr[1], true);
                            emit(ID_JUMP_IF_FALSE);
                            pushShort(0);
                            pos1 = code.length();
                            addDepth(-1);


                            curlyToBlock(expr[2]);
                            compile(expr[2], false);

                            emit(ID_JUMP);
                            pushShort(pos0 - code.length() - 2);
                            
                            setShort(pos1, code.length() - pos1);
                            hasResult = false;
                            break;
            }
            default:
                throw new Error("Uncompilable expression: " + stringify(expr));
        }

        if(hasResult && !yieldResult) {
            emit(ID_DROP);
            addDepth(-1);
        } else if(yieldResult && !hasResult) {
            emit(ID_PUSH_NIL);
            addDepth(1);
        }
    }

    //////////////////////
    // Virtual Machine //
    ////////////////////
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
    private static int readShort(int pc, byte[] code) {
        return (short) (((code[++pc] & 0xff) << 8) | (code[++pc] & 0xff));
    }

    public static Object execute(Closure cl) {
        int sp = -1;
        Object[] stack = new Object[0];
        int pc = -1;
        byte[] code = cl.code;
        Object[] constPool = cl.constPool;
        Object[] closure = cl.closure;
        for (;;) {
            ++pc;
            //System.out.println("pc:" + pc + " op:"  + idName(code[pc]) + " sp:" + sp + " stack.length:" + stack.length + " int:" + readShort(pc, code));
            switch (code[pc]) {
                case ID_ENSURE_STACKSPACE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    if (stack.length <= arg + sp + 1) {
                        // keep the allocate stack tight to max, 
                        // to catch errors;
                        // FIXME: grow exponential when tested
                        Object[] newstack = new Object[(arg + sp + 1)];
                        System.arraycopy(stack, 0, newstack, 0, sp + 1);
                        stack = newstack;
                    }
                    break;
                }
                case ID_INC_SP: {
                    sp += code[++pc];
                    break;
                }
                case ID_RETURN: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object result = stack[sp];
                    sp -= arg - 1;
                    if (sp == 0) {
                        return result;
                    }
                    pc = ((Integer) stack[--sp]).intValue();
                    code = (byte[]) stack[--sp];
                    constPool = (Object[]) stack[--sp];
                    closure = (Object[]) stack[--sp];
                    stack[sp] = result;
                    break;
                }
                case ID_SAVE_PC: {
                    stack[++sp] = closure;
                    stack[++sp] = constPool;
                    stack[++sp] = code;
                    break;
                }
                case ID_CALL_FN: {
                    int argc = code[++pc];
                    Closure fn = (Closure) stack[sp - argc];
                    stack[sp - argc] = new Integer(pc);
                    pc = -1;
                    code = fn.code;
                    constPool = fn.constPool;
                    closure = fn.closure;
                    break;
                }
                case ID_BUILD_FN: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Closure fn = new Closure((Closure) stack[sp]);
                    Object[] clos = new Object[arg];
                    for (int i = arg - 1; i >= 0; i--) {
                        --sp;
                        clos[i] = stack[sp];
                    }
                    fn.closure = clos;
                    stack[sp] = fn;
                    break;
                }
                case ID_SET_BOXED: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    ((Object[]) stack[sp - arg])[0] = stack[sp];
                    break;
                }
                case ID_SET_LOCAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[sp - arg] = stack[sp];
                    break;
                }
                case ID_SET_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    ((Object[]) closure[arg])[0] = stack[sp];
                    break;
                }
                case ID_GET_BOXED: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object o = ((Object[]) stack[sp - arg])[0];
                    stack[++sp] = o;
                    break;
                }
                case ID_GET_LOCAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object o = stack[sp - arg];
                    stack[++sp] = o;
                    break;
                }
                case ID_GET_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = ((Object[]) closure[arg])[0];
                    break;
                }
                case ID_GET_BOXED_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = closure[arg];
                    break;
                }
                case ID_LITERAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = constPool[arg];
                    break;
                }
                case ID_BOX_IT: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object[] box = {stack[sp - arg]};
                    stack[sp - arg] = box;
                    break;
                }
                case ID_PRINT: {
                    System.out.println(stringify(stack[sp]));
                    break;
                }
                case ID_DROP: {
                    --sp;
                    break;
                }
                case ID_PUSH_NIL: {
                    stack[++sp] = null;
                    break;
                }
                case ID_NOT: {
                    stack[sp] = stack[sp] == null ? TRUE : null;
                    break;
                }
                case ID_NEG: {
                    stack[sp] = new Integer(-((Integer)stack[sp]).intValue());
                    break;
                }
                case ID_ADD: {
                    int result = ((Integer) stack[sp]).intValue();
                    result += ((Integer) stack[--sp]).intValue();
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_SUB: {
                    int result = ((Integer) stack[sp]).intValue();
                    result = ((Integer) stack[--sp]).intValue() - result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_MUL: {
                    int result = ((Integer) stack[sp]).intValue();
                    result *= ((Integer) stack[--sp]).intValue();
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_REM: {
                    int result = ((Integer) stack[sp]).intValue();
                    result = ((Integer) stack[--sp]).intValue() % result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_NOT_EQUALS: {
                    Object o = stack[sp];
                    --sp;
                    stack[sp] = (o == null)
                            ? (stack[sp] == null ? TRUE : null)
                            : (o.equals(stack[sp]) ? TRUE : null);
                    break;
                }
                case ID_EQUALS: {
                    Object o = stack[sp];
                    --sp;
                    stack[sp] = (o == null)
                            ? (stack[sp] == null ? TRUE : null)
                            : (o.equals(stack[sp]) ? TRUE : null);
                    break;
                }
                case ID_PUT: {
                    Object val = stack[sp];
                    Object key = stack[--sp];
                    Object container = stack[--sp];
                    if (container instanceof Stack) {
                        ((Stack) container).setElementAt(val, ((Integer) key).intValue());
                    } else if (container instanceof Hashtable) {
                        if (val == null) {
                            ((Hashtable) container).remove(key);
                        } else {
                            ((Hashtable) container).put(key, val);
                        }
                    }
                    break;
                }
                case ID_SUBSCRIPT: {
                    Object key = stack[sp];
                    Object container = stack[--sp];
                    if (container instanceof Stack) {
                        stack[sp] = ((Stack) container).elementAt(((Integer) key).intValue());
                    } else if (container instanceof Hashtable) {
                        stack[sp] = ((Hashtable) container).get(key);
                    } else {
                        stack[sp] = null;
                    }
                    break;
                }
                case ID_PUSH: {
                    Object o = stack[sp];
                    ((Stack) stack[--sp]).push(o);
                    break;
                }
                case ID_POP: {
                    stack[sp] = ((Stack) stack[sp]).pop();
                    break;
                }
                case ID_LESS: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = ((Integer) o1).intValue() < ((Integer) o2).intValue() ? TRUE : null;
                    } else {
                        stack[sp] = o1.toString().compareTo(o2.toString()) < 0 ? TRUE : null;
                    }
                    break;
                }
                case ID_LESS_EQUALS: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = ((Integer) o1).intValue() <= ((Integer) o2).intValue() ? TRUE : null;
                    } else {
                        stack[sp] = o1.toString().compareTo(o2.toString()) <= 0 ? TRUE : null;
                    }
                    break;
                }
                case ID_JUMP: {
                    pc += readShort(pc, code) + 2;
                    break;
                }
                case ID_JUMP_IF_FALSE: {
                    if (stack[sp] == null) {
                        pc += readShort(pc, code) + 2;
                    } else {
                        pc += 2;
                    }
                    --sp;
                    break;
                }
                case ID_JUMP_IF_TRUE: {
                    if (stack[sp] != null) {
                        pc += readShort(pc, code) + 2;
                    } else {
                        pc += 2;
                    }
                    --sp;
                    break;
                }
                case ID_DUP: {
                    Object o = stack[sp];
                    stack[++sp] = o;
                    break;
                }
                case ID_NEW_LIST: {
                    stack[++sp] = new Stack();
                    break;
                }
                case ID_NEW_DICT: {
                    stack[++sp] = new Hashtable();
                    break;
                }
                default: {
                             throw new Error("Unknown opcode: " + idName(code[pc]));
                }
            }
        }
    }
}
