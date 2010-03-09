//<editor-fold desc="license and imports">
package com.solsort.mobile;

/**
This software is released under the 
AFFERO GENERAL PUBLIC LICENSE version 3

(the actual license text can be retrieved
from the Free Software Foundation:
http://www.gnu.org/licenses/agpl-3.0.html)

Copyright, 2009-2010, Rasmus Jensen, rasmus@solsort.com

Contact for other licensing options.
 */
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Stack;

/*`\subsection{Variables}'*/
/** Instances of the LightScript object, is an execution context,
 * where code can be parsed, compiled, and executed.
 *
 * @author Rasmus Jensen, rasmus@lightscript.net
 * @version 1.2
 */
// </editor-fold>
public final class LightScript {
    // TODO: globals object

    public static Object oldGlobalObject = null;
    private static Hashtable globals = new Hashtable();

    public static int toInt(Object object) {
        if (object instanceof Integer) {
            return ((Integer) object).intValue();
        }
        // CLASS DISPATCH

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*`\subsection{Public functions}'*/
    // <editor-fold desc="public api">
    /** Get the default-setter function */
    public Function defaultSetter() {
        return (Function) executionContext[EC_SETTER];
    }

    /** Set the default-setter function.
     * The default setter function is called as a method on the object,
     * with the key and the value as arguments. */
    public void defaultSetter(Function f) {
        executionContext[EC_SETTER] = f;
    }

    /** Get the default-getter function */
    public Function defaultGetter() {
        return (Function) executionContext[EC_GETTER];
    }

    /** Set the default-getter function.
     * The default getter function is called as a method on the object,
     * with a single argument, which is the key */
    public void defaultGetter(Function f) {
        executionContext[EC_GETTER] = f;
    }
    /**
     * context for execution
     */
    public Object[] executionContext;

    /** Constructor, loading standard library */
    public LightScript() {
        executionContext = new Object[9];
        executionContext[EC_GLOBALS] = new Hashtable();
        StdLib.register(this);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f) throws ScriptException {
        Object args[] = {thisPtr};
        return f.apply(args, 0, 0);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f, Object arg1) throws ScriptException {
        Object args[] = {thisPtr, arg1};
        return f.apply(args, 0, 1);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f, Object arg1, Object arg2) throws ScriptException {
        Object args[] = {thisPtr, arg1, arg2};
        return f.apply(args, 0, 2);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f, Object arg1, Object arg2, Object arg3) throws ScriptException {
        Object args[] = {thisPtr, arg1, arg2, arg3};
        return f.apply(args, 0, 3);
    }

    /** Shorthand for evaluating a string that contains LightScript code */
    public Object eval(String s) throws ScriptException {
        return eval(new ByteArrayInputStream(s.getBytes()));
    }

    /** Parse and execute LightScript code read from an input stream */
    public Object eval(InputStream is) throws ScriptException {
        Object result, t = UNDEFINED;
        Compiler c = new Compiler(is, this);
        do {
            result = t;
            t = c.evalNext(is);
        } while (t != null);

        return result;
    }

    public Object[] getBox(Object key) {
        Object box[] = (Object[]) globals.get(key);
        if(box == null) {
            box = new Object[1];
            box[0] = UNDEFINED;
            globals.put(key, box);
        }
        return box;
    }
    /** Set a global value for this execution context */
    public void set(Object key, Object value) {
        getBox(key)[0] = value;
    }

    /** Retrieve a global value from this execution context */
    public Object get(Object key) {
        return getBox(key)[0];
    }
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
    // </editor-fold>
    // <editor-fold desc="options">
    /*`\section{Definitions, API, and utility functions}'*/

    /* If debugging is enabled, more tests are run during run-time,
     * and errors may be caught in a more readable way.
     * It also adds support for more readable printing of
     * id, etc.
     */
    private static final boolean DEBUG_ENABLED = true;
    private static final boolean __PRINT_EXECUTED_INSTRUCTIONS__ = false;
    private static final boolean __DO_YIELD__ = true;

    /* If enabled, wipe the stack on function exit,
     * to kill dangling pointers on execution stack,
     * for better GC at performance price
     */
    private static final boolean __CLEAR_STACK__ = true;
    // </editor-fold>
    // <editor-fold desc="defines">

    /** Sizes of different kinds of stack frames */
    public static final int RET_FRAME_SIZE = 4;
    public static final int TRY_FRAME_SIZE = 5;
    /** Token used for separators (;,:), which are just discarded */
    private static final Object[] SEP_TOKEN = {new Integer(OpCodes.SEP)};
    /** The globals variables in this execution context.
     * they are boxed, in such that they can be passed
     * to the closure of af function, which will then
     * be able to modify it without looking it up here */
    private static final int EC_GLOBALS = 0;
    private static final int EC_OBJECT_PROTOTYPE = 1;
    private static final int EC_ARRAY_PROTOTYPE = 2;
    private static final int EC_FUNCTION_PROTOTYPE = 3;
    private static final int EC_STRING_PROTOTYPE = 4;
    /* Index in executionContext for the default setter function,
     * which is called when a property is set on
     * an object which is neither a Stack, Hashtable nor LightScriptObject
     *
     * The apply method of the setter gets the container as thisPtr,
     * and takes the key and value as arguments
     */
    private static final int EC_SETTER = 5;
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
    private static final int EC_GETTER = 6;
    private static final int EC_NEW_ITER = 8;
    //</editor-fold>
    /*`\subsection{Debugging}'*/
    //<editor-fold>
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
        "OpCodes.BITWISE_NOT"
    };

