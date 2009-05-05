/**
This software is released under the 
GNU GENERAL PUBLIC LICENSE version 3
(the actual license text can be retrieved
 from the Free Software Foundation)

Copyright, 2009, Rasmus Jensen, rasmus@lightscript.net

Contact for other licensing options.
*/

import java.io.InputStream;
import java.util.Enumeration;
import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Stack;

/*`\section{Definitions, API, and utility functions}'*/

/* If debugging is enabled, more tests are run during run-time,
 * and errors may be caught in a more readable way.
 * It also adds support for more readable printing of
 * id, etc.
 */
#define __DEBUG__

/* If enabled, wipe the stack on function exit,
 * to kill dangling pointers on execution stack,
 * for better GC at performance price
 */
#define __CLEAR_STACK__

//#define __HAVE_DOUBLE__

/* Identifiers, used both as node type,
 * and also used as opcode. 
 */
#define ID_NONE 127
#define ID_TRUE 0
#define ID_FALSE 1
#define ID_UNDEFINED 2
#define ID_NULL 3
#define ID_PAREN 4
#define ID_LIST_LITERAL 5
#define ID_CURLY 6
#define ID_VAR 7
#define ID_BUILD_FUNCTION 8
#define ID_IF 9
#define ID_WHILE 10
#define ID_CALL_FUNCTION 11
#define ID_AND 12
#define ID_OR 13
#define ID_ELSE 14
#define ID_SET 15
#define ID_IDENT 16
#define ID_BLOCK 17
#define ID_SEP 18
#define ID_IN 19
#define ID_FOR 20
#define ID_END 21
#define ID_CATCH 22
#define ID_DO 23
#define ID_INC 24
#define ID_DEC 25
#define ID_ADD 26
#define ID_EQUALS 27
#define ID_LESS 29
#define ID_LESS_EQUALS 30
#define ID_LITERAL 31
#define ID_MUL 32
#define ID_NEG 33
#define ID_NOT 34
#define ID_NOT_EQUALS 35
#define ID_REM 37
#define ID_RETURN 38
#define ID_SHIFT_RIGHT_ARITHMETIC 39
#define ID_SUB 40
#define ID_SUBSCRIPT 41
#define ID_THIS 42
#define ID_THROW 43
#define ID_TRY 44
#define ID_UNTRY 45
#define ID_BOX_IT 46
#define ID_BUILD_FN 47
#define ID_CALL_FN 48
#define ID_DROP 49
#define ID_DUP 50
#define ID_GET_BOXED 52
#define ID_GET_BOXED_CLOSURE 53
#define ID_GET_CLOSURE 54
#define ID_GET_LOCAL 55
#define ID_INC_SP 56
#define ID_JUMP 57
#define ID_JUMP_IF_FALSE 58
#define ID_JUMP_IF_TRUE 59
#define ID_NEW_DICT 60
#define ID_NEW_LIST 61
#define ID_NEXT 62
#define ID_POP 63
#define ID_PUSH 64
#define ID_PUT 65
#define ID_SAVE_PC 66
#define ID_SET_BOXED 67
#define ID_SET_CLOSURE 68
#define ID_SET_LOCAL 69
#define ID_SET_THIS 70
#define ID_SWAP 71
#define ID_DIV 72
#define ID_NEW_ITER 73
#define ID_JUMP_IF_UNDEFINED 74
#define ID_DELETE 75
#define ID_NEW 76
#define ID_GLOBAL 77
#define ID_SHIFT_RIGHT 78
#define ID_SHIFT_LEFT 79
#define ID_BITWISE_OR 81
#define ID_BITWISE_XOR 82
#define ID_BITWISE_AND 83
#define ID_BITWISE_NOT 84


/* The function id for the null denominator functions */
#define NUD_NONE 13
#define NUD_IDENT 1
#define NUD_LITERAL 2
#define NUD_END 3
#define NUD_SEP 4
#define NUD_LIST 5
#define NUD_PREFIX 6
#define NUD_PREFIX2 7
#define NUD_FUNCTION 8
#define NUD_VAR 9
#define NUD_ATOM 10
#define NUD_CATCH 11
#define NUD_CONST 12

/* The function id for the null denominator functions */
#define LED_NONE 8
#define LED_DOT 1
#define LED_INFIX 2
#define LED_INFIXR 3
#define LED_INFIX_LIST 4
#define LED_INFIX_IF 5
#define LED_OPASSIGN 6
#define LED_INFIX_SWAP 7

/* Tokens objects are encoded as integers */

/** The number of bits per denominator function */
#define SIZE_FN 4

/** The number of bits per id */
#define SIZE_ID 7

/* Masks for function/id */
#define MASK_ID ((1<<SIZE_ID) - 1)
#define MASK_FN ((1<<SIZE_FN) - 1)

/* Mask for the binding power / priority */
#define MASK_BP (-1 << (2*SIZE_ID + 2 *SIZE_FN))

/** The sep token, encoded as an integer */
#define TOKEN_SEP ((((((((\
                      0 << SIZE_FN)\
                    | NUD_SEP) << SIZE_ID)\
                    | ID_NONE) << SIZE_FN)\
                    | LED_NONE) << SIZE_ID)\
                    | ID_NONE)

/** The end token, encoded as an integer */
#define TOKEN_END ((((((((\
                      0 << SIZE_FN)\
                    | NUD_END) << SIZE_ID)\
                    | ID_NONE) << SIZE_FN)\
                    | LED_NONE) << SIZE_ID)\
                    | ID_NONE)

/** The token used for literals, encoded as an integer */
#define TOKEN_LITERAL ((((((((\
                      0 << SIZE_FN)\
                    | NUD_LITERAL) << SIZE_ID)\
                    | ID_NONE) << SIZE_FN)\
                    | LED_NONE) << SIZE_ID)\
                    | ID_NONE)

/** The token used for identifiers, encoded as an integer */
#define TOKEN_IDENT ((((((((\
                      0 << SIZE_FN)\
                    | NUD_IDENT) << SIZE_ID)\
                    | ID_NONE) << SIZE_FN)\
                    | LED_NONE) << SIZE_ID)\
                    | ID_NONE)

/** Sizes of different kinds of stack frames */
#define RET_FRAME_SIZE 5
#define TRY_FRAME_SIZE 5


/*`\subsection{Variables}'*/
/** Instances of the LightScript object, is an execution context,
  * where code can be parsed, compiled, and executed. 
  *
  * @author Rasmus Jensen, rasmus@lightscript.net
  * @version 1.1
  */
public final class LightScript {

    /** Token used for separators (;,:), which are just discarded */
    private static final Object[] SEP_TOKEN = {new Integer(ID_SEP)};

    /** The true truth value of results 
      * of tests/comparisons within LightScript */
    public static final Object TRUE = new StringBuffer("true");
    /** The null value within LightScript */
    public static final Object NULL = new StringBuffer("null");
    /** The undefined value within LightScript */
    public static final Object UNDEFINED = new StringBuffer("undefined");
    /** The false truth value of results 
      * of tests/comparisons within LightScript */
    public static final Object FALSE = new StringBuffer("false");

    /** Token string when reaching end of file, it can only occur
      * at end of file, as it would otherwise be parsed as three
      * tokens: "(", "EOF", and ")". */
    private static final String EOF = "(EOF)";

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



/*`\subsection{Public functions}'*/

    /** The globals variables in this execution context.
      * they are boxed, in such that they can be passed
      * to the closure of af function, which will then
      * be able to modify it without looking it up here */
    #define EC_GLOBALS 0
    #define EC_OBJECT_PROTOTYPE 1
    #define EC_ARRAY_PROTOTYPE 2
    #define EC_FUNCTION_PROTOTYPE 3
    #define EC_STRING_PROTOTYPE 4
    /* Index in executionContext for the default setter function, 
     * which is called when a property is set on
     * an object which is neither a Stack, Hashtable nor LightScriptObject
     *
     * The apply method of the setter gets the container as thisPtr, 
     * and takes the key and value as arguments
     */
    #define EC_SETTER 5
    /* Index in executionContext for the default getter function, 
     * called when subscripting an object
     * which is not a Stack, Hashtable, String nor LightScriptObject
     * or when the subscripting of any of those objects returns null.
     * (non-integer on stacks/strings, keys not found in Hashtable or 
     * its prototypes, when LightScriptObject.get returns null)
     *
     * The apply method of the getter gets the container as thisPtr, 
     * and takes the key as argument
     */
    #define EC_GETTER 6
    #define EC_WRAPPED_GLOBALS 7
    #define EC_NEW_ITER 8

