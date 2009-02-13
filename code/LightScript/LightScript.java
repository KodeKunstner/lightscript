import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Hashtable;
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
public class LightScript {

    private Hashtable boxedGlobals;

    public void eval(String s) throws LightScriptException {
        eval(new ByteArrayInputStream(s.getBytes()));
    }
    public void eval( InputStream is) throws LightScriptException {
        /*
        try {
        */
            this.is = is;
            sb = new StringBuffer();
            c = ' ';
            varsArgc = 0;
            nextToken();
            while(tokenVal != null || tokenNudFn != NUD_END) {
                // parse with every var in closure
                varsUsed = varsLocals = varsBoxed = new Stack();
                Object[] os = parse(0);
                varsClosure = varsUsed;
        
                // compile
                Code c = compile(os);
        
                // create closure from globals
                for(int i = 0; i < c.closure.length; i ++) {
                    Object box = boxedGlobals.get(c.closure[i]);
                    if(box == null) {
                        box = new Object[1];
                        boxedGlobals.put(c.closure[i], box);
                    }
                    c.closure[i] = box;
                }
                execute(c);
            }
            /*
        } catch(Error e) {
            throw new LightScriptException(e);
        }
        */
    }

    public void set(Object key, Object value) {
        Object[] box = (Object[])boxedGlobals.get(key);
        if(box == null) {
            box = new Object[1];
            boxedGlobals.put(key, box);
        }
        box[0] = value;
    }
    public Object get(Object key) {
        Object[] box = (Object[])boxedGlobals.get(key);
        if(box == null) {
            return null;
        } else {
            return box[0];
        }
    
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
    private static final char ID_SAVE_PC = 29;
    private static final char ID_CALL_FN = 30;
    private static final char ID_BUILD_FN = 31;
    private static final char ID_SET_BOXED = 32;
    private static final char ID_SET_LOCAL = 33;
    private static final char ID_SET_CLOSURE = 34;
    private static final char ID_GET_BOXED = 35;
    private static final char ID_GET_LOCAL = 36;
    private static final char ID_GET_CLOSURE = 37;
    // get a value from the closure without unboxing it
    private static final char ID_GET_BOXED_CLOSURE = 38;
    // box a value
    private static final char ID_BOX_IT = 39;
    private static final char ID_XXX = 40;
    private static final char ID_DROP = 41;
    private static final char ID_PUSH_NIL = 42;
    private static final char ID_PUT = 43;
    private static final char ID_PUSH = 44;
    private static final char ID_POP = 45;
    private static final char ID_JUMP = 46;
    private static final char ID_JUMP_IF_TRUE = 47;
    private static final char ID_DUP = 48;
    private static final char ID_NEW_LIST = 49;
    private static final char ID_NEW_DICT = 50;
    private static final char ID_BLOCK = 51;
    private static final char ID_SEP = 52;
    private static final char ID_IN = 53;
    private static final char ID_JUMP_IF_FALSE = 54;
    private static final char ID_SET_THIS = 55;
    private static final char ID_THIS = 56;
    private static final char ID_SWAP = 57;
    private static final char ID_FOR = 58;
    private static final char ID_END = 59;
    private static final char ID_THROW = 60;
    private static final char ID_TRY = 61;
    private static final char ID_CATCH = 62;
    private static final char ID_UNTRY = 63;
    private static final char ID_DO = 64;
    private static final Object[] END_TOKEN = {new Integer(ID_END)};
    private static final Object[] SEP_TOKEN = {new Integer(ID_SEP)};
    private static final Boolean TRUE = new Boolean(true);

    // size of the return frame
    private static final char RET_FRAME_SIZE = 3;
    private static final char TRY_FRAME_SIZE = 5;



    ////////////////////////
    ////// Debugging //////
    //////////////////////
    /////////////////////
    ////////////////////
    ///////////////////
    //////////////////

    private static final String[] idNames = {"", "PAREN", "LIST_LITERAL",
        "CURLY", "VAR", "RETURN", "NOT", "FUNCTION", "IF", "WHILE",
        "LITERAL", "CALL_FUNCTION", "SUBSCRIPT", "MUL", "REM", "ADD",
        "SUB", "NEG", "EQUALS", "NOT_EQUALS", "LEQ", "LESS", "AND", "OR",
        "ELSE", "SET", "IDENT", "ENSURE_STACKSPACE", "INC_SP",
        "SAVE_PC", "CALL_FN", "BUILD_FN", "SET_BOXED",
        "SET_LOCAL", "SET_CLOSURE", "GET_BOXED", "GET_LOCAL",
        "GET_CLOSURE", "GET_BOXED_CLOSURE", "BOX_IT",
        "XXX", "DROP", "PUSH_NIL","PUT", "PUSH", "POP",  "JUMP", "JUMP_IF_TRUE", "DUP",
        "NEW_LIST", "NEW_DICT", "BLOCK", "SEP", "IN", "JUMP_IF_FALSE",
        "SET_THIS", "THIS", "SWAP", "FOR", "END", "THROW", "TRY", "CATCH", "UNTRY",
        "DO"
        
    };

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
            } else if (os.length > 0) {
                sb.append(os[0]);
            }
            for (int i = 1; i < os.length; i++) {
                sb.append(" " + stringify(os[i]));
            }
            sb.append("]");
            return sb.toString();
        } else if (o instanceof Code) {
            Code c = (Code) o;
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
    /*

    private static String stringify(Object o) {
        return o.toString();
    }
    */

    ////////////////////////
    // Utility functions //
    //////////////////////
    /////////////////////
    ////////////////////
    ///////////////////
    //////////////////

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

    private static void stackAdd(Stack s, Object val) {
        if(s == null) {
            return;
        }
        int pos = s.indexOf(val);
        if (pos == -1) {
            pos = s.size();
            s.push(val);
        }
    }

    //////////////////////
    // Utility classes //
    ////////////////////
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
    private Object[] stackToVector(Stack s) {
        Object[] result = new Object[s.size()];
        s.copyInto(result);
        return result;
    }

    /**
     * Analysis of variables in a function being compiled,
     * updated during the parsing.
     */
    private static class Code implements LightScriptFunction {
        public Object apply(Object thisPtr, Object[] args, int argpos, int argcount) throws LightScriptException {
            if(argcount == argc) {
                return execute(this, args, argpos+argcount-1, thisPtr, argcount);
            } else {
                throw new LightScriptException("Wrong number of arguments");
            }
        }
        public int argc;
        public byte[] code;
        public Object[] constPool;
        public Object[] closure;

        public Code(int argc, byte[] code, Object[] constPool, Object[] closure) {
            this.argc = argc;
            this.code = code;
            this.constPool = constPool;
            this.closure = closure;
        }

        public Code(Code cl) {
            this.argc = cl.argc;
            this.code = cl.code;
            this.constPool = cl.constPool;
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
    private Stack varsUsed;
    private Stack varsBoxed;
    private Stack varsLocals;
    private Stack varsClosure;
    private int varsArgc;

    public LightScript() {
        boxedGlobals = new Hashtable();
        LightScriptStdLib.register(this);
    }

    //////////////////////
    //// Tokeniser //////
    ////////////////////
    ///////////////////
    //////////////////
    /////////////////
    ////////////////
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

    private boolean nextToken() {
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
                    newToken(false, "/");
                    return true;
                }
            }
            nextc();
        }

        // End of file
        if (c == -1) {
            newToken(false, null);
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
            newToken(true, sb.toString());
            return true;

        // Number
        } else if (isNum()) {
            do {
                pushc();
            } while (isNum());
            newToken(true, Integer.valueOf(sb.toString()));
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
        newToken(false, sb.toString());
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
        boolean tSep = tokenSep;
        int tNudFn = tokenNudFn;
        int tNudId = tokenNudId;
        Object tVal = tokenVal;
        nextToken();
        Object[] left = nud(tNudFn, tNudId, tVal);
        while (rbp < tokenBp && !tSep) {
            tSep = tokenSep;
            int tLedFn = tokenLedFn;
            int tLedId = tokenLedId;
            int tBp = tokenBp;
            nextToken();
            left = led(tLedFn, tLedId, left, tBp);
        }
        return left;
    }
    private Object tokenVal;
    private int tokenNudFn;
    private int tokenLedFn;
    private int tokenNudId;
    private int tokenLedId;
    private boolean tokenSep;
    private int tokenBp;
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
    private static final int NUD_ATOM = 13;
    private static final int LED_INFIX_IF = 14;
    private static final int NUD_CATCH = 14;

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

    private Object[] stripSep(Object[] os) {
        Stack s = new Stack();
        for(int i = 0; i < os.length; i++) {
            if(os[i] != SEP_TOKEN) {
                s.push(os[i]);
            }
        }
        os = new Object[s.size()];
        s.copyInto(os);
        return os;
    }

    private Object[] nud(int nudFn, int nudId, Object val) {
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
            case NUD_ATOM: {
                Object[] result = { new Integer(nudId) };
                return result;
            }
            case NUD_PREFIX:
                return v(nudId, parse(0));
            case NUD_PREFIX2:
                return v(nudId, parse(0), parse(0));
            case NUD_CATCH: {
                Object[] o = parse(0);
                stackAdd(varsLocals, ((Object[])o[1])[1]);
                return v(nudId, o, parse(0));
            }
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
                Object[] args = stripSep(parse(0));

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

    private Object[] led(int ledFn, int ledId, Object left, int bp) {
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
                Stack t = varsUsed;
                varsUsed= null;
                Object[] right = parse(bp);
                varsUsed = t;
                right[0] = new Integer(ID_LITERAL);
                return v(ledId, left, right);
            }
            case LED_INFIX_IF: {
                Object branch1 = parse(0);
                if(parse(0) != SEP_TOKEN) {
                    throw new Error("infix if error");
                }
                Object branch2 = parse(0);
                return v(ID_IF, left, v(ID_ELSE, branch1, branch2));
            }
            default:
                throw new Error("Unknown led: " + ledFn);
        }
    }

    private void newToken(boolean isLiteral, Object val) {
        this.tokenVal = val;
        tokenSep = false;
        tokenBp = 0;
        tokenNudFn = 0;
        tokenLedFn = 0;
        tokenNudId = 0;
        tokenLedId = 0;

        if (isLiteral) {
            tokenNudFn = NUD_LITERAL;

        } else if (val == null || "]".equals(val) || ")".equals(val) || "}".equals(val)) {
            tokenNudFn = NUD_END;
            tokenSep = true;

        } else if (".".equals(val)) {
            tokenBp = 700;
            tokenLedFn = LED_DOT;
            tokenLedId = ID_SUBSCRIPT;

        } else if ("(".equals(val)) {
            tokenBp = 600;
            tokenLedFn = LED_INFIX_LIST;
            tokenLedId = ID_CALL_FUNCTION;
            tokenNudFn = NUD_LIST;
            tokenNudId = ID_PAREN;

        } else if ("[".equals(val)) {
            tokenBp = 600;
            tokenLedFn = LED_INFIX_LIST;
            tokenLedId = ID_SUBSCRIPT;
            tokenNudFn = NUD_LIST;
            tokenNudId = ID_LIST_LITERAL;

        } else if ("*".equals(val)) {
            tokenBp = 500;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_MUL;

        } else if ("%".equals(val)) {
            tokenBp = 500;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_REM;

        } else if ("+".equals(val)) {
            tokenBp = 400;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_ADD;

        } else if ("-".equals(val)) {
            tokenBp = 400;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_SUB;
            tokenNudFn = NUD_PREFIX;
            tokenNudId = ID_NEG;

        } else if ("===".equals(val)) {
            tokenBp = 300;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_EQUALS;

        } else if ("!==".equals(val)) {
            tokenBp = 300;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_NOT_EQUALS;

        } else if ("<=".equals(val)) {
            tokenBp = 300;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_LESS_EQUALS;

        } else if ("<".equals(val)) {
            tokenBp = 300;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_LESS;

        } else if ("&&".equals(val)) {
            tokenBp = 200;
            tokenLedFn = LED_INFIXR;
            tokenLedId = ID_AND;

        } else if ("||".equals(val)) {
            tokenBp = 200;
            tokenLedFn = LED_INFIXR;
            tokenLedId = ID_OR;

        } else if ("else".equals(val)) {
            tokenBp = 200;
            tokenLedFn = LED_INFIXR;
            tokenLedId = ID_ELSE;

        } else if ("in".equals(val)) {
            tokenBp = 200;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_IN;

        } else if ("?".equals(val)) {
            tokenBp = 200;
            tokenLedFn = LED_INFIX_IF;

        } else if ("=".equals(val)) {
            tokenBp = 100;
            tokenLedFn = LED_INFIX;
            tokenLedId = ID_SET;

        } else if (":".equals(val) || ";".equals(val) || ",".equals(val)) {
            tokenNudFn = NUD_SEP;
            tokenSep = true;

        } else if ("{".equals(val)) {
            tokenNudFn = NUD_LIST;
            tokenNudId = ID_CURLY;

        } else if ("var".equals(val)) {
            tokenNudFn = NUD_VAR;
            tokenNudId = ID_VAR;

        } else if ("return".equals(val)) {
            tokenNudFn = NUD_PREFIX;
            tokenNudId = ID_RETURN;

        } else if ("!".equals(val)) {
            tokenNudFn = NUD_PREFIX;
            tokenNudId = ID_NOT;

        } else if ("throw".equals(val)) {
            tokenNudFn = NUD_PREFIX;
            tokenNudId = ID_THROW;

        } else if ("try".equals(val)) {
            tokenNudFn = NUD_PREFIX2;
            tokenNudId = ID_TRY;

        } else if ("catch".equals(val)) {
            tokenNudFn = NUD_CATCH;
            tokenNudId = ID_CATCH;

        } else if ("function".equals(val)) {
            tokenNudFn = NUD_FUNCTION;
            tokenNudId = ID_BUILD_FUNCTION;

        } else if ("do".equals(val)) {
            tokenNudFn = NUD_PREFIX2;
            tokenNudId = ID_DO;

        } else if ("for".equals(val)) {
            tokenNudFn = NUD_PREFIX2;
            tokenNudId = ID_FOR;

        } else if ("if".equals(val)) {
            tokenNudFn = NUD_PREFIX2;
            tokenNudId = ID_IF;

        } else if ("while".equals(val)) {
            tokenNudFn = NUD_PREFIX2;
            tokenNudId = ID_WHILE;

        } else if ("undefined".equals(val) || "null".equals(val) || "false".equals(val)) {
            tokenNudFn = NUD_ATOM;
            tokenNudId = ID_PUSH_NIL;

        } else if ("this".equals(val)) {
            tokenNudFn = NUD_ATOM;
            tokenNudId = ID_THIS;

        } else if ("true".equals(val)) {
            val = TRUE;
            tokenNudFn = NUD_LITERAL;
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
        code.append((char) opcode);
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
        if (((Integer) expr[0]).intValue() == ID_CURLY) {
            expr[0] = new Integer(ID_BLOCK);
        }
    }

    private Code compile(Object[] body) {
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
        for (int i = 0; i < varsBoxed.size(); i++) {
            int pos = varsLocals.indexOf(varsBoxed.elementAt(i));
            if (pos != -1) {
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

        // create a new code object;
        Code result = new Code(varsArgc, new byte[code.length()], 
                new Object[constPool.size()], new Object[varsClosure.size()]);

        // copy values into the code object
        constPool.copyInto(result.constPool);
        varsClosure.copyInto(result.closure);
        for (int i = 0; i < result.code.length; i++) {
            result.code[i] = (byte) code.charAt(i);
        }

        //System.out.println(stringify(body));
        //System.out.println(varsLocals);
        //System.out.println(varsBoxed);
        //System.out.println(varsClosure);
        //System.out.println(result);
        return result;
    }

    private int childType(Object[] expr, int i) {
        return ((Integer) ((Object[]) expr[i])[0]).intValue();
    }

    private void compile(Object rawexpr, boolean yieldResult) {
        boolean hasResult;
        Object[] expr = (Object[]) rawexpr;
        int id = ((Integer) expr[0]).intValue();
        switch (id) {
            case ID_ADD:
            case ID_MUL:
            case ID_REM:
            case ID_SUB:
            case ID_EQUALS:
            case ID_NOT_EQUALS:
            case ID_SUBSCRIPT:
            case ID_LESS_EQUALS:
            case ID_LESS: {
                compile(expr[1], true);
                compile(expr[2], true);
                emit(id);
                addDepth(-1);
                hasResult = true;
                break;
            }
            case ID_NOT:
            case ID_NEG: {
                compile(expr[1], true);
                emit(id);
                hasResult = true;
                break;
            }
            case ID_THIS: case ID_PUSH_NIL: {
                emit(id);
                addDepth(1);
                hasResult = true;
                break;
            }
            case ID_LITERAL: {
                emit(id);
                pushShort(constPoolId(expr[1]));
                hasResult = true;
                addDepth(1);
                break;
            }
            case ID_BLOCK: {
                for (int i = 1; i < expr.length; i++) {
                    compile(expr[i], false);
                }
                hasResult = false;
                break;
            }
            case ID_RETURN: {
                compile(expr[1], true);
                emit(ID_RETURN);
                pushShort(depth);
                addDepth(-1);
                hasResult = false;
                break;
            }
            case ID_IDENT: {
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
            }
            case ID_VAR: {
                int id2 = childType(expr, 1);
                if (id2 == ID_IDENT) {
                    hasResult = false;
                } else if (id2 == ID_SET) {
                    compile(expr[1], yieldResult);
                    hasResult = yieldResult;
                } else {
                    throw new Error("Error in var statement: " + stringify(expr));
                }
                break;
            }
            case ID_SET: {
                assertLength(expr, 3);
                int targetType = childType(expr, 1);
                if (targetType == ID_IDENT) {
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
                } else if(targetType == ID_SUBSCRIPT) {
                    Object[] subs = (Object[]) expr[1];
                    compile(subs[1], true);
                    compile(subs[2], true);
                    compile(expr[2], true);
                    emit(ID_PUT);
                    addDepth(-2);
                    hasResult = true;
                } else {
                    throw new Error("Uncompilable assignment operator: " + stringify(expr));
                }
                break;
            }
            case ID_PAREN: {
                if (expr.length != 2) {
                    throw new Error("Unexpected content of parenthesis: " + stringify(expr));
                }
                compile(expr[1], yieldResult);
                hasResult = yieldResult;
                break;
            }
            case ID_CALL_FUNCTION: {
                expr = stripSep(expr);
                boolean methodcall = (childType(expr, 1) == ID_SUBSCRIPT);

                if(methodcall) {
                    emit(ID_THIS);
                    addDepth(1);
                } 

                // save program counter
                emit(ID_SAVE_PC);
                addDepth(RET_FRAME_SIZE);

                // find the method/function
                if(methodcall) {
                    Object[] subs = (Object[]) expr[1];
                    compile(subs[1], true);
                    emit(ID_DUP);
                    addDepth(1);
                    emit(ID_SET_THIS);
                    addDepth(-1);
                    compile(subs[2], true);
                    emit(ID_SUBSCRIPT);
                    addDepth(-1);
                } else {
                    compile(expr[1], true);
                }

                // evaluate parameters
                for (int i = 2; i < expr.length; i++) {
                    compile(expr[i], true);
                }

                // call the function
                emit(ID_CALL_FN);
                if (expr.length > 129) {
                    throw new Error("too many parameters");
                }
                emit(expr.length - 2);
                addDepth(2 - expr.length - RET_FRAME_SIZE);
                if(methodcall) {
                    emit(ID_SWAP);
                    emit(ID_SET_THIS);
                    addDepth(-1);
                } 
                hasResult = true;
                break;
            }
            case ID_BUILD_FUNCTION: {
                Object[] vars = ((Code) expr[1]).closure;
                for (int i = 0; i < vars.length; i++) {
                    String name = (String) vars[i];
                    if (varsClosure.contains(name)) {
                        code.append(ID_GET_BOXED_CLOSURE);
                        pushShort(varsClosure.indexOf(name));
                    } else {
                        code.append(ID_GET_LOCAL);
                        pushShort(depth - varsLocals.indexOf(name) - 1);
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

            }
            case ID_IF: {
                int subtype = childType(expr, 2);

                if (subtype == ID_ELSE) {
                    Object[] branch = (Object[]) expr[2];

                    //    code for condition
                    //    jump_if_true -> label1
                    //    code for branch2
                    //    jump -> label2
                    // label1:
                    //    code for branch1
                    // label2:

                    int pos0, pos1, len;
                    // compile condition
                    compile(expr[1], true);

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
                    int pos0, len;

                    compile(expr[1], true);

                    code.append(ID_JUMP_IF_FALSE);
                    pushShort(0);
                    pos0 = code.length();
                    addDepth(-1);

                    curlyToBlock(expr[2]);
                    compile(expr[2], false);

                    len = code.length() - pos0;
                    setShort(pos0, len);

                    hasResult = false;
                    break;
                }
            }
            case ID_FOR: {
                Object[] args = (Object[]) expr[1];
                Object init, cond, step;
                if(args.length > 2) {
                    int pos = 1;
                    init = args[pos];
                    pos += (init == SEP_TOKEN) ? 1 : 2;
                    cond = args[pos];
                    pos += (cond == SEP_TOKEN) ? 1 : 2;
                    step = (pos < args.length) ? args[pos] : SEP_TOKEN;
                } else {
                    throw new Error("unsupported for: " + stringify(expr));
                }

                curlyToBlock(expr[2]);
                compile(v(ID_BLOCK, init, v(ID_WHILE, cond, v(ID_BLOCK, expr[2], step))), yieldResult);
                hasResult = yieldResult;
                break;
            }
            case ID_SEP: {
                hasResult = false;
                break;
            }
            case ID_AND: {
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
            }
            case ID_OR: {
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
            }
            case ID_LIST_LITERAL: {
                code.append(ID_NEW_LIST);
                addDepth(1);

                for (int i = 1; i < expr.length; i++) {
                    if (childType(expr, i) != ID_SEP) {
                        compile(expr[i], true);
                        code.append(ID_PUSH);
                        addDepth(-1);
                    }
                }
                hasResult = true;

                break;
            }
            case ID_CURLY: {
                code.append(ID_NEW_DICT);
                addDepth(1);

                int i = 0;
                while (i < expr.length - 3) {
                    do {
                        ++i;
                    } while (childType(expr, i) == ID_SEP);
                    compile(expr[i], true);
                    do {
                        ++i;
                    } while (childType(expr, i) == ID_SEP);
                    compile(expr[i], true);
                    code.append(ID_PUT);
                    addDepth(-2);
                }
                hasResult = true;

                break;
            }
            case ID_THROW: {
                compile(expr[1], true);
                code.append(ID_THROW);
                addDepth(-1);
                hasResult = false;
                break;
            }
            case ID_TRY: {
                //   try -> labelHandle;
                //   ... body
                //   untry
                //   jump -> labelExit;
                // labelHandle:
                //   set var <- Exception
                //   handleExpr
                // labelExit:
                int pos0, pos1, len;

                Object[] catchExpr = (Object[])expr[2];

                emit(ID_TRY);
                pushShort(0);
                pos0 = code.length();
                addDepth(TRY_FRAME_SIZE);

                curlyToBlock(expr[1]);
                compile(expr[1], false);

                emit(ID_UNTRY);
                addDepth(-TRY_FRAME_SIZE);

                emit(ID_JUMP);
                pushShort(0);
                pos1 = code.length();

                // lableHandle:
                setShort(pos0, code.length() - pos0);

                addDepth(1);
                Object name = ((Object [])((Object[])catchExpr[1])[1])[1];

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

                emit(ID_DROP);
                addDepth(-1);

                curlyToBlock(catchExpr[2]);
                compile(catchExpr[2], false);

                setShort(pos1, code.length() - pos1);

                hasResult = false;

                break;

            }
            case ID_WHILE: {
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
            case ID_DO: {
                int pos0;
                pos0 = code.length();

                curlyToBlock(expr[1]);
                compile(expr[1], false);

                compile(((Object[])expr[2])[1], true);
                emit(ID_JUMP_IF_TRUE);
                pushShort(pos0 - code.length() - 2);
                addDepth(-1);
                hasResult = false;
                break;
            }
            default:
                throw new Error("Uncompilable expression: " + stringify(expr));
        }

        if (hasResult && !yieldResult) {
            emit(ID_DROP);
            addDepth(-1);
        } else if (yieldResult && !hasResult) {
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

    private static Object execute(Code cl) throws LightScriptException {
        int sp = -1;
        Object[] stack = new Object[0];
        Object thisPtr = null;
        return execute(cl, stack, sp, thisPtr, 0);
    }
    private static Object execute(Code cl, Object[] stack, int sp, Object thisPtr, int argcount) throws LightScriptException {
        //System.out.println(stringify(cl));
        int pc = -1;
        byte[] code = cl.code;
        Object[] constPool = cl.constPool;
        Object[] closure = cl.closure;
        int exceptionHandler = - 1;
        int startStackPos = sp + 1 - argcount;
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
                    if (sp <= startStackPos) {
                        if(sp < startStackPos) {
                            throw new Error("Wrong stack discipline" + sp + " " +startStackPos);
                        }
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
                    Object o = stack[sp - argc];
                    if(o instanceof Code) {
                        Code fn = (Code) o;
                        stack[sp - argc] = new Integer(pc);
                        pc = -1;
                        code = fn.code;
                        constPool = fn.constPool;
                        closure = fn.closure;
                    } else if (o instanceof LightScriptFunction) {
                        try {
                            Object result = ((LightScriptFunction)o).apply(thisPtr, stack, sp - argc + 1, argc);
                            sp -= argc + RET_FRAME_SIZE;
                            stack[sp] = result;
                        } catch(LightScriptException e) {
                    if(exceptionHandler < 0) {
                        throw e;
                    } else {
                        System.out.println(stringify(stack));
                        sp = exceptionHandler;
                        exceptionHandler = ((Integer) stack[sp]).intValue();
                        pc = ((Integer) stack[--sp]).intValue();
                        code = (byte[]) stack[--sp];
                        constPool = (Object[]) stack[--sp];
                        closure = (Object[]) stack[--sp];
                        stack[sp] = e.value;
                    }
                    break;
                        }
                    } else {
                        throw new Error("Unknown function:" + o);
                    }
                    break;
                }
                case ID_BUILD_FN: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Code fn = new Code((Code) stack[sp]);
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
                    stack[sp] = new Integer(-((Integer) stack[sp]).intValue());
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
                    } else if (container instanceof LightScriptObject) {
                        ((LightScriptObject) container).set(key, val);
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
                    } else if (container instanceof LightScriptObject) {
                        stack[sp] = ((LightScriptObject) container).get(key);
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
                case ID_SET_THIS: {
                    thisPtr = stack[sp];
                    --sp;
                    break;
                }
                case ID_THIS: {
                    stack[++sp] = thisPtr;
                    break;
                }
                case ID_SWAP: {
                    Object t = stack[sp];
                    stack[sp] = stack[sp - 1];
                    stack[sp - 1] = t;
                    break;
                }
                case ID_THROW: {
                    Object result = stack[sp];
                    if(exceptionHandler < 0) {
                        throw new LightScriptException(result);
                    } else {
                        System.out.println(stringify(stack));
                        sp = exceptionHandler;
                        exceptionHandler = ((Integer) stack[sp]).intValue();
                        pc = ((Integer) stack[--sp]).intValue();
                        code = (byte[]) stack[--sp];
                        constPool = (Object[]) stack[--sp];
                        closure = (Object[]) stack[--sp];
                        stack[sp] = result;
                    }
                    break;
                }
                case ID_TRY: {
                    stack[++sp] = closure;
                    stack[++sp] = constPool;
                    stack[++sp] = code;
                    stack[++sp] = new Integer(pc + readShort(pc, code) + 2);
                    stack[++sp] = new Integer(exceptionHandler);
                    exceptionHandler = sp;
                    pc += 2;
                    break;
                }
                case ID_UNTRY: {
                    exceptionHandler = ((Integer) stack[sp]).intValue();
                    sp -= TRY_FRAME_SIZE;
                    break;
                }
                default: {
                    throw new Error("Unknown opcode: " + code[pc]);
                }
            }
        }
    }
}
