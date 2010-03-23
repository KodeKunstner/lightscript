package com.solsort.lightscript;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Stack;

final class Compiler {
    private String stringify(Object o) {
        return Code.stringify(o);
    }
    private static final boolean DUMP_PARSE_TREE = false;
    /* Constructors for nodes of the Abstract Syntax Tree.
     * Each node is an array containing an ID, followed by
     * its children or literal values */

    /** Token used for separators (,:;), which are just discarded */
    private static final Object[] SEP_TOKEN = {new Integer(Code.SEP)};

    /** (id, o) -> (Object []) {new Integer(id), o} */
    private static Object[] v(int id, Object o) {
        Object[] result = {new Integer(id), o};
        return result;
    }

    /** (id, o1, o2) -> (Object []) {new Integer(id), o1, o2} */
    private static Object[] v(int id, Object o1, Object o2) {
        Object[] result = {new Integer(id), o1, o2};
        return result;
    }

    /** (id, o1, o2, o3) -> (Object []) {new Integer(id), o1, o2, o3} */
    private static Object[] v(int id, Object o1, Object o2, Object o3) {
        Object[] result = {new Integer(id), o1, o2, o3};
        return result;
    }

    private static int getType(Object []expr) {
        return ((Integer)expr[0]).intValue();
    }

    /* The function id for the null denominator functions */
    private static final int NUD_IDENT = 1;
    private static final int NUD_LITERAL = 2;
    private static final int NUD_END = 3;
    private static final int NUD_SEP = 4;
    private static final int NUD_LIST = 5;
    private static final int NUD_PREFIX = 6;
    private static final int NUD_PREFIX2 = 7;
    private static final int NUD_FUNCTION = 8;
    private static final int NUD_VAR = 9;
    private static final int NUD_ATOM = 10;
    private static final int NUD_CATCH = 11;
    private static final int NUD_CONST = 12;
    private static final int NUD_NONE = 13;
    private static final int NUD_FOR = 14;

    /* The function id for the null denominator functions */
    private static final int LED_DOT = 1;
    private static final int LED_INFIX = 2;
    private static final int LED_INFIXR = 3;
    private static final int LED_INFIX_LIST = 4;
    private static final int LED_INFIX_IF = 5;
    private static final int LED_OPASSIGN = 6;
    private static final int LED_INFIX_SWAP = 7;
    private static final int LED_NONE = 8;

    /* Tokens objects are encoded as integers */
    /** The number of bits per denominator function */
    private static final int SIZE_FN = 4;
    /** The number of bits per id */
    private static final int SIZE_ID = 7;

    /* Masks for function/id */
    private static final int MASK_ID = ((1 << SIZE_ID) - 1);
    private static final int MASK_FN = ((1 << SIZE_FN) - 1);

    /* Mask for the binding power / priority */
    private static final int MASK_BP = (-1 << (2 * SIZE_ID + 2 * SIZE_FN));
    /** The sep token, encoded as an integer */
    private static final int TOKEN_SEP = ((((((((0 << SIZE_FN)
                                            | NUD_SEP ) << SIZE_ID)
                                            | Code.SEP) << SIZE_FN)
                                            | LED_NONE) << SIZE_ID)
                                          | Code.NONE);
    /** The semicolon sep token, encoded as an integer */
    private static final int TOKEN_SEMICOLON = ((((((((0 << SIZE_FN)
            | NUD_SEP) << SIZE_ID)
            | Code.NONE) << SIZE_FN)
            | LED_NONE) << SIZE_ID)
            | Code.NONE);
    /** The end token, encoded as an integer */
    private static final int TOKEN_END = ((((((((0 << SIZE_FN)
                                            | NUD_END) << SIZE_ID)
                                            | Code.NONE) << SIZE_FN)
                                            | LED_NONE) << SIZE_ID)
                                          | Code.NONE);
    /** The token used for literals, encoded as an integer */
    private static final int TOKEN_LITERAL = ((((((((0 << SIZE_FN)
            | NUD_LITERAL) << SIZE_ID)
            | Code.NONE) << SIZE_FN)
            | LED_NONE) << SIZE_ID)
            | Code.NONE);
    /** The token used for identifiers, encoded as an integer */
    private static final int TOKEN_IDENT = ((((((((0 << SIZE_FN)
                                            | NUD_IDENT) << SIZE_ID)
                                            | Code.NONE) << SIZE_FN)
                                            | LED_NONE) << SIZE_ID)
                                            | Code.NONE);
    /** Token string when reaching end of file, it can only occur
     * at end of file, as it would otherwise be parsed as three
     * tokens: "(", "EOF", and ")". */
    private static final String EOF = "(EOF)";

    /**
     * Compile the next statement from the inputstream
     * @return An argumentless function, whose execution correspond the execution of the statement.
     */
    public Function compileNextStatement() {
        skipSep();
        if (tokenVal == EOF || token == TOKEN_END) {
            return null;
        }
        // parse with every var in closure
        varsUsed = varsLocals = varsBoxed = new Stack();
        Object[] os = parse(0);
        varsClosure = varsUsed;

        if (DUMP_PARSE_TREE) {
            System.out.println(stringify(os));
        }
        // compile
        Code compiledCode = compile(os);
        // create closure from globals
        for (int i = 0; i < compiledCode.closure.length; i++) {
            compiledCode.closure[i] = ls.getBox(compiledCode.closure[i]);
        }
        return compiledCode;
    }