    /** Get the default iterator function */
    public LightScriptObject defaultIterator() {
        return (LightScriptObject) executionContext[EC_NEW_ITER];
    }
    /** Set the default-setter function.
     * The default iterator function is called as a method on the object,
     * and should return an iterator across that object. */
    public void defaultIterator(LightScriptFunction f) {
        executionContext[EC_NEW_ITER] = f;
    }
    /** Get the default-setter function */
    public LightScriptFunction defaultSetter() {
        return (LightScriptFunction) executionContext[EC_SETTER];
    }
    /** Set the default-setter function.
     * The default setter function is called as a method on the object,
     * with the key and the value as arguments. */
    public void defaultSetter(LightScriptFunction f) {
        executionContext[EC_SETTER] = f;
    }
    /** Get the default-getter function */
    public LightScriptFunction defaultGetter() {
        return (LightScriptFunction) executionContext[EC_GETTER];
    }
    /** Set the default-getter function. 
     * The default getter function is called as a method on the object,
     * with a single argument, which is the key */
    public void defaultGetter(LightScriptFunction f) {
        executionContext[EC_GETTER] = f;
    }
    /**
     * context for execution
     */
    private Object[] executionContext;

    /** Constructor, loading standard library */
    public LightScript() {
        executionContext = new Object[9];
        executionContext[EC_GLOBALS] = new Hashtable();
        StdLib.register(this);
    }

    /** Shorthand for evaluating a string that contains LightScript code */
    public Object eval(String s) throws LightScriptException {
        return eval(new ByteArrayInputStream(s.getBytes()));
    }

    /** Evaluate the next statement from an input stream */
    public Object evalNext(InputStream is) throws LightScriptException {
// if we debug, we want the real exception, with line number..
#ifndef __DEBUG__
        try {
#endif
            if(is != this.is) {
                this.is = is;
                sb = new StringBuffer();
                c = ' ';
                varsArgc = 0;
                nextToken();
            }
            while(token == TOKEN_SEP) {
                nextToken();
            }
            if(tokenVal == EOF || token == TOKEN_END) {
                return null;
            }
            // parse with every var in closure
            varsUsed = varsLocals = varsBoxed = new Stack();
            Object[] os = parse(0);
            varsClosure = varsUsed;
    
            // compile
            Code c = compile(os);
            // create closure from globals
            for(int i = 0; i < c.closure.length; i ++) {
                Object box = ((Hashtable)executionContext[EC_GLOBALS]).get(c.closure[i]);
                if(box == null) {
                    box = new Object[1];
                    ((Object[])box)[0] = UNDEFINED;
                    ((Hashtable)executionContext[EC_GLOBALS]).put(c.closure[i], box);
                }
                c.closure[i] = box;
            }
            return execute(c, new Object[0], 0, executionContext[EC_WRAPPED_GLOBALS]);
// if we debug, we want the real exception, with line number..
#ifndef __DEBUG__
        } catch(Throwable e) {
            throw new LightScriptException(e);
        }
#endif 
    }

    /** Parse and execute LightScript code read from an input stream */
    public Object eval( InputStream is) throws LightScriptException {
        Object result, t = UNDEFINED;
        do {
            result = t;
            t = evalNext(is);
        } while(t != null);

        return result;
    }

    /** Set a global value for this execution context */
    public void set(Object key, Object value) {
        ((LightScriptObject)executionContext[EC_WRAPPED_GLOBALS]).set(key, value);
    }

    /** Retrieve a global value from this execution context */
    public Object get(Object key) {
        return ((LightScriptObject)executionContext[EC_WRAPPED_GLOBALS]).get(key);
    }

/*`\subsection{Debugging}'*/

#ifdef __DEBUG__

    /** Mapping from ID to name of ID */
    private static final String[] idNames = {
        "", "", "", "", "PAREN", "LIST_LITERAL", "CURLY", "VAR", 
        "BUILD_FUNCTION", "IF", "WHILE", "CALL_FUNCTION", "AND",
        "OR", "ELSE", "SET", "IDENT", "BLOCK", "SEP", "IN", "FOR",
        "END", "CATCH", "DO", "INC", "DEC", "ADD", "EQUALS", 
        "NOT_USED_ANYMORE", "LESS", "LESS_EQUALS", "LITERAL", "MUL", "NEG", 
        "NOT", "NOT_EQUALS", "NOT_USED_ANYMORE", "REM", "RETURN", ">>", 
        "SUB", "SUBSCRIPT", "THIS", "THROW", "TRY", "UNTRY", "BOX_IT", 
        "BUILD_FN", "CALL_FN", "DROP", "DUP", "NOT_USED_ANYMORE", 
        "GET_BOXED", "GET_BOXED_CLOSURE", "GET_CLOSURE", "GET_LOCAL", 
        "INC_SP", "JUMP", "JUMP_IF_FALSE", "JUMP_IF_TRUE", "NEW_DICT", 
        "NEW_LIST", "NEXT", "POP", "PUSH", "PUT", "SAVE_PC", 
        "SET_BOXED", "SET_CLOSURE", "SET_LOCAL", "SET_THIS", "SWAP",
        "DIV", "NEW_ITER", "JUMP_IF_UNDEFINED", "DELETE", "NEW", "GLOBAL",
        "SHIFT_RIGHT", "SHIFT_LEFT", "BITWISE_OR", "BITWISE_XOR", "BITWISE_AND",
        "ID_BITWISE_NOT"
    };
    
    /** Function that maps from ID to a string representation of the ID,
      * robust for integers which is not IDs */
    private static String idName(int id) {
        return "" + id + ((id > 0 && id < idNames.length) ? idNames[id] : "");
    }

    /** A toString, that also works nicely on arrays, and LightScript code */
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
#else /* not debug */
#define idName(...) ""
    private static String stringify(Object o) {
        return o.toString();
    }
#endif
    /*`\subsection{Arithmetics}'*/
#ifndef __HAVE_DOUBLE__
#define FRACTIONAL_CLASS FixedPoint
    public static class FixedPoint {
        public long val;
        public FixedPoint(int i) {
            val = (long)i << 32;
        }
        public FixedPoint(long l) {
            val = l;
        }
        public String toString() {
            long l = val;
            StringBuffer sb = new StringBuffer();
            if(l < 0) {
                sb.append('-');
                l = -l;
            }
            sb.append((int)(l >> 32));
            l = (l & 0xffffffffL) + (1L<<31)/100000;
            sb.append('.');
            for(int i = 0; i < 5; i++) {
                l *= 10;
                sb.append(l >>> 32);
                l = l & 0xffffffffL;
            }
            return sb.toString();
        }
    }
    static int toInt(Object o) {
        if(o instanceof Integer) {
            return ((Integer)o).intValue();
        } else if(o instanceof FixedPoint) {
            return (int)(((FixedPoint)o).val >> 32);
        } else /* String */ {
            return Integer.parseInt((String) o);
        }
    }
    static long toFp(Object o) {
        if(o instanceof FixedPoint) {
            return ((FixedPoint)o).val;
        //} else if(o instanceof Integer) {
        }  else { // TODO: maybe add support for string to fp
            return (long)((Integer)o).intValue()<<32;
        }
    }

    static Object toNumObj(long d) {
        int i = (int)d;
        if(-32 < i && i < 32) {
            return new Integer((int)(d >> 32));
        } else {
            return new FixedPoint(d);
        }
    }

