package com.solsort.lightscript;

import java.util.Hashtable;
import java.util.Stack;

class Code implements Function {

    private static final boolean DEBUG_ENABLED = true;

    public Object apply(Object[] args, int argpos, int argcount)
            throws ScriptException {
        if (!DEBUG_ENABLED || argcount == argc) {
            Object stack[];
            if (argpos != 0) {
                stack = new Object[argcount + 1];
                for (int i = 0; i <= argcount; i++) {
                    stack[i] = args[argpos + i];
                }
            } else {
                stack = args;
            }
            return execute(this, stack, argcount);
        } else {
            throw new ScriptException("Wrong number of arguments");
        }
    }
    public int argc;
    public byte[] code;
    public Object[] constPool;
    public Object[] closure;
    public int maxDepth;

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
    /*`\section{Definitions, API, and utility functions}'*/

    /* If debugging is enabled, more tests are run during run-time,
     * and errors may be caught in a more readable way.
     * It also adds support for more readable printing of
     * id, etc.
     */
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

    public static int toInt(Object object) {
        if (object instanceof Integer) {
            return ((Integer) object).intValue();
        }
        // CLASS DISPATCH

        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static boolean toBool(Object o) {
        if (o == LightScript.TRUE) {
            return true;
        }
        if (o == LightScript.FALSE || o == LightScript.NULL || o == LightScript.UNDEFINED) {
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
            Object[] executionContext = ((LightScript) constPool[0]).executionContext;
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
                    System.out.println("pc:" + pc + " op:" + OpCodes.idName(code[pc])
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
                        executionContext = ((LightScript) constPool[0]).executionContext;
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
                                stack[sp - i] = LightScript.UNDEFINED;
                            }

                            stack[sp - argc - 1] = new Integer(pc);
                            thisPtr = stack[sp - argc];
                            pc = -1;
                            code = fn.code;
                            constPool = fn.constPool;
                            executionContext = ((LightScript) constPool[0]).executionContext;
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
                                    executionContext = ((LightScript) constPool[0]).executionContext;
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
                        stack[sp] = toBool(stack[sp]) ? LightScript.FALSE : LightScript.TRUE;
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
                                ? (stack[sp] == null ? LightScript.FALSE : LightScript.TRUE)
                                : (o.equals(stack[sp]) ? LightScript.FALSE : LightScript.TRUE);
                        break;
                    }
                    case OpCodes.EQUALS: {
                        Object o = stack[sp];
                        --sp;
                        stack[sp] = (o == null)
                                ? (stack[sp] == null ? LightScript.TRUE : LightScript.FALSE)
                                : (o.equals(stack[sp]) ? LightScript.TRUE : LightScript.FALSE);
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
                                    result = LightScript.UNDEFINED;
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
                                    < ((Integer) o2).intValue() ? LightScript.TRUE : LightScript.FALSE;
                        } else {
// CLASS DISPATCH
                            stack[sp] = o1.toString().compareTo(o2.toString())
                                    < 0 ? LightScript.TRUE : LightScript.FALSE;
                        }
                        break;
                    }
                    case OpCodes.LESS_EQUALS: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            stack[sp] = ((Integer) o1).intValue()
                                    <= ((Integer) o2).intValue() ? LightScript.TRUE : LightScript.FALSE;
                        } else {
// CLASS DISPATCH
                            stack[sp] = o1.toString().compareTo(o2.toString())
                                    <= 0 ? LightScript.TRUE : LightScript.FALSE;
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
                        if (LightScript.UNDEFINED != stack[sp]) {
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
                            executionContext = ((LightScript) constPool[0]).executionContext;
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
                        stack[++sp] = executionContext;
                        break;
                    }
                    case OpCodes.DELETE: {
                        Object key = stack[sp];
                        Object container = stack[--sp];
                        //CLASS DISPATCH replace below
                        if (container instanceof Hashtable) {
                            ((Hashtable) container).remove(key);
                        } else if (container instanceof Stack && key instanceof Integer) {
                            ((Stack) container).setElementAt(LightScript.UNDEFINED, ((Integer) key).intValue());
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

