package com.solsort.mobile;

import java.util.Stack;

final class LightScriptCode implements LightScriptFunction {

    public Object apply(Object[] args, int argpos, int argcount)
            throws LightScriptException {
        if (!LightScript.DEBUG_ENABLED || argcount == argc) {
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

    LightScriptCode(int argc, byte[] code, Object[] constPool, Object[] closure, int maxDepth) {
        this.argc = argc;
        this.code = code;
        this.constPool = constPool;
        this.closure = closure;
        this.maxDepth = maxDepth;
    }

    LightScriptCode(LightScriptCode cl) {
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

    private static int toInt(LightScript ls, Object[] stack, int sp) throws LightScriptException {
        if (stack[sp] instanceof Integer) {
            return ((Integer) stack[sp]).intValue();
        } else {
            return ((Integer)LightScriptCode.unop(ls, stack, sp, "toInt")).intValue();
        }
    }

    private static boolean toBool(LightScript ls, Object stack[], int sp) throws LightScriptException  {
        Object o = stack[sp];
        if (o == LightScript.TRUE) {
            return true;
        }
        if (o == LightScript.FALSE || o == LightScript.NULL || o == LightScript.UNDEFINED) {
            return false;
        }
        return LightScriptCode.unop(ls, stack, sp, "toBool") == LightScript.TRUE;
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
          Object o = ls.getMethod(stack[sp].getClass(), name);
          if(o!= LightScript.UNDEFINED) {
              o = ((LightScriptFunction) o).apply(stack, sp, 0);
          }
          return o;

    }

    private static Object binop(LightScript ls, Object stack[], int sp, String name) throws LightScriptException {
        Object o = ls.getMethod(stack[sp].getClass(), name);
        if (o != LightScript.UNDEFINED) {
            o = ((LightScriptFunction) o).apply(stack, sp, 1);
        }
        if (o == LightScript.UNDEFINED) {
            o = ls.getMethod(stack[sp + 1].getClass(), name);
            if (o != LightScript.UNDEFINED) {
                o = ((LightScriptFunction) o).apply(stack, sp, 1);
            }
        }
        return o;
    }

    /**
     * evaluate some bytecode
     */
    private static Object execute(LightScriptCode cl, Object[] stack, int argcount) throws LightScriptException {
        //if(!LightScript.DEBUG_ENABLED) {
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
                    System.out.println("pc:" + pc + " op:" + LightScriptOpCodes.idName(code[pc])
                            + " sp:" + sp + " stack.length:" + stack.length
                            + " int:" + readShort(pc, code));
                }
                switch (code[pc]) {
                    case LightScriptOpCodes.INC_SP: {
                        sp += code[++pc];
                        break;
                    }
                    case LightScriptOpCodes.RETURN: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object result = stack[sp];
                        sp -= arg;
                        if (sp == 0) {
                            return result;
                        }
                        if (LightScript.DEBUG_ENABLED) {
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
                    case LightScriptOpCodes.SAVE_PC: {
                        stack[++sp] = thisPtr;
                        stack[++sp] = closure;
                        stack[++sp] = constPool;
                        stack[++sp] = code;
                        break;
                    }
                    case LightScriptOpCodes.CALL_FN: {
                        int argc = code[++pc];
                        Object o = stack[sp - argc - 1];
                        if (o instanceof LightScriptCode) {
                            LightScriptCode fn = (LightScriptCode) o;

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
                            if (LightScript.DEBUG_ENABLED) {
                                throw new Error("Unknown function:" + o);
                            }
                        }
                        break;
                    }
                    case LightScriptOpCodes.BUILD_FN: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        LightScriptCode fn = new LightScriptCode((LightScriptCode) stack[sp]);
                        Object[] clos = new Object[arg];
                        for (int i = arg - 1; i >= 0; i--) {
                            --sp;
                            clos[i] = stack[sp];
                        }
                        fn.closure = clos;
                        stack[sp] = fn;
                        break;
                    }
                    case LightScriptOpCodes.SET_BOXED: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        ((Object[]) stack[sp - arg])[0] = stack[sp];
                        break;
                    }
                    case LightScriptOpCodes.SET_LOCAL: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[sp - arg] = stack[sp];
                        break;
                    }
                    case LightScriptOpCodes.SET_CLOSURE: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        ((Object[]) closure[arg])[0] = stack[sp];
                        break;
                    }
                    case LightScriptOpCodes.GET_BOXED: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object o = ((Object[]) stack[sp - arg])[0];
                        stack[++sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.GET_LOCAL: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object o = stack[sp - arg];
                        stack[++sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.GET_CLOSURE: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[++sp] = ((Object[]) closure[arg])[0];
                        break;
                    }
                    case LightScriptOpCodes.GET_BOXED_CLOSURE: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[++sp] = closure[arg];
                        break;
                    }
                    case LightScriptOpCodes.LITERAL: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        stack[++sp] = constPool[arg];
                        break;
                    }
                    case LightScriptOpCodes.BOX_IT: {
                        int arg = readShort(pc, code);
                        pc += 2;
                        Object[] box = {stack[sp - arg]};
                        stack[sp - arg] = box;
                        break;
                    }
                    case LightScriptOpCodes.DROP: {
                        --sp;
                        break;
                    }
                    case LightScriptOpCodes.NOT: {
                        stack[sp] = toBool(ls, stack, sp) ? LightScript.FALSE : LightScript.TRUE;
                        break;
                    }
                    case LightScriptOpCodes.NEG: {
                        Object o = stack[sp];
                        if (o instanceof Integer) {
                            o = new Integer(-((Integer) o).intValue());
                        } else {
                            o = LightScriptCode.unop(ls, stack, sp, "-");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.ADD: {
                        Object o2 = stack[sp];
                        --sp;
                        Object o = stack[sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            int result = ((Integer) o).intValue();
                            result += ((Integer) o2).intValue();
                            o = new Integer(result);
                        } else {
                            o = LightScriptCode.binop(ls, stack, sp, "+");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.SUB: {
                        Object o2 = stack[sp];
                        Object o = stack[--sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            o = new Integer(((Integer) o).intValue()
                                    - ((Integer) o2).intValue());
                        } else {
                            o = LightScriptCode.binop(ls, stack, sp, "-");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.SHIFT_RIGHT_ARITHMETIC: {
                        int result = toInt(ls, stack, sp);
                        result = toInt(ls, stack, --sp) >> result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case LightScriptOpCodes.MUL: {
                        Object o2 = stack[sp];
                        Object o = stack[--sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            o = new Integer(((Integer) o).intValue()
                                    * ((Integer) o2).intValue());
                        } else {
                            o = LightScriptCode.binop(ls, stack, sp, "*");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.DIV: {
                        --sp;
                        stack[sp] = LightScriptCode.binop(ls, stack, sp, "/");
                        break;
                    }
                    case LightScriptOpCodes.REM: {
                        Object o2 = stack[sp];
                        Object o = stack[--sp];
                        if (o instanceof Integer && o2 instanceof Integer) {
                            o = new Integer(((Integer) o).intValue()
                                    % ((Integer) o2).intValue());
                        } else /* float */ {
                            o = LightScriptCode.binop(ls, stack, sp, "%");
                        }
                        stack[sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.NOT_EQUALS: {
                        Object o = stack[sp];
                        --sp;
                        stack[sp] = (o == null)
                                ? (stack[sp] == null ? LightScript.FALSE : LightScript.TRUE)
                                : (o.equals(stack[sp]) ? LightScript.FALSE : LightScript.TRUE);
                        // CLASS DISPATCH
                        break;
                    }
                    case LightScriptOpCodes.EQUALS: {
                        Object o = stack[sp];
                        --sp;
                        stack[sp] = (o == null)
                                ? (stack[sp] == null ? LightScript.TRUE : LightScript.FALSE)
                                : (o.equals(stack[sp]) ? LightScript.TRUE : LightScript.FALSE);
                        // CLASS DISPATCH
                        break;
                    }
                    case LightScriptOpCodes.PUT: {
                        sp -= 2;
                        ls.getSetter(stack[sp].getClass()).apply(stack, sp, 2);
                        break;
                    }
                    case LightScriptOpCodes.SUBSCRIPT: {
                        sp -= 1;
                        stack[sp] = ls.getGetter(stack[sp].getClass()).apply(stack, sp, 1);
                        break;
                    }
                    case LightScriptOpCodes.PUSH: {
                        Object o = stack[sp];
                        ((Stack) stack[--sp]).push(o);
                        break;
                    }
                    case LightScriptOpCodes.LESS: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            o1 = ((Integer) o1).intValue()
                                    < ((Integer) o2).intValue() ? LightScript.TRUE : LightScript.FALSE;
                        } else {
                            o1 = LightScriptCode.binop(ls, stack, sp, "<");
                        }
                        stack[sp] = o1;
                        break;
                    }
                    case LightScriptOpCodes.LESS_EQUAL: {
                        Object o2 = stack[sp];
                        Object o1 = stack[--sp];
                        if (o1 instanceof Integer && o2 instanceof Integer) {
                            o1 = ((Integer) o1).intValue()
                                    <= ((Integer) o2).intValue() ? LightScript.TRUE : LightScript.FALSE;
                        } else {
                            o1 = LightScriptCode.binop(ls, stack, sp, "<=");

                        }
                        stack[sp] = o1;
                        break;
                    }
                    case LightScriptOpCodes.JUMP: {
                        pc += readShort(pc, code) + 2;
                        if (__DO_YIELD__) {
                            Thread.yield();
                        }
                        break;
                    }
                    case LightScriptOpCodes.JUMP_IF_UNDEFINED: {
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
                    case LightScriptOpCodes.JUMP_IF_FALSE: {
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
                    case LightScriptOpCodes.JUMP_IF_TRUE: {
                        if (toBool(ls, stack, sp)) {
                            pc += readShort(pc, code) + 2;
                        } else {
                            pc += 2;
                        }
                        --sp;
                        break;
                    }
                    case LightScriptOpCodes.DUP: {
                        Object o = stack[sp];
                        stack[++sp] = o;
                        break;
                    }
                    case LightScriptOpCodes.NEW_LIST: {
                        stack[++sp] = ((LightScriptFunction)ls.newArrayFunctionBoxed[0]).apply(stack, sp, -1);
                        break;
                    }
                    case LightScriptOpCodes.NEW_DICT: {
                        stack[++sp] = ((LightScriptFunction)ls.newObjectFunctionBoxed[0]).apply(stack, sp, -1);
                        break;
                    }
                    case LightScriptOpCodes.SET_THIS: {
                        thisPtr = stack[sp];
                        --sp;
                        break;
                    }
                    case LightScriptOpCodes.THIS: {
                        stack[++sp] = thisPtr;
                        break;
                    }
                    case LightScriptOpCodes.SWAP: {
                        Object t = stack[sp];
                        stack[sp] = stack[sp - 1];
                        stack[sp - 1] = t;
                        break;
                    }
                    case LightScriptOpCodes.THROW: {
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
                    case LightScriptOpCodes.TRY: {
                        stack[++sp] = closure;
                        stack[++sp] = constPool;
                        stack[++sp] = code;
                        stack[++sp] = new Integer(pc + readShort(pc, code) + 2);
                        stack[++sp] = new Integer(exceptionHandler);
                        exceptionHandler = sp;
                        pc += 2;
                        break;
                    }
                    case LightScriptOpCodes.UNTRY: {
                        exceptionHandler = ((Integer) stack[sp]).intValue();
                        sp -= TRY_FRAME_SIZE;
                        break;
                    }
                    case LightScriptOpCodes.NEW_ITER: {
                        stack[sp] = LightScriptCode.unop(ls, stack, sp, "__iter__");
                        break;
                    }
                    case LightScriptOpCodes.NEXT: {
                        LightScriptFunction iter = (LightScriptFunction) stack[sp];
                        stack[++sp] = iter.apply(stack, sp, 0);
                        break;
                    }
                    case LightScriptOpCodes.GLOBAL: {
                        stack[++sp] = ls;
                        break;
                    }
                    case LightScriptOpCodes.DELETE: {
                        stack = LightScriptCode.ensureSpace(stack, sp, 1);
                        stack[sp+1] = null;
                        --sp;
                        ls.getSetter(stack[sp].getClass()).apply(stack, sp, 2);
                        break;
                    }
                    case LightScriptOpCodes.SHIFT_RIGHT: {
                        int result = toInt(ls, stack, sp);
                        result = toInt(ls, stack, --sp) >>> result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case LightScriptOpCodes.SHIFT_LEFT: {
                        int result = toInt(ls, stack, sp);
                        result = toInt(ls, stack, --sp) << result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case LightScriptOpCodes.BITWISE_OR: {
                        int result = toInt(ls, stack, sp);
                        result = toInt(ls, stack, --sp) | result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case LightScriptOpCodes.BITWISE_XOR: {
                        int result = toInt(ls, stack, sp);
                        result = toInt(ls, stack, --sp) ^ result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case LightScriptOpCodes.BITWISE_AND: {
                        int result = toInt(ls, stack, sp);
                        result = toInt(ls, stack, --sp) & result;
                        stack[sp] = new Integer(result);
                        break;
                    }
                    case LightScriptOpCodes.BITWISE_NOT: {
                        int result = ~toInt(ls, stack, sp);
                        stack[sp] = new Integer(result);
                        break;
                    }
                    default: {
                        if (LightScript.DEBUG_ENABLED) {
                            throw new Error("Unknown opcode: " + code[pc]);
                        }
                    }
                }
            }
        } catch (Error e) {
            throw new LightScriptException(e);
        }
    }
    //</editor-fold>
}