    static Object fpNeg(long a) {
        return toNumObj(-a);
    }
    static Object fpAdd(long a, long b) {
        return toNumObj(a + b);
    }
    static Object fpSub(long a, long b) {
        return toNumObj(a - b);
    }
    static Object fpMul(long a, long b) {
        long t = a & 0xffffffffL;
        long result = (t * (b&0xffffffffL));
        result = (result >>> 32) + ((result >>> 31) & 1);
        result += t * (b >>> 32);
        result += b * (a >>> 32);
        return toNumObj(result);
    }
    static Object fpDiv(long a, long b) {
        boolean neg = false;
        if(a == 0) {
            return toNumObj(0);
        }
        if(a < 0) {
            neg = !neg;
            a = -a;
        }

        int shift = 33;
        do {
            a <<= 1;
            shift -= 1;
        } while(a > 0 && shift > 0);
        a >>>= 1;

        b = b >> shift;

        if(b == 0) {
            a = ~0L >>> 1;
        } else {
            a /= b;
        }
        return toNumObj(neg ? -a : a);
    }
    static Object fpRem(long a, long b) {
        return toNumObj(a % b);
    }
    static boolean fpLess(long a, long b) {
        return a < b;
    }
    static boolean fpLessEq(long a, long b) {
        return a <= b;
    }
#else /* not __HAVE_DOUBLE__ */
#define FRACTIONAL_CLASS Double
    static int toInt(Object o) {
        if(o instanceof Integer) {
            return ((Integer)o).intValue();
        } else if(o instanceof Double) {
            return ((Double)o).intValue();
        } else /* String */ {
            return Integer.parseInt((String) o);
        }
    }
    static double toFp(Object o) {
        if(o instanceof Double) {
            return ((Double)o).doubleValue();
        } else if(o instanceof Integer) {
            return ((Integer)o).intValue();
        } else /* string */ {
            return Double.parseDouble((String) o);
        }
    }

    static Object toNumObj(double d) {
        int i = (int) d;
        if(d == i) {
            return new Integer(i);
        } else {
            return new Double(d);
        }
    }

    static Object fpNeg(double d) {
        return toNumObj(-d);
    }
    static Object fpAdd(double a, double b) {
        return toNumObj(a + b);
    }
    static Object fpSub(double a, double b) {
        return toNumObj(a - b);
    }
    static Object fpMul(double a, double b) {
        return toNumObj(a * b);
    }
    static Object fpDiv(double a, double b) {
        return toNumObj(a / b);
    }
    static Object fpRem(double a, double b) {
        return toNumObj(a % b);
    }
    static boolean fpLess(double a, double b) {
        return a < b;
    }
    static boolean fpLessEq(double a, double b) {
        return a <= b;
    }
#endif

    /*`\subsection{Utility functions}'*/

    /* Constructors for nodes of the Abstract Syntax Tree.
     * Each node is an array containing an ID, followed by 
     * its children or literal values */
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