    /** Function that maps from ID to a string representation of the ID,
     * robust for integers which is not IDs */
    private static String idName(int id) {
        return "" + id + ((id > 0 && id < idNames.length) ? idNames[id] : "");
    }

    /** A toString, that also works nicely on arrays, and LightScript code */
    public static String stringify(Object o) {
        if (DEBUG_ENABLED) {
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
        } else {
            return o.toString();
        }
    }
    /*`\subsection{Utility classes}'*/
    /*`\subsubsection{StdLib}'*/

    /*`\subsubsection{Code}'*/
    /**
     * Analysis of variables in a function being compiled,
     * updated during the parsing.
     */
    
    /*`\section{Virtual Machine}\index{Virtual machine}'*/
    //<editor-fold>

    private static int readShort(int pc, byte[] code) {
        return (short) (((code[++pc] & 0xff) << 8) | (code[++pc] & 0xff));
    }

    private static boolean toBool(Object o) {
        if (o == TRUE) {
            return true;
        }
        if (o == FALSE || o == NULL || o == UNDEFINED) {
            return false;
        }
        if (o instanceof String) {
            return !((String) o).equals("");
        }
        if (DEBUG_ENABLED) {
            if (o instanceof Integer) {
                return ((Integer) o).intValue() != 0;
            }
            throw new Error("unhandled toBool case for:" + o.toString());
        } else {
            return ((Integer) o).intValue() != 0;
        }
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
    public static Object execute(Code cl, Object[] stack, int argcount) throws ScriptException {
        //if(!DEBUG_ENABLED) {
        try {
            //}
            int sp = argcount;

            //System.out.println(stringify(cl));
            int pc = -1;
            byte[] code = cl.code;
            Object[] constPool = cl.constPool;
            Object[] closure = cl.closure;
            Object[] executionContext = ((LightScript)constPool[0]).executionContext;
            int exceptionHandler = - 1;
            stack = ensureSpace(stack, sp, cl.maxDepth);
            Object thisPtr = stack[0];
            int usedStack;
            if (__CLEAR_STACK__) {
                usedStack = sp + cl.maxDepth;
            }

            for (;;) {
                ++pc;
                if (__PRINT_EXECUTED_INSTRUCTIONS__) {
                    System.out.println("pc:" + pc + " op:" + idName(code[pc])
                            + " sp:" + sp + " stack.length:" + stack.length
                            + " int:" + readShort(pc, code));
                }
                switch (code[pc]) {
                    case OpCodes.INC_SP: {
                        sp += code[++pc];
                        break;
                    }
                    case OpCodes.RETURN: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object result = stack[sp];
                        sp -= arg;
                        if (sp == 0) {
                            return result;
                        }
                        if (DEBUG_ENABLED) {
                            if (sp < 0) {
                                throw new Error("Wrong stack discipline"
                                        + sp);
                            }
                        }
                        if (__CLEAR_STACK__) {
                            for (int i = sp; i <= usedStack; i++) {
                                stack[i] = null;
                            }
                        }
                        pc = ((Integer) stack[--sp]).intValue();
                        code = (byte[]) stack[--sp];
                        constPool = (Object[]) stack[--sp];
                        executionContext = ((LightScript)constPool[0]).executionContext;
                        closure = (Object[]) stack[--sp];
                        thisPtr = stack[--sp];
                        stack[sp] = result;
                        if (__DO_YIELD__) {
                            Thread.yield();
                        }
                        break;
                    }
                    case OpCodes.SAVE_PC: {
                        stack[++sp] = thisPtr;
                        stack[++sp] = closure;
                        stack[++sp] = constPool;
                        stack[++sp] = code;
                        break;
                    }
                    case OpCodes.CALL_FN: {
                        int argc = code[++pc];
                        Object o = stack[sp - argc - 1];
                        if (o instanceof Code) {
                            Code fn = (Code) o;

                            int deltaSp = fn.argc - argc;
                            stack = ensureSpace(stack, sp, fn.maxDepth + deltaSp);
                            if (__CLEAR_STACK__) {
                                usedStack = sp + fn.maxDepth + deltaSp;
                            }
                            sp += deltaSp;
                            argc = fn.argc;

                            for (int i = 0; i < deltaSp; i++) {
                                stack[sp - i] = UNDEFINED;
                            }

                            stack[sp - argc - 1] = new Integer(pc);
                            thisPtr = stack[sp - argc];
                            pc = -1;
                            code = fn.code;
                            constPool = fn.constPool;
                            executionContext = ((LightScript)constPool[0]).executionContext;
                            closure = fn.closure;
                        } else if (o instanceof Function) {
                            try {
                                Object result = ((Function) o).apply(stack, sp - argc, argc);
                                sp -= argc + 1 + RET_FRAME_SIZE;
                                stack[sp] = result;
                            } catch (ScriptException e) {
                                if (exceptionHandler < 0) {
                                    throw e;
                                } else {
                                    //System.out.println(stringify(stack));
                                    sp = exceptionHandler;
                                    exceptionHandler = ((Integer) stack[sp]).intValue();
                                    pc = ((Integer) stack[--sp]).intValue();
                                    code = (byte[]) stack[--sp];
                                    constPool = (Object[]) stack[--sp];
                                    executionContext = ((LightScript)constPool[0]).executionContext;
                                    closure = (Object[]) stack[--sp];
                                    stack[sp] = e.value;
                                }
                                break;
                            }
                        } else {
                            if (DEBUG_ENABLED) {
                                throw new Error("Unknown function:" + o);
                            }
                        }
                        break;
                    }
                    case OpCodes.BUILD_FN: {
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
                    case OpCodes.SET_BOXED: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        ((Object[]) stack[sp - arg])[0] = stack[sp];
                        break;
                    }
                    case OpCodes.SET_LOCAL: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[sp - arg] = stack[sp];
                        break;
                    }
                    case OpCodes.SET_CLOSURE: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        ((Object[]) closure[arg])[0] = stack[sp];
                        break;
                    }
                    case OpCodes.GET_BOXED: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object o = ((Object[]) stack[sp - arg])[0];
                        stack[++sp] = o;
                        break;
                    }
                    case OpCodes.GET_LOCAL: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object o = stack[sp - arg];
                        stack[++sp] = o;
                        break;
                    }
                    case OpCodes.GET_CLOSURE: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[++sp] = ((Object[]) closure[arg])[0];
                        break;
                    }
                    case OpCodes.GET_BOXED_CLOSURE: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[++sp] = closure[arg];
                        break;
                    }
                    case OpCodes.LITERAL: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[++sp] = constPool[arg];
                        break;
                    }
                    case OpCodes.BOX_IT: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object[] box = {stack[sp - arg]};
                        stack[sp - arg] = box;
                        break;
                    }
                    case OpCodes.DROP: {
                        --sp;
                        break;
                    }
                    case OpCodes.NOT: {
                        stack[sp] = toBool(stack[sp]) ? FALSE : TRUE;
                        break;
                    }
                    case OpCodes.NEG: {
                        Object o = stack[sp];
                        if (o instanceof Integer) {
                            o = new Integer(-((Integer) o).intValue());
                        } else /* if o is float */ {
// CLASS DISPATCH
                        }
                        stack[sp] = o;
                        break;
                    }
                    case OpCodes.ADD: {
                        Object o2 = stack[sp];
                        --sp;
                        Object o = stack[sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            int result = ((Integer) o).intValue();
                            result += ((Integer) o2).intValue();
                            stack[sp] = new Integer(result);
                        } else {
// CLASS DISPATCH
                            stack[sp] = String.valueOf(o) + String.valueOf(o2);
                        }
                        break;
                    }
                    case OpCodes.SUB: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            stack[sp] = new Integer(((Integer) o1).intValue()
                                    - ((Integer) o2).intValue());
                        } else /* float */ {
// CLASS DISPATCH
                        }
                        break;
                    }
                    case OpCodes.SHIFT_RIGHT_ARITHMETIC: {
                        int result = toInt(stack[sp]);
                        result = toInt(stack[--sp]) >> result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case OpCodes.MUL: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            stack[sp] = new Integer(((Integer) o1).intValue()
                                    * ((Integer) o2).intValue());
                        } else {
// CLASS DISPATCH
                        }
                        break;
                    }
                    case OpCodes.DIV: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