    public Compiler(InputStream is, LightScript ls) {
        this.ls = ls;
        this.is = is;
        this.sb = new StringBuffer();
        this.c = ' ';
        this.varsArgc = 0;
        nextToken();
    }

    private static Hashtable idMapping;

    // <editor-fold desc="properties">

    private LightScript ls;

    /** This stack is used during compilation to build the constant pool
     * of the compiled function. The constant pool contains the constants
     * that are used during execution of the function */
    private Stack constPool;

    /* Used to keep track of stack depth during compilation, to be able to
     * resolve variables */
    private int maxDepth;
    private int depth;
    /** This stringbuffer is used as a dynamically sized bytevector
     * where opcodes are added, during the compilation */
    private StringBuffer code;
    /** The stream which we are parsing */
    private InputStream is;
    /** the just read character */
    private int c;
    /** the buffer for building the tokens */
    private StringBuffer sb;

    /* Per function statistics
     * Sets of variable names for keeping track of which variables
     * are used where and how, in order to know whether they should
     * be boxed, and be placed on the stack or in closures. */
    /** The variables used within a function */
    private Stack varsUsed;
    /** The variables that needs to be boxed */
    private Stack varsBoxed;
    /** The local variables (arguments, and var-defined) */
    private Stack varsLocals;
    /** The variables in the closure */
    private Stack varsClosure;
    /** The number of arguments to the function, corresponds to the first
     * names in varsLocals */
    private int varsArgc;
    /** The value of the just read token.
     * used if the token is an identifier or literal.
     * possible types are String and Integer */
    private Object tokenVal;
    /** The integer encoded token object, including priority, IDs
     * and Function ids for null/left denominator functions */
    private int token;
    //</editor-fold>

    /*`\section{Tokeniser}'\index{Tokeniser}*/
    // <editor-fold>
    /** Read the next character from the input stream */
    private void nextc() {
        try {
            c = is.read();
        } catch (Exception e) {
            c = -1;
        }
    }

    /** Append a character to the token buffer */
    private void pushc() {
        sb.append((char) c);
        nextc();
    }

    /** Test if the current character is a number */
    private boolean isNum() {
        return '0' <= c && c <= '9';
    }

    /** Test if the current character is alphanumeric */
    private boolean isAlphaNum() {
        return isNum() || c == '_' || ('a' <= c && c <= 'z')
               || c == '$' || ('A' <= c && c <= 'Z');
    }

    /** Test if the current character could be a part of a multi character
     * symbol, such as &&, ||, += and the like. */
    private boolean isSymb() {
        return c == '=' || c == '!' || c == '<' || c == '&' || c == '/' || c == '*'
               || c == '%' || c == '|' || c == '+' || c == '-' || c == '>';
    }