    /** Returns a new object array, with the seperator tokens removed */
    private static Object[] stripSep(Object[] os) {
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

    /** Push a value into a stack if it is not already there */
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

/*`\subsection{Utility classes}'*/


    /*`\subsubsection{StdLib}'*/
    private static class StdLib implements LightScriptFunction, LightScriptObject {
        private int id;
        private Object closure[];
    
        // globally named functions
        #define STD_PRINT (0)
        #define STD_TYPEOF (1)
        #define STD_PARSEINT (2)
        #define STD_CLONE (3)
    
        private static final String[] names = {"print", "gettype", "parseint", "clone"};
        // methods and other stuff added manually to lightscript
        #define STD_GLOBALLY_NAMED (4)
        #define STD_HAS_OWN_PROPERTY (STD_GLOBALLY_NAMED + 0)
        #define STD_ARRAY_PUSH (STD_GLOBALLY_NAMED + 1)
        #define STD_ARRAY_POP (STD_GLOBALLY_NAMED + 2)
        #define STD_ARRAY_JOIN (STD_GLOBALLY_NAMED + 3)
        #define STD_DEFAULT_SETTER (STD_GLOBALLY_NAMED + 4)
        #define STD_DEFAULT_GETTER (STD_GLOBALLY_NAMED + 5)
        #define STD_NEW_ITERATOR (STD_GLOBALLY_NAMED + 6)
        #define STD_INTEGER_ITERATOR (STD_GLOBALLY_NAMED + 7)
        #define STD_ENUMERATION_ITERATOR (STD_GLOBALLY_NAMED + 8)
        #define STD_GLOBAL_WRAPPER (STD_GLOBALLY_NAMED + 9)
    
        private static final int[] argcs = {1, 1, 2, 1
            // not named
            // hasown, push, pop, join
            , 0, 1, 0, 1 
            // default- setter getter
            , 2, 1
            // new iter int-iter enum-iter
            , 0, 0, 0
            // globalwrapper
            , 0
        };

        public void set(Object key, Object value) {
            if(id == STD_GLOBAL_WRAPPER) {
                Object[] box = (Object[])((Hashtable)closure[EC_GLOBALS]).get(key);
                if(box == null) {
                    box = new Object[1];
                    ((Hashtable)closure[EC_GLOBALS]).put(key, box);
                }
                box[0] = value;
            } 
        }

        public Object get(Object key) {
            if(id == STD_GLOBAL_WRAPPER) {
                Object[] box = (Object[])((Hashtable)closure[EC_GLOBALS]).get(key);
                if(box == null) {
                    return null;
                } else {
                    return box[0];
                }
            } else if("length".equals(key)) {
                return new Integer(argcs[id]);
            } else {

                return null;
            }
        }
    
        private static Hashtable clone(Object o) {
            Hashtable result = new Hashtable();
            result.put("prototype", o);
            return result;
        }
    
        private StdLib(int id) {
            this.id = id;
        }
        public Object apply(Object thisPtr, Object[] args, int argpos, int argcount) throws LightScriptException {
            if(argcs[id] >= 0 && argcount != argcs[id]) {
                throw new LightScriptException("Error: Wrong number of arguments");
            }
            switch(id) {
                case STD_PRINT: {
                     System.out.println(args[argpos]);
                     break;
                }
                case STD_TYPEOF: {
                    Object o = args[argpos];
                    if(o instanceof Hashtable) {
                        return "object";
                    } else if(o instanceof Stack) {
                        return "array";
                    } else if(o instanceof Integer) {
                        return "number";
                    } else if(o == LightScript.UNDEFINED) {
                        return "undefined";
                    } else if(o == LightScript.NULL) {
                        return "null";
                    } else if(o == LightScript.TRUE || o == LightScript.FALSE) {
                        return "boolean";
                    } else {
                        return "builtin";
                    }
                }
                case STD_PARSEINT: {
                    return Integer.valueOf((String)args[argpos], ((Integer)args[argpos +1]).intValue());
                }
                case STD_CLONE: {
                    return clone((Hashtable)args[argpos]);
                }
                case STD_HAS_OWN_PROPERTY: {
                    if(thisPtr instanceof Hashtable) {
                        return ((Hashtable)thisPtr).contains(args[argpos])
                                ? LightScript.TRUE
                                : LightScript.FALSE;
                    }
                    break;
                }
                case STD_ARRAY_PUSH: {
                    ((Stack)thisPtr).push(args[argpos]);
                     break;
                }
                case STD_ARRAY_POP: {
                    ((Stack)thisPtr).pop();
                    break;
                }
                case STD_ARRAY_JOIN: {
                    Stack s = (Stack) thisPtr;
                    if(s.size() == 0) {
                        return "";
                    }
                    StringBuffer sb = new StringBuffer();
                    sb.append(s.elementAt(0).toString());
                    String sep = args[argpos].toString();
                    for(int i = 1; i < s.size(); i++) {
                        sb.append(sep);
                        sb.append(s.elementAt(i));
                    }
                    return sb.toString();
                }
                case STD_DEFAULT_SETTER: {
                    Object key = args[argpos];
                    Object val = args[argpos + 1];
                    // implementation like "thisPtr[key] = val"
                    break;
                }
                case STD_DEFAULT_GETTER: {
                    Object key = args[argpos];
                    // implementation like "return thisPtr[key]"
                    break;
                }
                case STD_NEW_ITERATOR: {
                    if(thisPtr instanceof Hashtable) {
                        StdLib result;
                        result = new StdLib(STD_ENUMERATION_ITERATOR);
                        result.closure = new Object[1];
                        result.closure[0] = ((Hashtable)thisPtr).keys();
                        return result;
                    }
                    if(thisPtr instanceof Stack) {
                        StdLib result;
                        result = new StdLib(STD_INTEGER_ITERATOR);
                        result.closure = new Object[2];
                        result.closure[0] = new Integer(-1);
                        result.closure[1] = new Integer(((Stack)thisPtr).size() - 1);
                        return result;
                    }
                    break;
                }
                case STD_INTEGER_ITERATOR: {
                    if(closure[0].equals(closure[1])) {
                        return LightScript.UNDEFINED;
                    }
                    int current = ((Integer)closure[0]).intValue();
                    current = current + 1;
                    Object result = new Integer(current);
                    closure[0] = result;
                    return result;
                }
                case STD_ENUMERATION_ITERATOR: {
                    Enumeration e = (Enumeration) closure[0];
                    if(!e.hasMoreElements()) {
                        return LightScript.UNDEFINED;
                    }
                    return e.nextElement();
                }
                case STD_GLOBAL_WRAPPER: {
                    break;
                }
            }
            return LightScript.UNDEFINED;
        }
    
        public static void register(LightScript ls) {
    
            Hashtable objectPrototype = new Hashtable();
            ls.executionContext[EC_OBJECT_PROTOTYPE] = objectPrototype;
    
            Hashtable arrayPrototype = new Hashtable();
            ls.executionContext[EC_ARRAY_PROTOTYPE] = arrayPrototype;
    
            Hashtable stringPrototype = clone(objectPrototype);
            ls.executionContext[EC_STRING_PROTOTYPE] = stringPrototype;
    
            Hashtable functionPrototype = clone(objectPrototype);
            ls.executionContext[EC_FUNCTION_PROTOTYPE] = functionPrototype;
    
            ls.executionContext[EC_SETTER] = new StdLib(STD_DEFAULT_SETTER);
    
            ls.executionContext[EC_GETTER] = new StdLib(STD_DEFAULT_GETTER);

            ls.executionContext[EC_NEW_ITER] = new StdLib(STD_NEW_ITERATOR);

            StdLib globalWrapper = new StdLib(STD_GLOBAL_WRAPPER);
            globalWrapper.closure = ls.executionContext;
            ls.executionContext[EC_WRAPPED_GLOBALS] = globalWrapper;

    
            for(int i = 0; i < names.length; i++) {
                ls.set(names[i], new StdLib(i));
            }
    
            objectPrototype.put("hasOwnProperty", new StdLib(STD_HAS_OWN_PROPERTY));
            Hashtable object = clone(objectPrototype);
    
            // Create members for array
            arrayPrototype.put("push", new StdLib(STD_ARRAY_PUSH));
            arrayPrototype.put("pop", new StdLib(STD_ARRAY_POP));
            arrayPrototype.put("join", new StdLib(STD_ARRAY_JOIN));
            Hashtable array = clone(arrayPrototype);
    
            Hashtable string = clone(stringPrototype);
    
            Hashtable function = clone(stringPrototype);
    
    
            ls.set("Object", object);
            ls.set("String", string);
            ls.set("Array", array);
            ls.set("Function", function);
        }
    }
    /*`\subsubsection{Code}'*/
    /**
     * Analysis of variables in a function being compiled,
     * updated during the parsing.
     */
    private static class Code implements LightScriptFunction, LightScriptObject {
        public Object apply(Object thisPtr, Object[] args, 
                            int argpos, int argcount) 
                                throws LightScriptException {
#ifdef __DEBUG__
            if(argcount == argc) {
#endif
                Object stack[];
                if(argpos != 0) {
                    stack = new Object[argcount];
                    for(int i = 0; i<argcount; i++) {
                        stack[i] = args[argpos + i];
                    }
                } else {
                    stack = args;
                }
                return execute(this, stack, argcount, thisPtr);
#ifdef __DEBUG__
            } 
            else {
                throw new LightScriptException("Wrong number of arguments");
            }
#endif
        }
        public int argc;
        public byte[] code;
        public Object[] constPool;
        public Object[] closure;
        public int maxDepth;

        public Object get(Object key) {
            if("length".equals(key)) {
                return new Integer(argc);
            }
            Hashtable prototype = (Hashtable)((Object[])constPool[0])[EC_FUNCTION_PROTOTYPE];
            if("prototype".equals(key)) {
                return prototype;
            }
            return prototype.get(key);
        }
        public void set(Object key, Object val) {
        }
        public Code(int argc, byte[] code, Object[] constPool, Object[] closure, int maxDepth) {
            this.argc = argc;
            this.code = code;
            this.constPool = constPool;
            this.closure = closure;
            this.maxDepth = maxDepth;
        }

        public Code(Code cl) {
            this.argc = cl.argc;
            this.code = cl.code;
            this.constPool = cl.constPool;
            this.maxDepth = cl.maxDepth;
        }
    }

/*`\section{Tokeniser}'\index{Tokeniser}*/

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
                                   || ('A' <= c && c <= 'Z');
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
                    for(;;) {
                        nextc();
                        if(c == '*') {
                            nextc();
                            if(c == '/') {
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

/*`\section{Parser}\label{code-lightscript-parser} 
\index{Top down operator precedence parser}'*/

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

    /** Read expressions until an end-token is reached.
      * @param s an accumulator stack where the expressions are appended
      * @return an array of parsed expressions, with s prepended 
      */
    private Object[] readList(Stack s) {
        while (token != TOKEN_END) {
            Object[] p = parse(0);
            s.push(p);
        }
        nextToken();

        Object[] result = new Object[s.size()];
        s.copyInto(result);
        return result;
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
                stackAdd(varsUsed, val);
                return v(ID_IDENT, val);
            case NUD_LITERAL:
                return v(ID_LITERAL, val);
            case NUD_CONST:
                return v(ID_LITERAL, 
                            nudId == ID_TRUE ? TRUE
                          : nudId == ID_FALSE ? FALSE
                          : nudId == ID_NULL ? NULL 
                          : UNDEFINED
                );
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

                boolean isNamed = false;
                String fnName = null;
                // add function arguments to statistics
                varsArgc = args.length - 1;
                if (((Integer) args[0]).intValue() == ID_PAREN) {
                    for (int i = 1; i < args.length; i++) {
                        Object[] os = (Object[]) args[i];
#ifdef __DEBUG__
                        if (((Integer) os[0]).intValue() != ID_IDENT) {
                            throw new Error("parameter not variable name" 
                                            + stringify(args));
                        }
#endif
                        varsLocals.push(os[1]);
                    }
                } else {
#ifdef __DEBUG__
                    if (((Integer) args[0]).intValue() != ID_CALL_FUNCTION) {
                        throw new Error("parameter not variable name" 
                                        + stringify(args));
                    }
#endif
                    varsArgc--;
                    fnName = (String)varsUsed.elementAt(0);
                    varsUsed.removeElementAt(0);
                    isNamed = true;
                    for(int i = 0; i < varsUsed.size(); i++) {
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
                if(isNamed) {
                    result = v(ID_SET, v(ID_IDENT, fnName), result);
                    stackAdd(prevUsed, fnName);
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
                Object[] expr = parse(0);
                int type = ((Integer) expr[0]).intValue();
                if (type == ID_IDENT) {
                    stackAdd(varsLocals, expr[1]);
                } else {
                    Object[] expr2 = (Object[]) expr[1];
#ifdef __DEBUG__
                    if (type == ID_SET 
                    && ((Integer) expr2[0]).intValue() == ID_IDENT) {
#endif
                        stackAdd(varsLocals, expr2[1]);
#ifdef __DEBUG__
                    } 
                    else {
                        throw new Error("Error in var");
                    }
#endif
                }
                return v(nudId, expr);
            default:
#ifdef __DEBUG__
                throw new Error("Unknown token: " + token+ ", val: " + val);
#else
                return null;
#endif
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
                return v(ID_SET, left, v(ledId, left, parse(bp - 1)));
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
#ifdef __DEBUG__
                if(((Integer)right[0]).intValue() != ID_IDENT) {
                    throw new Error("right side of dot not a string: " 
                                    + stringify(right));
                }
#endif

                right[0] = new Integer(ID_LITERAL);
                return v(ID_SUBSCRIPT, left, right);
            }
            case LED_INFIX_IF: {
                Object branch1 = parse(0);
#ifdef __DEBUG__
                if(parse(0) != SEP_TOKEN) {
                    throw new Error("infix if error");
                }
#else
                parse(0);
#endif
                Object branch2 = parse(0);
                return v(ID_IF, left, v(ID_ELSE, branch1, branch2));
            }
            default:
#ifdef __DEBUG__
                throw new Error("Unknown led token: " + token);
#else  
                return null;
#endif
        }
    }
    private static Hashtable idMapping;

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
            + (char) ID_NONE
            + (char) LED_NONE
            + (char) ID_NONE
        + "]"
            + (char) 1
            + (char) NUD_END
            + (char) ID_NONE
            + (char) LED_NONE
            + (char) ID_NONE
        + ")"
            + (char) 1
            + (char) NUD_END
            + (char) ID_NONE
            + (char) LED_NONE
            + (char) ID_NONE
        + "}"
            + (char) 1
            + (char) NUD_END
            + (char) ID_NONE
            + (char) LED_NONE
            + (char) ID_NONE
        + "."
            + (char) 8
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_DOT
            + (char) ID_SUBSCRIPT
        + "("
            + (char) 7
            + (char) NUD_LIST
            + (char) ID_PAREN
            + (char) LED_INFIX_LIST
            + (char) ID_CALL_FUNCTION
        + "["
            + (char) 7
            + (char) NUD_LIST
            + (char) ID_LIST_LITERAL
            + (char) LED_INFIX_LIST
            + (char) ID_SUBSCRIPT
        + ">>"
            + (char) 6 
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_SHIFT_RIGHT_ARITHMETIC
        + "<<"
            + (char) 6 
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_SHIFT_LEFT
        + "|"
            + (char) 3 
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_BITWISE_OR
        + "^"
            + (char) 3 
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_BITWISE_XOR
        + "&"
            + (char) 3 
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_BITWISE_AND
        + "~"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_BITWISE_NOT
            + (char) LED_NONE
            + (char) ID_NONE
        + ">>>"
            + (char) 6 
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_SHIFT_RIGHT
        + "/"
            + (char) 6
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_DIV
        + "*"
            + (char) 6
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_MUL
        + "%"
            + (char) 6
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_REM
        + "+"
            + (char) 5
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_ADD
        + "-"
            + (char) 5
            + (char) NUD_PREFIX
            + (char) ID_NEG
            + (char) LED_INFIX
            + (char) ID_SUB
        + "=="
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_EQUALS
        + "==="
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_EQUALS
        + "!="
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_NOT_EQUALS
        + "!=="
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_NOT_EQUALS
        + "<="
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_LESS_EQUALS
        + "<"
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX
            + (char) ID_LESS
        + ">="
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX_SWAP
            + (char) ID_LESS_EQUALS
        + ">"
            + (char) 4
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX_SWAP
            + (char) ID_LESS
        + "&&"
            + (char) 3
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIXR
            + (char) ID_AND
        + "||"
            + (char) 3
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIXR
            + (char) ID_OR
        + "else"
            + (char) 3
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIXR
            + (char) ID_ELSE
        + "in"
            + (char) 3
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIXR
            + (char) ID_IN
        + "?"
            + (char) 3
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIX_IF
            + (char) ID_NONE
        + "="
            + (char) 2
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_INFIXR
            + (char) ID_SET
        + "+="
            + (char) 2
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_OPASSIGN
            + (char) ID_ADD
        + "-="
            + (char) 2
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_OPASSIGN
            + (char) ID_SUB
        + "*="
            + (char) 2
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_OPASSIGN
            + (char) ID_MUL
        + "/="
            + (char) 2
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_OPASSIGN
            + (char) ID_DIV
        + "%="
            + (char) 2
            + (char) NUD_NONE
            + (char) ID_NONE
            + (char) LED_OPASSIGN
            + (char) ID_REM
        + "++"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_INC
            + (char) LED_NONE
            + (char) ID_NONE
        + "--"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_DEC
            + (char) LED_NONE
            + (char) ID_NONE
        + ":"
            + (char) 1
            + (char) NUD_SEP
            + (char) ID_NONE
            + (char) LED_NONE
            + (char) ID_NONE
        + ";"
            + (char) 1
            + (char) NUD_SEP
            + (char) ID_NONE
            + (char) LED_NONE
            + (char) ID_NONE
        + ","
            + (char) 1
            + (char) NUD_SEP
            + (char) ID_NONE
            + (char) LED_NONE
            + (char) ID_NONE
        + "{"
            + (char) 1
            + (char) NUD_LIST
            + (char) ID_CURLY
            + (char) LED_NONE
            + (char) ID_NONE
        + "var"
            + (char) 1
            + (char) NUD_VAR
            + (char) ID_VAR
            + (char) LED_NONE
            + (char) ID_NONE
        + "delete"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_DELETE
            + (char) LED_NONE
            + (char) ID_NONE
        + "new"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_NEW
            + (char) LED_NONE
            + (char) ID_NONE
        + "return"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_RETURN
            + (char) LED_NONE
            + (char) ID_NONE
        + "!"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_NOT
            + (char) LED_NONE
            + (char) ID_NONE
        + "throw"
            + (char) 1
            + (char) NUD_PREFIX
            + (char) ID_THROW
            + (char) LED_NONE
            + (char) ID_NONE
        + "try"
            + (char) 1
            + (char) NUD_PREFIX2
            + (char) ID_TRY
            + (char) LED_NONE
            + (char) ID_NONE
        + "catch"
            + (char) 1
            + (char) NUD_CATCH
            + (char) ID_CATCH
            + (char) LED_NONE
            + (char) ID_NONE
        + "function"
            + (char) 1
            + (char) NUD_FUNCTION
            + (char) ID_BUILD_FUNCTION
            + (char) LED_NONE
            + (char) ID_NONE
        + "do"
            + (char) 1
            + (char) NUD_PREFIX2
            + (char) ID_DO
            + (char) LED_NONE
            + (char) ID_NONE
        + "for"
            + (char) 1
            + (char) NUD_PREFIX2
            + (char) ID_FOR
            + (char) LED_NONE
            + (char) ID_NONE
        + "if"
            + (char) 1
            + (char) NUD_PREFIX2
            + (char) ID_IF
            + (char) LED_NONE
            + (char) ID_NONE
        + "while"
            + (char) 1
            + (char) NUD_PREFIX2
            + (char) ID_WHILE
            + (char) LED_NONE
            + (char) ID_NONE
        + "undefined"
            + (char) 1
            + (char) NUD_CONST
            + (char) ID_UNDEFINED
            + (char) LED_NONE
            + (char) ID_NONE
        + "null"
            + (char) 1
            + (char) NUD_CONST
            + (char) ID_NULL
            + (char) LED_NONE
            + (char) ID_NONE
        + "false"
            + (char) 1
            + (char) NUD_CONST
            + (char) ID_FALSE
            + (char) LED_NONE
            + (char) ID_NONE
        + "this"
            + (char) 1
            + (char) NUD_ATOM
            + (char) ID_THIS
            + (char) LED_NONE
            + (char) ID_NONE
        + "true"
            + (char) 1
            + (char) NUD_CONST
            + (char) ID_TRUE
            + (char) LED_NONE
            + (char) ID_NONE
        ;
        idMapping = new Hashtable();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < identifiers.length(); i++) {
            int result = identifiers.charAt(i);
            // result is the binding power
            // next in string is encoded nud/led-function/id object
            if(result < 32) {
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
                sb.append((char)result);
            }
        }
    }

    private void resolveToken(Object val) {
        tokenVal = val;

        Object o = idMapping.get(val);
        if(o == null) {
            token = TOKEN_IDENT;
        } else {
            token = ((Integer)o).intValue() + MASK_BP;
        }
    }

/*`\section{Compiler}'*/

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

#ifdef __DEBUG__
    private void assertLength(Object[] list, int len) {
        if (list.length != len) {
            throw new Error("Wrong number of parameters:" + stringify(list));
        }
    }
#else
#define assertLength(...)
#endif

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
        if (((Integer) expr[0]).intValue() == ID_CURLY) {
            expr[0] = new Integer(ID_BLOCK);
        }
    }

    private Code compile(Object[] body) {
        constPool = new Stack();
        constPool.push(executionContext);
        code = new StringBuffer();

        // allocate space for local vars
        maxDepth = depth = varsLocals.size();
        int framesize = depth - varsArgc;
        while (framesize >= 127) {
            emit(ID_INC_SP);
            emit(127);
            framesize -= 127;
        }
        if (framesize > 0) {
            emit(ID_INC_SP);
            emit(framesize);
        }

        // box boxed values in frame
        for (int i = 0; i < varsBoxed.size(); i++) {
            int pos = varsLocals.indexOf(varsBoxed.elementAt(i));
            if (pos != -1) {
                emit(ID_BOX_IT);
                pushShort(depth - pos - 1);
            }
        }

        // compile
        curlyToBlock(body);
        compile(body, true);

        // emit return code, including current stack depth to drop
        emit(ID_RETURN);
        pushShort(depth);

        // patch amount of stack space needed
        maxDepth -= varsArgc;

        // create a new code object;
        Code result = new Code(varsArgc, new byte[code.length()], 
                new Object[constPool.size()], new Object[varsClosure.size()], maxDepth);

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

    private static int childType(Object[] expr, int i) {
        return ((Integer) ((Object[]) expr[i])[0]).intValue();
    }

    /** 
     * Generates code that sets the variable to the value of the 
     * top of the stack, not altering the stack
     * @param name the name of the variable.
     */
    private void compileSet(Object name) {
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
    }

    private void compile(Object rawexpr, boolean yieldResult) {
        boolean hasResult;
        Object[] expr = (Object[]) rawexpr;
        int id = ((Integer) expr[0]).intValue();
        switch (id) {
            case ID_ADD:
            case ID_MUL:
            case ID_DIV:
            case ID_SHIFT_RIGHT_ARITHMETIC:
            case ID_SHIFT_RIGHT:
            case ID_SHIFT_LEFT:
            case ID_BITWISE_OR:
            case ID_BITWISE_XOR:
            case ID_BITWISE_AND:
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
            case ID_BITWISE_NOT:
            case ID_NOT:
            case ID_NEG: {
                compile(expr[1], true);
                emit(id);
                hasResult = true;
                break;
            }
            case ID_DELETE: {
                Object[] expr2 = (Object[]) expr[1];
                int subtype = ((Integer)expr2[0]).intValue();
                if(subtype == ID_SUBSCRIPT) {
                    compile(expr2[1], true);
                    compile(expr2[2], true);
#ifdef __DEBUG__
                } else if(subtype != ID_IDENT) {
                    throw new Error("Deleting non-var");
#endif
                } else {
                    emit(ID_GLOBAL);
                    addDepth(1);
                    compile(expr2[1], true);
                }
                emit(id);
                addDepth(-1);
                hasResult = true;
                break;
            }
            case ID_NEW: {
                int subtype = childType(expr, 1);
                if(subtype != ID_CALL_FUNCTION) {
                    expr = v(ID_CALL_FUNCTION, expr);
                }
                expr[1] = v(ID_SUBSCRIPT, expr, v(ID_LITERAL, "constructor"));
                compile(expr, yieldResult);
                hasResult = yieldResult;
                break;
            }
            case ID_THIS: {
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
#ifdef __DEBUG__
                    if (pos == -1) {
                        throw new Error("Unfound var: " + stringify(expr));
                    }
#endif
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
#ifdef __DEBUG__
                    throw new Error("Error in var statement: " 
                                    + stringify(expr));
#else
                    return;
#endif
                }
                break;
            }
            case ID_SET: {
                assertLength(expr, 3);
                int targetType = childType(expr, 1);
                hasResult = true;
                if (targetType == ID_IDENT) {
                    String name = (String) ((Object[]) expr[1])[1];
                    compile(expr[2], true);
                    compileSet(name);
                } else if(targetType == ID_SUBSCRIPT) {
                    Object[] subs = (Object[]) expr[1];
                    compile(subs[1], true);
                    compile(subs[2], true);
                    compile(expr[2], true);
                    emit(ID_PUT);
                    addDepth(-2);
                } 
#ifdef __DEBUG__
                else {
                    throw new Error("Uncompilable assignment operator: " 
                                    + stringify(expr));
                }
#endif
                break;
            }
            case ID_PAREN: {
#ifdef __DEBUG__
                if (expr.length != 2) {
                    throw new Error("Unexpected content of parenthesis: " 
                                    + stringify(expr));
                }
#endif
                compile(expr[1], yieldResult);
                hasResult = yieldResult;
                break;
            }
            case ID_CALL_FUNCTION: {
                expr = stripSep(expr);
                boolean methodcall = (childType(expr, 1) == ID_SUBSCRIPT);

                // save program counter
                emit(ID_SAVE_PC);
                addDepth(RET_FRAME_SIZE - 1);


                // find the method/function
                if(methodcall) {
                    Object[] subs = (Object[]) expr[1];
                    compile(subs[1], true);

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

                    emit(ID_GLOBAL);
                    addDepth(1);
                    emit(ID_SET_THIS);
                    addDepth(-1);

                    compile(expr[1], true);
                }


                // call the function
                emit(ID_CALL_FN);
#ifdef __DEBUG__
                if (expr.length > 129) {
                    throw new Error("too many parameters");
                }
#endif
                emit(expr.length - 2);
                addDepth(2 - expr.length - RET_FRAME_SIZE);

                hasResult = true;
                break;
            }
            case ID_BUILD_FUNCTION: {
                Object[] vars = ((Code) expr[1]).closure;
                for (int i = 0; i < vars.length; i++) {
                    String name = (String) vars[i];
                    if (varsClosure.contains(name)) {
                        emit(ID_GET_BOXED_CLOSURE);
                        pushShort(varsClosure.indexOf(name));
                    } else {
                        emit(ID_GET_LOCAL);
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

                    emit(ID_JUMP_IF_TRUE);
                    pushShort(0);
                    pos0 = code.length();
                    addDepth(-1);

                    curlyToBlock(branch[2]);
                    compile(branch[2], yieldResult);

                    emit(ID_JUMP);
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

                    emit(ID_JUMP_IF_FALSE);
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
                    //for(..;..;..)
                    int pos = 1;
                    init = args[pos];
                    pos += (init == SEP_TOKEN) ? 1 : 2;
                    cond = args[pos];
                    pos += (cond == SEP_TOKEN) ? 1 : 2;
                    step = (pos < args.length) ? args[pos] : SEP_TOKEN;
                    curlyToBlock(expr[2]);
                    compile(v(ID_BLOCK, init, v(ID_WHILE, cond, 
                                v(ID_BLOCK, expr[2], step))), yieldResult);
                    hasResult = yieldResult;
                    break;
                } else {
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
                    Object[] in = (Object[])((Object[])expr[1])[1];
                    Object name = ((Object[])in[1])[1];
                    if(!(name instanceof String)) {
                        // var name
                        name = ((Object[])name)[1];
                    }
#ifdef __DEBUG__
                    if(!(name instanceof String)) {
                        throw new Error("for-in has no var");
                    }
#endif

                    // evaluate b
                    compile(in[2], true);

                    emit(ID_NEW_ITER);
                    pos0 = code.length();
                    // get next
                    emit(ID_NEXT);
                    addDepth(1);

                    // store value in variable
                    compileSet(name);

                    // exit if done
                    emit(ID_JUMP_IF_UNDEFINED);
                    pushShort(0);
                    pos1 = code.length();
                    addDepth(-1);

                    // compile block
                    curlyToBlock(expr[2]);
                    compile(expr[2], false);

                    emit(ID_JUMP);
                    pushShort(0);

                    setShort(pos1, code.length() - pos1);
                    setShort(code.length(), pos0 - code.length());

                    emit(ID_DROP);
                    addDepth(-1);
                    hasResult = false;
                    
                    break;
                }

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

                compile(v(ID_LITERAL, UNDEFINED), true);

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
                emit(ID_NEW_LIST);
                addDepth(1);

                for (int i = 1; i < expr.length; i++) {
                    if (childType(expr, i) != ID_SEP) {
                        compile(expr[i], true);
                        emit(ID_PUSH);
                        addDepth(-1);
                    }
                }
                hasResult = true;

                break;
            }
            case ID_CURLY: {
                emit(ID_NEW_DICT);
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
                    emit(ID_PUT);
                    addDepth(-2);
                }
                hasResult = true;

                break;
            }
            case ID_THROW: {
                compile(expr[1], true);
                emit(ID_THROW);
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

                compileSet(name);
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
            case ID_DEC: {
                compile(v(ID_SET, expr[1], v(ID_SUB, expr[1], 
                            v(ID_LITERAL, new Integer(1)))), yieldResult);
                return;
            }
            case ID_INC: {
                compile(v(ID_SET, expr[1], v(ID_ADD, expr[1], 
                            v(ID_LITERAL, new Integer(1)))), yieldResult);
                return;
            }
            default:
#ifdef __DEBUG__
                throw new Error("Uncompilable expression: " + stringify(expr));
#else
                return;
#endif
        }

        if (hasResult && !yieldResult) {
            emit(ID_DROP);
            addDepth(-1);
        } else if (yieldResult && !hasResult) {
            compile(v(ID_LITERAL, UNDEFINED), true);
        }
    }

/*`\section{Virtual Machine}\index{Virtual machine}'*/

    private static int readShort(int pc, byte[] code) {
        return (short) (((code[++pc] & 0xff) << 8) | (code[++pc] & 0xff));
    }

    private static boolean toBool(Object o) {
        if(o == TRUE) {
            return true;
        }
        if(o == FALSE || o == NULL || o == UNDEFINED) {
            return false;
        }
        if(o instanceof String) {
            return !((String)o).equals("");
        }
#ifdef DEBUG
        if(o instanceof Integer) {
#endif
            return ((Integer)o).intValue() != 0;
#ifdef DEBUG
        }
        throw new Error("unhandled toBool case for:" + o.toString());
#endif
    }
    private static Object[] ensureSpace(Object[] stack, int sp, int maxDepth) {
        if (stack.length <= maxDepth + sp + 1) {
            // Currently keep the allocate stack tight to max, 
            // to catch errors;
            // Possibly change this to grow exponential 
            // for better performance later on.
            Object[] newstack = new Object[maxDepth + sp + 1];
            System.arraycopy(stack, 0, newstack, 0, sp + 1);
            return newstack;
        }
        return stack;
    }
    /**
     * evaluate some bytecode 
     */
    private static Object execute(Code cl, Object[] stack, int argcount, Object thisPtr) throws LightScriptException {
        int sp = argcount - 1;

        //System.out.println(stringify(cl));
        int pc = -1;
        byte[] code = cl.code;
        Object[] constPool = cl.constPool;
        Object[] closure = cl.closure;
        Object[] executionContext = (Object[]) constPool[0];
        int exceptionHandler = - 1;
        stack = ensureSpace(stack, sp, cl.maxDepth);
#ifdef __CLEAR_STACK__
        int usedStack = sp + cl.maxDepth;
#endif

        for (;;) {
            ++pc;
            /*
            System.out.println("pc:" + pc + " op:"  + idName(code[pc]) 
                             + " sp:" + sp + " stack.length:" + stack.length 
                             + " int:" + readShort(pc, code));
            */
            switch (code[pc]) {
                case ID_INC_SP: {
                    sp += code[++pc];
                    break;
                }
                case ID_RETURN: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object result = stack[sp];
                    sp -= arg - 1;
#ifdef __CLEAR_STACK__
                    for(int i = sp; i <= usedStack; i++) {
                        stack[i] = null;
                    }
#endif
                    if (sp <= 0) {
#ifdef __DEBUG__
                        if(sp < 0) {
                            throw new Error("Wrong stack discipline" 
                                + sp);
                        }
#endif
                        return result;
                    }
                    pc = ((Integer) stack[--sp]).intValue();
                    code = (byte[]) stack[--sp];
                    constPool = (Object[]) stack[--sp];
                    executionContext = (Object[]) constPool[0];
                    closure = (Object[]) stack[--sp];
                    thisPtr = stack[--sp];
                    stack[sp] = result;
                    break;
                }
                case ID_SAVE_PC: {
                    stack[++sp] = thisPtr;
                    stack[++sp] = closure;
                    stack[++sp] = constPool;
                    stack[++sp] = code;
                    break;
                }
                case ID_CALL_FN: {
                    int argc = code[++pc];
                    Object o = stack[sp];
                    --sp;
                    if(o instanceof Code) {
                        Code fn = (Code) o;

                        int deltaSp = fn.argc - argc;
                        stack = ensureSpace(stack, sp, fn.maxDepth + deltaSp);
#ifdef __CLEAR_STACK__
                        usedStack = sp + fn.maxDepth + deltaSp;
#endif
                        sp += deltaSp;
                        argc = fn.argc;

                        for(int i = 0; i < deltaSp; i++) {
                            stack[sp - i] = UNDEFINED;
                        }
                        
                        stack[sp - argc] = new Integer(pc);
                        pc = -1;
                        code = fn.code;
                        constPool = fn.constPool;
                        executionContext = (Object[]) constPool[0];
                        closure = fn.closure;
                    } else if (o instanceof LightScriptFunction) {
                        try {
                            Object result = ((LightScriptFunction)o
                                ).apply(thisPtr, stack, sp - argc + 1, argc);
                            sp -= argc + RET_FRAME_SIZE - 1;
                            stack[sp] = result;
                        } catch(LightScriptException e) {
                            if(exceptionHandler < 0) {
                                throw e;
                            } else {
                                //System.out.println(stringify(stack));
                                sp = exceptionHandler;
                                exceptionHandler = ((Integer) stack[sp]).intValue();
                                pc = ((Integer) stack[--sp]).intValue();
                                code = (byte[]) stack[--sp];
                                constPool = (Object[]) stack[--sp];
                                executionContext = (Object[]) constPool[0];
                                closure = (Object[]) stack[--sp];
                                stack[sp] = e.value;
                            }
                            break;
                        }
#ifdef __DEBUG__
                    } else {
                        throw new Error("Unknown function:" + o);
#endif
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
                case ID_NOT: {
                    stack[sp] = toBool(stack[sp]) ? FALSE : TRUE;
                    break;
                }
                case ID_NEG: {
                    Object o = stack[sp];
                    if(o instanceof Integer) {
                        o = new Integer(-((Integer)o).intValue());
                    } else /* if o is float */ {
                        o = fpNeg(toFp(o));
                    }
                    stack[sp] = o;
                    break;
                }
                case ID_ADD: {
                    Object o2 = stack[sp];
                    --sp;
                    Object o = stack[sp];
                    if(o instanceof Integer && o2 instanceof Integer) {
                        int result = ((Integer) o).intValue();
                        result += ((Integer) o2).intValue();
                        stack[sp] = new Integer(result);
                    } else if( (o instanceof Integer || o instanceof FRACTIONAL_CLASS) 
                           &&  (o2 instanceof Integer || o2 instanceof FRACTIONAL_CLASS) ) {
                    } else {
                        stack[sp] = String.valueOf(o) + String.valueOf(o2);
                    }
                    break;
                }
                case ID_SUB: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if(o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = new Integer(((Integer)o1).intValue() 
                                    - ((Integer)o2).intValue());
                    } else /* float */ {
                        stack[sp] = fpSub(toFp(o1), toFp(o2));
                    }
                    break;
                }
                case ID_SHIFT_RIGHT_ARITHMETIC: {
                    int result = toInt(stack[sp]);
                    result = toInt(stack[--sp]) >> result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_MUL: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if(o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = new Integer(((Integer)o1).intValue() 
                                    * ((Integer)o2).intValue());
                    } else {
                        stack[sp] = fpMul(toFp(o1), toFp(o2));
                    }
                    break;
                }
                case ID_DIV: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    stack[sp] = fpDiv(toFp(o1), toFp(o2));
                    break;
                }
                case ID_REM: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if(o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = new Integer(((Integer)o1).intValue() 
                                    % ((Integer)o2).intValue());
                    } else /* float */ {
                        stack[sp] = fpRem(toFp(o1), toFp(o2));
                    }
                    break;
                }
                case ID_NOT_EQUALS: {
                    Object o = stack[sp];
                    --sp;
                    stack[sp] = (o == null)
                            ? (stack[sp] == null ? FALSE : TRUE )
                            : (o.equals(stack[sp]) ? FALSE : TRUE );
                    break;
                }
                case ID_EQUALS: {
                    Object o = stack[sp];
                    --sp;
                    stack[sp] = (o == null)
                            ? (stack[sp] == null ? TRUE : FALSE)
                            : (o.equals(stack[sp]) ? TRUE : FALSE);
                    break;
                }
                case ID_PUT: {
                    Object val = stack[sp];
                    Object key = stack[--sp];
                    Object container = stack[--sp];

                    if (container instanceof LightScriptObject) {
                        ((LightScriptObject)container).set(key, val);

                    } else if (container instanceof Stack) {
                        int pos = toInt(key);
                        Stack s = (Stack) container;
                        if(pos >= s.size()) {
                            s.setSize(pos + 1);
                        }
                        s.setElementAt(val, pos);

                    } else if (container instanceof Hashtable) {
                        if (val == null) {
                            ((Hashtable) container).remove(key);
                        } else {
                            ((Hashtable) container).put(key, val);
                        }
                    } else {
                        ((LightScriptFunction)executionContext[EC_SETTER]).apply(container, stack, sp + 1, 2);
                    }
                    break;
                }
                case ID_SUBSCRIPT: {
                    Object key = stack[sp];
                    Object container = stack[--sp];
                    Object result = null;


                    // "Object"
                    if (container instanceof LightScriptObject) {
                        result = ((LightScriptObject)container).get(key);
                    }  else if (container instanceof Hashtable) {
                        result = ((Hashtable) container).get(key);
                        if(result == null) {
                            Object prototype = ((Hashtable)container).get("prototype");
                            // repeat case ID_SUBSCRIPT with prototype as container
                            if(prototype != null) {
                                stack[sp] = prototype;
                                sp += 1;
                                pc -= 1;
                                break;
                            }
                        }

                    // "Array"
                    } else if (container instanceof Stack) {
                        if(key instanceof Integer) {
                            int pos = ((Integer) key).intValue();
                            Stack s = (Stack) container;
                            result = 0 <= pos && pos < s.size()
                                         ? s.elementAt(pos)
                                         : null;
                        } else if("length".equals(key)) {
                            result = new Integer(((Stack)container).size());
                        } else {
                            result = ((Hashtable)executionContext[EC_ARRAY_PROTOTYPE]).get(key);
                        }

                    // "String"
                    } else if (container instanceof String) {
                        if(key instanceof Integer) {
                            int pos = ((Integer) key).intValue();
                            String s = (String)container;
                            result = 0 <= pos && pos < s.length()
                                         ? s.substring(pos, pos+1)
                                         : null;
                        } else if("length".equals(key)) {
                            result = new Integer(((String)container).length());
                        } else {
                            result = ((Hashtable)executionContext[EC_STRING_PROTOTYPE]).get(key);
                        }

                    // Other builtin types, by calling userdefined default getter
                    } else {
                        result = ((LightScriptFunction)executionContext[EC_GETTER]).apply(container, stack, sp + 1, 1);
                    } 
                    
                    // prototype property or element within (super-)prototype
                    if(result == null) {
                        if("prototype".equals(key)) {
                            if(container instanceof Stack) {
                                result = (Hashtable)executionContext[EC_ARRAY_PROTOTYPE];
                            } else if(container instanceof String) {
                                result = (Hashtable)executionContext[EC_STRING_PROTOTYPE];
                            } else {
                                result = (Hashtable)executionContext[EC_OBJECT_PROTOTYPE];
                            }
                        } else {
                            result = ((Hashtable)executionContext[EC_OBJECT_PROTOTYPE]).get(key);
                            if(result == null) {
                                result = UNDEFINED;
                            }
                        }
                    }
                    stack[sp] = result;
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
                        stack[sp] = ((Integer) o1).intValue() 
                            < ((Integer) o2).intValue() ? TRUE : FALSE;
                    } else if( (o1 instanceof Integer || o1 instanceof FRACTIONAL_CLASS) 
                           &&  (o2 instanceof Integer || o2 instanceof FRACTIONAL_CLASS) ) {
                        stack[sp] = fpLess(toFp(o1), toFp(o2)) ? TRUE : FALSE;
                    } else {
                        stack[sp] = o1.toString().compareTo(o2.toString()) 
                            < 0 ? TRUE : FALSE;
                    }
                    break;
                }
                case ID_LESS_EQUALS: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = ((Integer) o1).intValue() 
                            <= ((Integer) o2).intValue() ? TRUE : FALSE;
                    } else if( (o1 instanceof Integer || o1 instanceof FRACTIONAL_CLASS) 
                           &&  (o2 instanceof Integer || o2 instanceof FRACTIONAL_CLASS) ) {
                        stack[sp] = fpLessEq(toFp(o1), toFp(o2)) ? TRUE : FALSE;
                    } else {
                        stack[sp] = o1.toString().compareTo(o2.toString()) 
                            <= 0 ? TRUE : FALSE;
                    }
                    break;
                }
                case ID_JUMP: {
                    pc += readShort(pc, code) + 2;
                    break;
                }
                case ID_JUMP_IF_UNDEFINED: {
                    if (UNDEFINED != stack[sp]) {
                        pc += 2;
                    } else {
                        pc += readShort(pc, code) + 2;
                    }
                    --sp;
                    break;
                }
                case ID_JUMP_IF_FALSE: {
                    if (toBool(stack[sp])) {
                        pc += 2;
                    } else {
                        pc += readShort(pc, code) + 2;
                    }
                    --sp;
                    break;
                }
                case ID_JUMP_IF_TRUE: {
                    if (toBool(stack[sp])) {
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
                        //System.out.println(stringify(stack));
                        sp = exceptionHandler;
                        exceptionHandler = ((Integer) stack[sp]).intValue();
                        pc = ((Integer) stack[--sp]).intValue();
                        code = (byte[]) stack[--sp];
                        constPool = (Object[]) stack[--sp];
                        executionContext = (Object[]) constPool[0];
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
                case ID_NEW_ITER: {
                    stack[sp] = ((LightScriptFunction)executionContext[EC_NEW_ITER]).apply(stack[sp], stack, sp, 0);
                    break;
                }
                case ID_NEXT: {
                    LightScriptFunction iter = (LightScriptFunction)stack[sp];
                    stack[++sp] = iter.apply(thisPtr, stack, sp, 0);
                    break;
                }
                case ID_GLOBAL: {
                    stack[++sp] = executionContext[EC_WRAPPED_GLOBALS];
                    break;
                }
                case ID_DELETE: {
                    Object key = stack[sp];
                    Object container = stack[--sp];
                    if(container instanceof Hashtable) {
                        ((Hashtable)container).remove(key);
                    } else if(container instanceof Stack && key instanceof Integer) {
                        ((Stack)container).setElementAt(UNDEFINED, ((Integer)key).intValue());
                    } else if(container instanceof LightScriptObject) {
                        ((LightScriptObject)container).set(key, UNDEFINED);
#ifdef __DEBUG__
                    } else {
                        throw new Error("deleting non-deletable");
#endif
                    }
                    break;
                }
                case ID_SHIFT_RIGHT: {
                    int result = toInt(stack[sp]);
                    result = toInt(stack[--sp]) >>> result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_SHIFT_LEFT: {
                    int result = toInt(stack[sp]);
                    result = toInt(stack[--sp]) << result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_BITWISE_OR:
                {
                    int result = toInt(stack[sp]);
                    result = toInt(stack[--sp]) | result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_BITWISE_XOR:
                {
                    int result = toInt(stack[sp]);
                    result = toInt(stack[--sp]) ^ result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_BITWISE_AND:
                {
                    int result = toInt(stack[sp]);
                    result = toInt(stack[--sp]) & result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case ID_BITWISE_NOT:
                {
                    int result = ~toInt(stack[sp]);
                    stack[sp] = new Integer(result);
                    break;
                }
                default: {
#ifdef __DEBUG__
                    throw new Error("Unknown opcode: " + code[pc]);
#endif
                }
            }
        }
    }
}

