package com.solsort.lightscript;

import java.util.Hashtable;
import java.util.Stack;

class Code implements LightScriptFunction {

    private static final boolean DEBUG_ENABLED = true;

    public Object apply(Object[] args, int argpos, int argcount)
            throws LightScriptException {
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
            throw new LightScriptException("Wrong number of arguments");
        }
    }
    int argc;
    byte[] code;
    Object[] constPool;
    Object[] closure;
    int maxDepth;

    Code(int argc, byte[] code, Object[] constPool, Object[] closure, int maxDepth) {
        this.argc = argc;
        this.code = code;
        this.constPool = constPool;
        this.closure = closure;
        this.maxDepth = maxDepth;
    }

    Code(Code cl) {
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
    static final int RET_FRAME_SIZE = 4;
    static final int TRY_FRAME_SIZE = 5;

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

        throw new Error("Not yet implemented");
    }

    private static boolean toBool(LightScript ls, Object stack[], int sp) throws LightScriptException  {
        Object o = stack[sp];
        if (o == LightScript.TRUE) {
            return true;
        }
        if (o == LightScript.FALSE || o == LightScript.NULL || o == LightScript.UNDEFINED) {
            return false;
        }
        return Code.unop(ls, stack, sp, "toBool") == LightScript.TRUE;
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

    private static Object unop(LightScript ls, Object stack[], int sp, String name) throws LightScriptException {
          Object o = ls.getTypeMethod(stack[sp].getClass(), name);
          if(o!= LightScript.UNDEFINED) {
              o = ((LightScriptFunction) o).apply(stack, sp, 0);
          }
          return o;

    }

    private static Object binop(LightScript ls, Object stack[], int sp, String name) throws LightScriptException {
        Object o = ls.getTypeMethod(stack[sp].getClass(), name);
        if (o != LightScript.UNDEFINED) {
            o = ((LightScriptFunction) o).apply(stack, sp, 1);
        }
        if (o == LightScript.UNDEFINED) {
            o = ls.getTypeMethod(stack[sp + 1].getClass(), name);
            if (o != LightScript.UNDEFINED) {
                o = ((LightScriptFunction) o).apply(stack, sp, 1);
            }
        }
        return o;
    }

    /**
     * evaluate some bytecode
     */
    public static Object execute(Code cl, Object[] stack, int argcount) throws LightScriptException {
        //if(!DEBUG_ENABLED) {
        try {
            //}
            int sp = argcount;

            //System.out.println(stringify(cl));
            int pc = -1;
            byte[] code = cl.code;
            Object[] constPool = cl.constPool;
            Object[] closure = cl.closure;
            LightScript ls = (LightScript) constPool[0];
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
                        ls = (LightScript) constPool[0];
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
                            ls = (LightScript) constPool[0];
                            closure = fn.closure;
                        } else if (o instanceof LightScriptFunction) {
                            try {
                                Object result = ((LightScriptFunction) o).apply(stack, sp - argc, argc);
                                sp -= argc + 1 + RET_FRAME_SIZE;
                                stack[sp] = result;
                            } catch (LightScriptException e) {
                                if (exceptionHandler < 0) {
                                    throw e;
                                } else {
                                    //System.out.println(stringify(stack));
                                    sp = exceptionHandler;
                                    exceptionHandler = ((Integer) stack[sp]).intValue();
                                    pc = ((Integer) stack[--sp]).intValue();
                                    code = (byte[]) stack[--sp];
                                    constPool = (Object[]) stack[--sp];
                                    ls = (LightScript) constPool[0];
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
                        stack[sp] = toBool(ls, stack, sp) ? LightScript.FALSE : LightScript.TRUE;
                        break;
                    }
                    case OpCodes.NEG: {
                        Object o = stack[sp];
                        if (o instanceof Integer) {
                            o = new Integer(-((Integer) o).intValue());
                        } else {
                            o = Code.unop(ls, stack, sp, "-");
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
                            o = new Integer(result);
                        } else {
                            o = Code.binop(ls, stack, sp, "+");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case OpCodes.SUB: {
                        Object o2 = stack[sp];
                        Object o = stack[--sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            o = new Integer(((Integer) o).intValue()
                                    - ((Integer) o2).intValue());
                        } else {
                            o = Code.binop(ls, stack, sp, "-");
                        }
                        stack[sp] = o;
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
                        Object o = stack[--sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            o = new Integer(((Integer) o).intValue()
                                    * ((Integer) o2).intValue());
                        } else {
                            o = Code.binop(ls, stack, sp, "*");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case OpCodes.DIV: {
                        --sp;
                        stack[sp] = Code.binop(ls, stack, sp, "/");
                        break;
                    }
                    case OpCodes.REM: {
                        Object o2 = stack[sp];
                        Object o = stack[--sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            o = new Integer(((Integer) o).intValue()
                                    % ((Integer) o2).intValue());
                        } else /* float */ {
                            o = Code.binop(ls, stack, sp, "%");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case OpCodes.NOT_EQUALS: {
                        Object o = stack[sp];
                        --sp;
                        stack[sp] = (o == null)
                                ? (stack[sp] == null ? LightScript.FALSE : LightScript.TRUE)
                                : (o.equals(stack[sp]) ? LightScript.FALSE : LightScript.TRUE);
                        // CLASS DISPATCH
                        break;
                    }
                    case OpCodes.EQUALS: {
                        Object o = stack[sp];
                        --sp;
                        stack[sp] = (o == null)
                                ? (stack[sp] == null ? LightScript.TRUE : LightScript.FALSE)
                                : (o.equals(stack[sp]) ? LightScript.TRUE : LightScript.FALSE);
                        // CLASS DISPATCH
                        break;
                    }
                    case OpCodes.PUT: {
                        sp -= 2;
                        ls.getSetter(stack[sp].getClass()).apply(stack, sp, 2);
                        break;
                    }
                    case OpCodes.SUBSCRIPT: {
                        sp -= 1;
                        stack[sp] = ls.getGetter(stack[sp].getClass()).apply(stack, sp, 1);
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
                            o1 = ((Integer) o1).intValue()
                                    < ((Integer) o2).intValue() ? LightScript.TRUE : LightScript.FALSE;
                        } else {
                            o1 = Code.binop(ls, stack, sp, "<");
                        }
                        stack[sp] = o1;
                        break;
                    }
                    case OpCodes.LESS_EQUAL: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            o1 = ((Integer) o1).intValue()
                                    <= ((Integer) o2).intValue() ? LightScript.TRUE : LightScript.FALSE;
                        } else {
                            o1 = Code.binop(ls, stack, sp, "<=");

                        }
                        stack[sp] = o1;
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
                        if (toBool(ls, stack, sp)) {
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
                        if (toBool(ls, stack, sp)) {
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
                            throw new LightScriptException(result);
                        } else {
                            //System.out.println(stringify(stack));
                            sp = exceptionHandler;
                            exceptionHandler = ((Integer) stack[sp]).intValue();
                            pc = ((Integer) stack[--sp]).intValue();
                            code = (byte[]) stack[--sp];
                            constPool = (Object[]) stack[--sp];
                            ls = (LightScript) constPool[0];
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
                        stack[sp] = Code.unop(ls, stack, sp, "__iter__");
                        break;
                    }
                    case OpCodes.NEXT: {
                        LightScriptFunction iter = (LightScriptFunction) stack[sp];
                        stack[++sp] = iter.apply(stack, sp, 0);
                        break;
                    }
                    case OpCodes.GLOBAL: {
                        stack[++sp] = ls;
                        break;
                    }
                    case OpCodes.DELETE: {
                        stack = Code.ensureSpace(stack, sp, 1);
                        stack[sp+1] = null;
                        --sp;
                        ls.getSetter(stack[sp].getClass()).apply(stack, sp, 2);
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
                throw new LightScriptException(e);
            } else {
                throw e;
            }
        }
    }
    //</editor-fold>
}