    /** Read the next toke from the input stream.
     * The token is stored in the token and tokenVal property. */
    private void nextToken() {
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
                } else if (c == '*') {
                    for (;;) {
                        nextc();
                        if (c == '*') {
                            nextc();
                            if (c == '/') {
                                break;
                            }
                        }
                    }
                } else {
                    resolveToken("/");
                    return;
                }
            }
            nextc();
        }

        // End of file
        if (c == -1) {
            token = TOKEN_END;
            tokenVal = EOF;
            return;

            // String
        } else if (c == '"' || c == '\'') {
            int quote = c;
            nextc();
            while (c != -1 && c != quote) {
                if (c == '\\') {
                    nextc();
                    if (c == 'n') {
                        c = '\n';
                    }
                }
                pushc();
            }
            nextc();
            token = TOKEN_LITERAL;
            tokenVal = sb.toString();
            return;

            // Number
        } else if (isNum()) {
            do {
                pushc();
            } while (isNum());
            // TODO: handle floating point here...
            token = TOKEN_LITERAL;
            tokenVal = Integer.valueOf(sb.toString());
            return;

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
        resolveToken(sb.toString());
        return;
    }
    //</editor-fold>

    /*`\section{Parser}\label{code-lightscript-parser}
    \index{Top down operator precedence parser}'*/
    //<editor-fold>

    /** Parse the next expression from the input stream
     * @param rbp right binding power
     */
    private Object[] parse(int rbp) {
        Object[] left = nud(token);

        // token & MASK_BP extract the binding power/priority of the token
        while (rbp < (token & MASK_BP)) {
            left = led(token, left);
        }

        return left;
    }

    private void skipSep() {
        while (token == TOKEN_SEP || token == TOKEN_SEMICOLON) {
            nextToken();
        }
    }

    private Object[] readVars(Stack s) {
        {
            do {
                skipSep();
                s.push(parse(0));
            } while (token == TOKEN_SEP);
        }

        return Util.stackToTuple(s);
    }

    /** Read expressions until an end-token is reached.
     * @param s an accumulator stack where the expressions are appended
     * @return an array of parsed expressions, with s prepended
     */
    private Object[] readList(Stack s) {
        while (token != TOKEN_END) {
            s.push(parse(0));
            skipSep();
        }
        nextToken();
        return Util.stackToTuple(s);
    }

    private void doAssert(boolean b) {
        if (LightScript.DEBUG_ENABLED) {
            if (!b) throw new Error("Syntax error");
        }
    }

    private Object[] readForList() {
        nextToken(); // skip "("
        Object o[] = (token == TOKEN_SEMICOLON) ? SEP_TOKEN : parse(0);
        if (token == TOKEN_END) {
            nextToken();
            return o;
        } else {
            doAssert(token==TOKEN_SEMICOLON);
            nextToken();
            Object o2[] = (token == TOKEN_SEMICOLON) ? SEP_TOKEN : parse(0);
            doAssert(token==TOKEN_SEMICOLON);
            nextToken();
            Object o3[] = (token == TOKEN_END) ? SEP_TOKEN : parse(0);
            doAssert(token==TOKEN_END);
            nextToken();
            return v(Code.PAREN, o, o2, o3);
        }
    }

    /** Call the null denominator function for a given token
     * and also read the next token. */
    private Object[] nud(int tok) {
        Object val = tokenVal;
        nextToken();
        int nudId = (tok >> (SIZE_ID + SIZE_FN)) & ((1 << SIZE_ID) - 1);
        // extract the token function id from tok
        switch ((tok >> (SIZE_ID * 2 + SIZE_FN)) & ((1 << SIZE_FN) - 1)) {
        case NUD_IDENT:
            Util.stackPushUnique(varsUsed, val);
            return v(Code.IDENT, val);
        case NUD_LITERAL:
            return v(Code.LITERAL, val);
        case NUD_CONST:
            return v(Code.LITERAL,
                     nudId == Code.TRUE ? LightScript.TRUE
                     : nudId == Code.FALSE ? LightScript.FALSE
                     : nudId == Code.NULL ? LightScript.NULL
                     : LightScript.UNDEFINED);
        case NUD_FOR:
            return v(nudId, readForList(), parse(0));
        case NUD_END:
            return null;// result does not matter,
            // as this is removed during parsing
        case NUD_SEP:
            return SEP_TOKEN;
        case NUD_LIST: {
            Stack s = new Stack();
            s.push(new Integer(nudId));
            return readList(s);
        }
        case NUD_ATOM: {
            Object[] result = {new Integer(nudId)};
            return result;
        }
        case NUD_PREFIX:
            return v(nudId, parse(0));
        case NUD_PREFIX2:
            return v(nudId, parse(0), parse(0));
        case NUD_CATCH: {
            Object[] o = parse(0);
            Util.stackPushUnique(varsLocals, ((Object[]) o[1])[1]);
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
            Object[] args = parse(0);

            boolean isNamed = false;
            String fnName = null;
            // add function arguments to statistics
            varsArgc = args.length - 1;
            if (getType(args) == Code.PAREN) {
                for (int i = 1; i < args.length; i++) {
                    Object[] os = (Object[]) args[i];
                    if (LightScript.DEBUG_ENABLED) {
                        if (getType(os) != Code.IDENT) {
                            throw new Error("parameter not variable name"
                                            + stringify(args));
                        }
                    }
                    varsLocals.push(os[1]);
                }
            } else {
                if (LightScript.DEBUG_ENABLED) {
                    if (getType(args) != Code.CALL_FUNCTION) {
                        throw new Error("parameter not variable name"
                                        + stringify(args));
                    }
                }
                varsArgc--;
                fnName = (String) varsUsed.elementAt(0);
                varsUsed.removeElementAt(0);
                isNamed = true;
                for (int i = 0; i < varsUsed.size(); i++) {
                    varsLocals.push(varsUsed.elementAt(i));
                }

            }

            // parse the body of the function
            // notice that this may update vars{Used|Boxed|Locals}
            Object[] body = parse(0);

            // non-local variables are boxed into the closure
            for (int i = 0; i < varsUsed.size(); i++) {
                Object o = varsUsed.elementAt(i);
                if (!varsLocals.contains(o)) {
                    Util.stackPushUnique(varsBoxed, o);
                }
            }

            //  find the variables in the closure
            // and add that they need to be boxed at parent.
            varsClosure = new Stack();
            for (int i = 0; i < varsBoxed.size(); i++) {
                Object o = varsBoxed.elementAt(i);
                if (!varsLocals.contains(o)) {
                    Util.stackPushUnique(prevBoxed, o);
                    Util.stackPushUnique(varsClosure, o);
                }
            }
            Object[] result = v(nudId, compile(body));
            if (isNamed) {
                result = v(Code.SET, v(Code.IDENT, fnName), result);
                Util.stackPushUnique(prevUsed, fnName);
            }
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
            Object[] expr;
            Stack s = new Stack();
            s.push(new Integer(Code.BLOCK));
            Object[] exprs = readVars(s);
            for (int i=1; i <exprs.length; ++i) {
                expr = (Object[])exprs[i];
                int type = getType(expr);
                if (type == Code.IDENT) {
                    Util.stackPushUnique(varsLocals, expr[1]);
                    exprs[i] = SEP_TOKEN;
                } else {
                    Object[] expr2 = (Object[]) expr[1];
                    if (LightScript.DEBUG_ENABLED) {
                        if (type == Code.SET
                                && getType(expr2) == Code.IDENT) {
                            Util.stackPushUnique(varsLocals, expr2[1]);
                        } else {
                            throw new Error("Error in var");
                        }
                    } else {
                        Util.stackPushUnique(varsLocals, expr2[1]);
                    }
                }
            }
            return exprs;
        default:
            if (LightScript.DEBUG_ENABLED) {
                throw new Error("Unknown token: " + token + ", val: " + val);
            } else {
                return null;
            }
        }
    }

    /** Call the left denominator function for a given token
     * and also read the next token. */
    private Object[] led(int tok, Object left) {
        nextToken();
        int bp = tok & MASK_BP;
        int ledId = tok & ((1 << SIZE_ID) - 1);
        // extract led function id from token
        switch ((tok >> SIZE_ID) & ((1 << SIZE_FN) - 1)) {
        case LED_INFIX:
            return v(ledId, left, parse(bp));
        case LED_INFIX_SWAP:
            return v(ledId, parse(bp), left);
        case LED_OPASSIGN:
            return v(Code.SET, left, v(ledId, left, parse(bp - 1)));
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
            varsUsed = null;
            Object[] right = parse(bp);
            varsUsed = t;
            if (LightScript.DEBUG_ENABLED) {
                if (getType(right) != Code.IDENT) {
                    throw new Error("right side of dot not a string: "
                                    + stringify(right));
                }
            }

            right[0] = new Integer(Code.LITERAL);
            return v(Code.SUBSCRIPT, left, right);
        }
        case LED_INFIX_IF: {
            Object branch1 = parse(0);
            skipSep();
            Object branch2 = parse(0);
            return v(Code.IF, left, v(Code.ELSE, branch1, branch2));
        }
        default:
            if (LightScript.DEBUG_ENABLED) {
                throw new Error("Unknown led token: " + token);
            } else {
                return null;
            }
        }
    }

    // initialise idMappings
    static {
        /** This string is used for initialising the idMapping.
         * Each token type has five properties:
         * First there is the string of the token,
         *   then ther is the bindingpower,
         *   followed by the null denominator function,
         *     and its corresponding id
         *   and finally the left denominator function,
         *     and its corresponding id
         */
        String identifiers = ""
        + "(EOF)"
        + (char) 1
        + (char) NUD_END
        + (char) Code.NONE
        + (char) LED_NONE
        + (char) Code.NONE
        + "]"
        + (char) 1
        + (char) NUD_END
        + (char) Code.NONE
        + (char) LED_NONE
        + (char) Code.NONE
        + ")"
        + (char) 1
        + (char) NUD_END
        + (char) Code.NONE
        + (char) LED_NONE
        + (char) Code.NONE
        + "}"
        + (char) 1
        + (char) NUD_END
        + (char) Code.NONE
        + (char) LED_NONE
        + (char) Code.NONE
        + "."
        + (char) 8
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_DOT
        + (char) Code.SUBSCRIPT
        + "("
        + (char) 7
        + (char) NUD_LIST
        + (char) Code.PAREN
        + (char) LED_INFIX_LIST
        + (char) Code.CALL_FUNCTION
        + "["
        + (char) 7
        + (char) NUD_LIST
        + (char) Code.LIST_LITERAL
        + (char) LED_INFIX_LIST
        + (char) Code.SUBSCRIPT
        + ">>"
        + (char) 6
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.SHIFT_RIGHT_ARITHMETIC
        + "<<"
        + (char) 6
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.SHIFT_LEFT
        + "|"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.BITWISE_OR
        + "^"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.BITWISE_XOR
        + "&"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.BITWISE_AND
        + "~"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.BITWISE_NOT
        + (char) LED_NONE
        + (char) Code.NONE
        + ">>>"
        + (char) 6
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.SHIFT_RIGHT
        + "/"
        + (char) 6
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.DIV
        + "*"
        + (char) 6
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.MUL
        + "%"
        + (char) 6
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.REM
        + "+"
        + (char) 5
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.ADD
        + "-"
        + (char) 5
        + (char) NUD_PREFIX
        + (char) Code.NEG
        + (char) LED_INFIX
        + (char) Code.SUB
        + "=="
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.EQUALS
        + "==="
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.EQUALS
        + "!="
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.NOT_EQUALS
        + "!=="
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.NOT_EQUALS
        + "<="
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.LESS_EQUAL
        + "<"
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX
        + (char) Code.LESS
        + ">="
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX_SWAP
        + (char) Code.LESS_EQUAL
        + ">"
        + (char) 4
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX_SWAP
        + (char) Code.LESS
        + "&&"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIXR
        + (char) Code.AND
        + "||"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIXR
        + (char) Code.OR
        + "else"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIXR
        + (char) Code.ELSE
        + "in"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIXR
        + (char) Code.IN
        + "?"
        + (char) 3
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIX_IF
        + (char) Code.NONE
        + "="
        + (char) 2
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_INFIXR
        + (char) Code.SET
        + "+="
        + (char) 2
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_OPASSIGN
        + (char) Code.ADD
        + "-="
        + (char) 2
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_OPASSIGN
        + (char) Code.SUB
        + "*="
        + (char) 2
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_OPASSIGN
        + (char) Code.MUL
        + "/="
        + (char) 2
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_OPASSIGN
        + (char) Code.DIV
        + "%="
        + (char) 2
        + (char) NUD_NONE
        + (char) Code.NONE
        + (char) LED_OPASSIGN
        + (char) Code.REM
        + "++"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.INC
        + (char) LED_NONE
        + (char) Code.NONE
        + "--"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.DEC
        + (char) LED_NONE
        + (char) Code.NONE
        + ":"
        + (char) 1
        + (char) NUD_SEP
        + (char) Code.SEP
        + (char) LED_NONE
        + (char) Code.NONE
        + ";"
        + (char) 1
        + (char) NUD_SEP
        + (char) Code.NONE // Different than for ":" and ","
        + (char) LED_NONE
        + (char) Code.NONE
        + ","
        + (char) 1
        + (char) NUD_SEP
        + (char) Code.SEP
        + (char) LED_NONE
        + (char) Code.NONE
        + "{"
        + (char) 1
        + (char) NUD_LIST
        + (char) Code.CURLY
        + (char) LED_NONE
        + (char) Code.NONE
        + "var"
        + (char) 1
        + (char) NUD_VAR
        + (char) Code.VAR
        + (char) LED_NONE
        + (char) Code.NONE
        + "delete"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.DELETE
        + (char) LED_NONE
        + (char) Code.NONE
        + "new"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.NEW
        + (char) LED_NONE
        + (char) Code.NONE
        + "return"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.RETURN
        + (char) LED_NONE
        + (char) Code.NONE
        + "!"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.NOT
        + (char) LED_NONE
        + (char) Code.NONE
        + "throw"
        + (char) 1
        + (char) NUD_PREFIX
        + (char) Code.THROW
        + (char) LED_NONE
        + (char) Code.NONE
        + "try"
        + (char) 1
        + (char) NUD_PREFIX2
        + (char) Code.TRY
        + (char) LED_NONE
        + (char) Code.NONE
        + "catch"
        + (char) 1
        + (char) NUD_CATCH
        + (char) Code.CATCH
        + (char) LED_NONE
        + (char) Code.NONE
        + "function"
        + (char) 1
        + (char) NUD_FUNCTION
        + (char) Code.BUILD_FUNCTION
        + (char) LED_NONE
        + (char) Code.NONE
        + "do"
        + (char) 1
        + (char) NUD_PREFIX2
        + (char) Code.DO
        + (char) LED_NONE
        + (char) Code.NONE
        + "for"
        + (char) 1
        + (char) NUD_FOR
        + (char) Code.FOR
        + (char) LED_NONE
        + (char) Code.NONE
        + "if"
        + (char) 1
        + (char) NUD_PREFIX2
        + (char) Code.IF
        + (char) LED_NONE
        + (char) Code.NONE
        + "while"
        + (char) 1
        + (char) NUD_PREFIX2
        + (char) Code.WHILE
        + (char) LED_NONE
        + (char) Code.NONE
        + "undefined"
        + (char) 1
        + (char) NUD_CONST
        + (char) Code.UNDEFINED
        + (char) LED_NONE
        + (char) Code.NONE
        + "null"
        + (char) 1
        + (char) NUD_CONST
        + (char) Code.NULL
        + (char) LED_NONE
        + (char) Code.NONE
        + "false"
        + (char) 1
        + (char) NUD_CONST
        + (char) Code.FALSE
        + (char) LED_NONE
        + (char) Code.NONE
        + "this"
        + (char) 1
        + (char) NUD_ATOM
        + (char) Code.THIS
        + (char) LED_NONE
        + (char) Code.NONE
        + "true"
        + (char) 1
        + (char) NUD_CONST
        + (char) Code.TRUE
        + (char) LED_NONE
        + (char) Code.NONE;
        idMapping = new Hashtable();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < identifiers.length(); i++) {
            int result = identifiers.charAt(i);
            // result is the binding power
            // next in string is encoded nud/led-function/id object
            if (result < 32) {
                // read nud function
                result = (result << SIZE_FN) + identifiers.charAt(++i);
                // read nud identifier
                result = (result << SIZE_ID) + identifiers.charAt(++i);
                // read led function
                result = (result << SIZE_FN) + identifiers.charAt(++i);
                // read led identifier
                result = (result << SIZE_ID) + identifiers.charAt(++i);
                // save to mapping, and start next string.
                idMapping.put(sb.toString(), new Integer(result));
                sb.setLength(0);

                // result is a char to be addded to the string
            } else {
                sb.append((char) result);
            }
        }
    }

    private void resolveToken(Object val) {
        tokenVal = val;

        Object o = idMapping.get(val);
        if (o == null) {
            token = TOKEN_IDENT;
        } else {
            token = ((Integer) o).intValue() + MASK_BP;
        }
    }
    //</editor-fold>

    /*`\section{Compiler}'*/
    //<editor-fold>

    private void pushShort(int i) {
        emit(((i >> 8) & 0xff));
        emit((i & 0xff));
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
        if (LightScript.DEBUG_ENABLED) {
            if (list.length != len) {
                throw new Error("Wrong number of parameters:" + stringify(list));
            }
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

    private static void curlyToBlock(Object oexpr) {
        Object[] expr = (Object[]) oexpr;
        if (getType(expr) == Code.CURLY) {
            expr[0] = new Integer(Code.BLOCK);
        }
    }

    private Code compile(Object[] body) {
        constPool = new Stack();
        constPool.push(ls);
        code = new StringBuffer();

        // allocate space for local vars
        maxDepth = depth = varsLocals.size();
        int framesize = depth - varsArgc;
        while (framesize >= 127) {
            emit(Code.INC_SP);
            emit(127);
            framesize -= 127;
        }
        if (framesize > 0) {
            emit(Code.INC_SP);
            emit(framesize);
        }

        // box boxed values in frame
        for (int i = 0; i < varsBoxed.size(); i++) {
            int pos = varsLocals.indexOf(varsBoxed.elementAt(i));
            if (pos != -1) {
                emit(Code.BOX_IT);
                pushShort(depth - pos - 1);
            }
        }

        // compile
        curlyToBlock(body);
        compile(body, true);

        // emit return code, including current stack depth to drop
        emit(Code.RETURN);
        pushShort(depth);

        // patch amount of stack space needed
        maxDepth -= varsArgc;

        // create a new code object;
        Code result = new Code(varsArgc, new byte[code.length()],
                               Util.stackToTuple(constPool), Util.stackToTuple(varsClosure), maxDepth);

        // copy values into the code object
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

    private static int childType(Object[] expr, int i) {
        return getType((Object[]) expr[i]);
    }

    /**
     * Generates code that sets the variable to the value of the
     * top of the stack, not altering the stack
     * @param name the name of the variable.
     */
    private void compileSet(Object name) {
        int pos = varsClosure.indexOf(name);
        if (pos >= 0) {
            emit(Code.SET_CLOSURE);
            pushShort(pos);
        } else {
            pos = varsLocals.indexOf(name);
            if (varsBoxed.contains(name)) {
                emit(Code.SET_BOXED);
            } else {
                emit(Code.SET_LOCAL);
            }
            pushShort(depth - pos - 1);
        }
    }

    private void compile(Object rawexpr, boolean yieldResult) {
        boolean hasResult;
        Object[] expr = (Object[]) rawexpr;
        int id = getType(expr);
        switch (id) {
        case Code.ADD:
        case Code.MUL:
        case Code.DIV:
        case Code.SHIFT_RIGHT_ARITHMETIC:
        case Code.SHIFT_RIGHT:
        case Code.SHIFT_LEFT:
        case Code.BITWISE_OR:
        case Code.BITWISE_XOR:
        case Code.BITWISE_AND:
        case Code.REM:
        case Code.SUB:
        case Code.EQUALS:
        case Code.NOT_EQUALS:
        case Code.SUBSCRIPT:
        case Code.LESS_EQUAL:
        case Code.LESS: {
            compile(expr[1], true);
            compile(expr[2], true);
            emit(id);
            addDepth(-1);
            hasResult = true;
            break;
        }
        case Code.BITWISE_NOT:
        case Code.NOT:
        case Code.NEG: {
            compile(expr[1], true);
            emit(id);
            hasResult = true;
            break;
        }
        case Code.DELETE: {
            Object[] expr2 = (Object[]) expr[1];
            int subtype = getType(expr2);
            if (subtype == Code.SUBSCRIPT) {
                compile(expr2[1], true);
                compile(expr2[2], true);
            } else if (LightScript.DEBUG_ENABLED && subtype != Code.IDENT) {
                throw new Error("Deleting non-var");
            } else {
                emit(Code.GLOBAL);
                addDepth(1);
                compile(expr2[1], true);
            }
            emit(id);
            addDepth(-1);
            hasResult = true;
            break;
        }
        case Code.NEW: {
            int subtype = childType(expr, 1);
            if (subtype != Code.CALL_FUNCTION) {
                expr = v(Code.CALL_FUNCTION, expr);
            }
            expr[1] = v(Code.SUBSCRIPT, expr, v(Code.LITERAL, "constructor"));
            compile(expr, yieldResult);
            hasResult = yieldResult;
            break;
        }
        case Code.THIS: {
            emit(id);
            addDepth(1);
            hasResult = true;
            break;
        }
        case Code.LITERAL: {
            emit(id);
            pushShort(constPoolId(expr[1]));
            hasResult = true;
            addDepth(1);
            break;
        }
        case Code.BLOCK: {
            for (int i = 1; i < expr.length; i++) {
                compile(expr[i], false);
            }
            hasResult = false;
            break;
        }
        case Code.RETURN: {
            compile(expr[1], true);
            emit(Code.RETURN);
            pushShort(depth);
            addDepth(-1);
            hasResult = false;
            break;
        }
        case Code.IDENT: {
            String name = (String) expr[1];
            int pos = varsClosure.indexOf(name);
            if (pos >= 0) {
                emit(Code.GET_CLOSURE);
                pushShort(pos);
            } else {
                pos = varsLocals.indexOf(name);
                if (LightScript.DEBUG_ENABLED) {
                    if (pos == -1) {
                        throw new Error("Unfound var: " + stringify(expr));
                    }
                }
                if (varsBoxed.contains(name)) {
                    emit(Code.GET_BOXED);
                } else {
                    emit(Code.GET_LOCAL);
                }
                pushShort(depth - pos - 1);
            }
            addDepth(1);
            hasResult = true;
            break;
        }
        case Code.SET: {
            assertLength(expr, 3);
            int targetType = childType(expr, 1);
            hasResult = true;
            if (targetType == Code.IDENT) {
                String name = (String) ((Object[]) expr[1])[1];
                compile(expr[2], true);
                compileSet(name);
            } else if (targetType == Code.SUBSCRIPT) {
                Object[] subs = (Object[]) expr[1];
                compile(subs[1], true);
                compile(subs[2], true);
                compile(expr[2], true);
                emit(Code.PUT);
                addDepth(-2);
            } else {
                if (LightScript.DEBUG_ENABLED) {
                    throw new Error("Uncompilable assignment operator: "
                                    + stringify(expr));
                }
            }
            break;
        }
        case Code.PAREN: {
            assertLength(expr, 2);
            compile(expr[1], yieldResult);
            hasResult = yieldResult;
            break;
        }
        case Code.CALL_FUNCTION: {
            boolean methodcall = (childType(expr, 1) == Code.SUBSCRIPT);

            // save program counter
            emit(Code.SAVE_PC);
            addDepth(Code.RET_FRAME_SIZE);

            // find the method/function
            if (methodcall) {
                Object[] subs = (Object[]) expr[1];
                compile(subs[1], true);

                emit(Code.DUP);
                addDepth(1);

                compile(subs[2], true);
                emit(Code.SUBSCRIPT);
                addDepth(-1);

                emit(Code.SWAP);

            } else {
                compile(expr[1], true);

                emit(Code.GLOBAL);
                addDepth(1);
            }

            // evaluate parameters
            for (int i = 2; i < expr.length; i++) {
                compile(expr[i], true);
            }

            // call the function
            emit(Code.CALL_FN);
            if (LightScript.DEBUG_ENABLED) {
                if (expr.length > 129) {
                    throw new Error("too many parameters");
                }
            }
            emit(expr.length - 2);
            addDepth(1 - expr.length - Code.RET_FRAME_SIZE);

            hasResult = true;
            break;
        }
        case Code.BUILD_FUNCTION: {
            Object[] vars = ((Code) expr[1]).closure;
            for (int i = 0; i < vars.length; i++) {
                String name = (String) vars[i];
                if (varsClosure.contains(name)) {
                    emit(Code.GET_BOXED_CLOSURE);
                    pushShort(varsClosure.indexOf(name));
                } else {
                    emit(Code.GET_LOCAL);
                    pushShort(depth - varsLocals.indexOf(name) - 1);
                }
                addDepth(1);
            }
            emit(Code.LITERAL);
            pushShort(constPoolId(expr[1]));
            addDepth(1);
            emit(Code.BUILD_FN);
            pushShort(vars.length);
            addDepth(-vars.length);
            hasResult = true;
            break;

        }
        case Code.IF: {
            int subtype = childType(expr, 2);

            if (subtype == Code.ELSE) {
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

                emit(Code.JUMP_IF_TRUE);
                pushShort(0);
                pos0 = code.length();
                addDepth(-1);

                curlyToBlock(branch[2]);
                compile(branch[2], yieldResult);

                emit(Code.JUMP);
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

                emit(Code.JUMP_IF_FALSE);
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
        case Code.FOR: {
            assertLength(expr, 3);
            Object[] args = (Object[]) expr[1];
            Object init, cond, step;
            if (args.length == 4) {
                init = args[1];
                cond = args[2];
                step = args[3];
                curlyToBlock(expr[2]);
                compile(v(Code.BLOCK, init, v(Code.WHILE, cond,
                                              v(Code.BLOCK, expr[2], step))), yieldResult);
                hasResult = yieldResult;
                break;
            } else {
                assertLength(args, 3);
                doAssert(getType(args) == Code.IN);
                // for(a in b) c
                //
                //   evalute b
                // labelTop:
                //   getNextElement
                //   save -> a
                //   if no element jump to labelEnd
                //   c
                //   jump -> labelTop
                // labelEnd:
                //   drop iterator.
                int pos0, pos1;

                // find the name
                Object[] in = args;
                Object name = ((Object[]) in[1])[1];
                if (!(name instanceof String)) {
                    // var name
                    name = ((Object[]) name)[1];
                }
                if (LightScript.DEBUG_ENABLED) {
                    if (!(name instanceof String)) {
                        throw new Error("for-in has no var");
                    }
                }

                // evaluate b
                compile(in[2], true);

                emit(Code.NEW_ITER);
                pos0 = code.length();
                // get next
                emit(Code.NEXT);
                addDepth(1);

                // store value in variable
                compileSet(name);

                // exit if done
                emit(Code.JUMP_IF_UNDEFINED);
                pushShort(0);
                pos1 = code.length();
                addDepth(-1);

                // compile block
                curlyToBlock(expr[2]);
                compile(expr[2], false);

                emit(Code.JUMP);
                pushShort(0);

                setShort(pos1, code.length() - pos1);
                setShort(code.length(), pos0 - code.length());

                emit(Code.DROP);
                addDepth(-1);
                hasResult = false;

                break;
            }

        }
        case Code.SEP: {
            assertLength(expr, 1);
            hasResult = false;
            break;
        }
        case Code.AND: {
            assertLength(expr, 3);
            int pos0, pos1, len;

            compile(expr[1], true);

            emit(Code.JUMP_IF_TRUE);
            pushShort(0);
            pos0 = code.length();
            addDepth(-1);

            compile(v(Code.LITERAL, LightScript.UNDEFINED), true);

            emit(Code.JUMP);
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
        case Code.OR: {
            assertLength(expr, 3);
            int pos0, pos1, len;

            compile(expr[1], true);

            emit(Code.DUP);
            addDepth(1);

            emit(Code.JUMP_IF_TRUE);
            pushShort(0);
            pos0 = code.length();
            addDepth(-1);

            emit(Code.DROP);
            addDepth(-1);

            compile(expr[2], true);

            pos1 = code.length();
            len = pos1 - pos0;
            setShort(pos0, len);

            hasResult = true;

            break;
        }
        case Code.LIST_LITERAL: {
            emit(Code.NEW_LIST);
            addDepth(1);

            for (int i = 1; i < expr.length; i++) {
                if (childType(expr, i) != Code.SEP) {
                    compile(expr[i], true);
                    emit(Code.PUSH);
                    addDepth(-1);
                }
            }
            hasResult = true;

            break;
        }
        case Code.CURLY: {
            emit(Code.NEW_DICT);
            addDepth(1);

            int i = 0;
            while (i < expr.length - 2) {
                ++i;
                compile(expr[i], true);
                ++i;
                compile(expr[i], true);
                emit(Code.PUT);
                addDepth(-2);
            }
            hasResult = true;

            break;
        }
        case Code.THROW: {
            compile(expr[1], true);
            emit(Code.THROW);
            addDepth(-1);
            hasResult = false;
            break;
        }
        case Code.TRY: {
            //   try -> labelHandle;
            //   ... body
            //   untry
            //   jump -> labelExit;
            // labelHandle:
            //   set var <- Exception
            //   handleExpr
            // labelExit:
            int pos0, pos1, len;

            Object[] catchExpr = (Object[]) expr[2];

            emit(Code.TRY);
            pushShort(0);
            pos0 = code.length();
            addDepth(Code.TRY_FRAME_SIZE);

            curlyToBlock(expr[1]);
            compile(expr[1], false);

            emit(Code.UNTRY);
            addDepth(-Code.TRY_FRAME_SIZE);

            emit(Code.JUMP);
            pushShort(0);
            pos1 = code.length();

            // lableHandle:
            setShort(pos0, code.length() - pos0);

            addDepth(1);
            Object name = ((Object[]) ((Object[]) catchExpr[1])[1])[1];

            compileSet(name);
            emit(Code.DROP);
            addDepth(-1);

            curlyToBlock(catchExpr[2]);
            compile(catchExpr[2], false);

            setShort(pos1, code.length() - pos1);

            hasResult = false;

            break;

        }
        case Code.WHILE: {
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
            emit(Code.JUMP_IF_FALSE);
            pushShort(0);
            pos1 = code.length();
            addDepth(-1);


            curlyToBlock(expr[2]);
            compile(expr[2], false);

            emit(Code.JUMP);
            pushShort(pos0 - code.length() - 2);

            setShort(pos1, code.length() - pos1);
            hasResult = false;
            break;
        }
        case Code.DO: {
            int pos0;
            pos0 = code.length();

            curlyToBlock(expr[1]);
            compile(expr[1], false);

            compile(((Object[]) expr[2])[1], true);
            emit(Code.JUMP_IF_TRUE);
            pushShort(pos0 - code.length() - 2);
            addDepth(-1);
            hasResult = false;
            break;
        }
        case Code.DEC: {
            compile(v(Code.SET, expr[1], v(Code.SUB, expr[1],
                                           v(Code.LITERAL, Util.integerOne))), yieldResult);
            return;
        }
        case Code.INC: {
            compile(v(Code.SET, expr[1], v(Code.ADD, expr[1],
                                           v(Code.LITERAL, Util.integerOne))), yieldResult);
            return;
        }
        default:
            if (LightScript.DEBUG_ENABLED) {
                throw new Error("Uncompilable expression: " + stringify(expr));
            } else {
                return;
            }
        }

        if (hasResult && !yieldResult) {
            emit(Code.DROP);
            addDepth(-1);
        } else if (yieldResult && !hasResult) {
            compile(v(Code.LITERAL, LightScript.UNDEFINED), true);
        }
    }
    //</editor-fold>
}