// CLASS DISPATCH
//                        stack[sp] = fpDiv(toFp(o1), toFp(o2));
                        break;
                    }
                    case OpCodes.REM: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            stack[sp] = new Integer(((Integer) o1).intValue()
                                    % ((Integer) o2).intValue());
                        } else /* float */ {
// CLASS DISPATCH
                        }
                        break;
                    }
                    case OpCodes.NOT_EQUALS: {
                        Object o = stack[sp];
                        --sp;
                        stack[sp] = (o == null)
                                ? (stack[sp] == null ? FALSE : TRUE)
                                : (o.equals(stack[sp]) ? FALSE : TRUE);
                        break;
                    }
                    case OpCodes.EQUALS: {
                        Object o = stack[sp];
                        --sp;
                        stack[sp] = (o == null)
                                ? (stack[sp] == null ? TRUE : FALSE)
                                : (o.equals(stack[sp]) ? TRUE : FALSE);
                        break;
                    }
                    case OpCodes.PUT: {
                        Object val = stack[sp];
                        Object key = stack[--sp];
                        Object container = stack[--sp];

                        //CLASS DISPATCH replace below
                        if (container instanceof Stack) {
                            int pos = toInt(key);
                            Stack s = (Stack) container;
                            if (pos >= s.size()) {
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
                            ((Function) executionContext[EC_SETTER]).apply(stack, sp, 2);
                        }
                        break;
                    }
                    case OpCodes.SUBSCRIPT: {
                        Object key = stack[sp];
                        Object container = stack[--sp];
                        Object result = null;


                        // "Object"
                        //CLASS DISPATCH replace below
                        if (container instanceof Hashtable) {
                            result = ((Hashtable) container).get(key);
                            if (result == null) {
                                Object prototype = ((Hashtable) container).get("__proto__");
                                // repeat case OpCodes.SUBSCRIPT with prototype as container
                                if (prototype != null) {
                                    stack[sp] = prototype;
                                    sp += 1;
                                    pc -= 1;
                                    break;
                                }
                            }

                            // "Array"
                        } else if (container instanceof Stack) {
                            if (key instanceof Integer) {
                                int pos = ((Integer) key).intValue();
                                Stack s = (Stack) container;
                                result = 0 <= pos && pos < s.size()
                                        ? s.elementAt(pos)
                                        : null;
                            } else if ("length".equals(key)) {
                                result = new Integer(((Stack) container).size());
                            } else {
                                result = ((Hashtable) executionContext[EC_ARRAY_PROTOTYPE]).get(key);
                            }

                            // "String"
                        } else if (container instanceof String) {
                            if (key instanceof Integer) {
                                int pos = ((Integer) key).intValue();
                                String s = (String) container;
                                result = 0 <= pos && pos < s.length()
                                        ? s.substring(pos, pos + 1)
                                        : null;
                            } else if ("length".equals(key)) {
                                result = new Integer(((String) container).length());
                            } else {
                                result = ((Hashtable) executionContext[EC_STRING_PROTOTYPE]).get(key);
                            }

                            // Other builtin types, by calling userdefined default getter
                        } else {
                            result = ((Function) executionContext[EC_GETTER]).apply(stack, sp, 1);
                        }

                        // prototype property or element within (super-)prototype
                        if (result == null) {
                            if ("__proto__".equals(key)) {
                                if (container instanceof Stack) {
                                    result = (Hashtable) executionContext[EC_ARRAY_PROTOTYPE];
                                } else if (container instanceof String) {
                                    result = (Hashtable) executionContext[EC_STRING_PROTOTYPE];
                                } else {
                                    result = (Hashtable) executionContext[EC_OBJECT_PROTOTYPE];
                                }
                            } else {
                                result = ((Hashtable) executionContext[EC_OBJECT_PROTOTYPE]).get(key);
                                if (result == null) {
                                    result = UNDEFINED;
                                }
                            }
                        }
                        stack[sp] = result;
                        break;
                    }
                    case OpCodes.PUSH: {
                        Object o = stack[sp];
                        ((Stack) stack[--sp]).push(o);
                        break;
                    }
                    case OpCodes.POP: {
                        stack[sp] = ((Stack) stack[sp]).pop();
                        break;
                    }
                    case OpCodes.LESS: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            stack[sp] = ((Integer) o1).intValue()
                                    < ((Integer) o2).intValue() ? TRUE : FALSE;
                        } else {
// CLASS DISPATCH
                            stack[sp] = o1.toString().compareTo(o2.toString())
                                    < 0 ? TRUE : FALSE;
                        }
                        break;
                    }
                    case OpCodes.LESS_EQUALS: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            stack[sp] = ((Integer) o1).intValue()
                                    <= ((Integer) o2).intValue() ? TRUE : FALSE;
                        } else {
// CLASS DISPATCH
                            stack[sp] = o1.toString().compareTo(o2.toString())
                                    <= 0 ? TRUE : FALSE;
                        }
                        break;
                    }
                    case OpCodes.JUMP: {
                        pc += readShort(pc, code) + 2;
                        if (__DO_YIELD__) {
                            Thread.yield();
                        }
                        break;
                    }
                    case OpCodes.JUMP_IF_UNDEFINED: {
                        if (UNDEFINED != stack[sp]) {
                            pc += 2;
                        } else {
                            pc += readShort(pc, code) + 2;
                            if (__DO_YIELD__) {
                                Thread.yield();
                            }
                        }
                        --sp;
                        break;
                    }
                    case OpCodes.JUMP_IF_FALSE: {
                        if (toBool(stack[sp])) {
                            pc += 2;
                        } else {
                            pc += readShort(pc, code) + 2;
                            if (__DO_YIELD__) {
                                Thread.yield();
                            }
                        }
                        --sp;
                        break;
                    }
                    case OpCodes.JUMP_IF_TRUE: {
                        if (toBool(stack[sp])) {
                            pc += readShort(pc, code) + 2;
                        } else {
                            pc += 2;
                        }
                        --sp;
                        break;
                    }
                    case OpCodes.DUP: {
                        Object o = stack[sp];
                        stack[++sp] = o;
                        break;
                    }
                    case OpCodes.NEW_LIST: {
                        stack[++sp] = new Stack();
                        break;
                    }
                    case OpCodes.NEW_DICT: {
                        stack[++sp] = new Hashtable();
                        break;
                    }
                    case OpCodes.SET_THIS: {
                        thisPtr = stack[sp];
                        --sp;
                        break;
                    }
                    case OpCodes.THIS: {
                        stack[++sp] = thisPtr;
                        break;
                    }
                    case OpCodes.SWAP: {
                        Object t = stack[sp];
                        stack[sp] = stack[sp - 1];
                        stack[sp - 1] = t;
                        break;
                    }
                    case OpCodes.THROW: {
                        Object result = stack[sp];
                        if (exceptionHandler < 0) {
                            throw new ScriptException(result);
                        } else {
                            //System.out.println(stringify(stack));
                            sp = exceptionHandler;
                            exceptionHandler = ((Integer) stack[sp]).intValue();
                            pc = ((Integer) stack[--sp]).intValue();
                            code = (byte[]) stack[--sp];
                            constPool = (Object[]) stack[--sp];
                            executionContext = ((LightScript)constPool[0]).executionContext;
                            closure = (Object[]) stack[--sp];
                            stack[sp] = result;
                        }
                        break;
                    }
                    case OpCodes.TRY: {
                        stack[++sp] = closure;
                        stack[++sp] = constPool;
                        stack[++sp] = code;
                        stack[++sp] = new Integer(pc + readShort(pc, code) + 2);
                        stack[++sp] = new Integer(exceptionHandler);
                        exceptionHandler = sp;
                        pc += 2;
                        break;
                    }
                    case OpCodes.UNTRY: {
                        exceptionHandler = ((Integer) stack[sp]).intValue();
                        sp -= TRY_FRAME_SIZE;
                        break;
                    }
                    case OpCodes.NEW_ITER: {
                        stack[sp] = ((Function) executionContext[EC_NEW_ITER]).apply(stack, sp, 0);
                        break;
                    }
                    case OpCodes.NEXT: {
                        Function iter = (Function) stack[sp];
                        stack[++sp] = iter.apply(stack, sp, 0);
                        break;
                    }
                    case OpCodes.GLOBAL: {
                        stack[++sp] = oldGlobalObject;
                        break;
                    }
                    case OpCodes.DELETE: {
                        Object key = stack[sp];
                        Object container = stack[--sp];
                        //CLASS DISPATCH replace below
                        if (container instanceof Hashtable) {
                            ((Hashtable) container).remove(key);
                        } else if (container instanceof Stack && key instanceof Integer) {
                            ((Stack) container).setElementAt(UNDEFINED, ((Integer) key).intValue());
                        } else {
                            if (DEBUG_ENABLED) {
                                throw new Error("deleting non-deletable");
                            }
                        }
                        break;
                    }
                    case OpCodes.SHIFT_RIGHT: {
                        int result = toInt(stack[sp]);
                        result = toInt(stack[--sp]) >>> result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case OpCodes.SHIFT_LEFT: {
                        int result = toInt(stack[sp]);
                        result = toInt(stack[--sp]) << result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case OpCodes.BITWISE_OR: {
                        int result = toInt(stack[sp]);
                        result = toInt(stack[--sp]) | result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case OpCodes.BITWISE_XOR: {
                        int result = toInt(stack[sp]);
                        result = toInt(stack[--sp]) ^ result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case OpCodes.BITWISE_AND: {
                        int result = toInt(stack[sp]);
                        result = toInt(stack[--sp]) & result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case OpCodes.BITWISE_NOT: {
                        int result = ~toInt(stack[sp]);
                        stack[sp] = new Integer(result);
                        break;
                    }
                    default: {
                        if (DEBUG_ENABLED) {
                            throw new Error("Unknown opcode: " + code[pc]);
                        }
                    }
                }
            }
// if we debug, we want the real exception, with line number..
        } catch (Error e) {
            if (!DEBUG_ENABLED) {
                throw new ScriptException(e);
            } else {
                throw e;
            }
        }
    }
    //</editor-fold>
}
