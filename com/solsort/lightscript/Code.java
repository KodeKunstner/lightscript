package com.solsort.lightscript;

import java.util.Stack;

final class Code implements Function {

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

    /** Function that maps from ID to a string representation of the ID,
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

    private static int toInt(LightScript ls, Object[] stack, int sp) throws LightScriptException {
        if (stack[sp] instanceof Integer) {
            return ((Integer) stack[sp]).intValue();
        } else {
            return ((Integer)Code.unop(ls, stack, sp, "toInt")).intValue();
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
        Object o = ls.getMethod(stack[sp].getClass(), name);
        if (o!= LightScript.UNDEFINED) {
            o = ((Function) o).apply(stack, sp, 0);
        }
        return o;

    }

    private static Object binop(LightScript ls, Object stack[], int sp, String name) throws LightScriptException {
        Object o = ls.getMethod(stack[sp].getClass(), name);
        if (o != LightScript.UNDEFINED) {
            o = ((Function) o).apply(stack, sp, 1);
        }
        if (o == LightScript.UNDEFINED) {
            o = ls.getMethod(stack[sp + 1].getClass(), name);
            if (o != LightScript.UNDEFINED) {
                o = ((Function) o).apply(stack, sp, 1);
            }
        }
        return o;
    }

    /**
     * evaluate some bytecode
     */
    private static Object execute(Code cl, Object[] stack, int argcount) throws LightScriptException {
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
                    System.out.println("pc:" + pc + " op:" + Code.idName(code[pc])
                                       + " sp:" + sp + " stack.length:" + stack.length
                                       + " int:" + readShort(pc, code));
                }
                switch (code[pc]) {
                case Code.INC_SP: {
                    sp += code[++pc];
                    break;
                }
                case Code.RETURN: {
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
                case Code.SAVE_PC: {
                    stack[++sp] = thisPtr;
                    stack[++sp] = closure;
                    stack[++sp] = constPool;
                    stack[++sp] = code;
                    break;
                }
                case Code.CALL_FN: {
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
                    } else if (o instanceof Function) {
                        try {
                            Object result = ((Function) o).apply(stack, sp - argc, argc);
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
                case Code.BUILD_FN: {
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
                case Code.SET_BOXED: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    ((Object[]) stack[sp - arg])[0] = stack[sp];
                    break;
                }
                case Code.SET_LOCAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[sp - arg] = stack[sp];
                    break;
                }
                case Code.SET_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    ((Object[]) closure[arg])[0] = stack[sp];
                    break;
                }
                case Code.GET_BOXED: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object o = ((Object[]) stack[sp - arg])[0];
                    stack[++sp] = o;
                    break;
                }
                case Code.GET_LOCAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object o = stack[sp - arg];
                    stack[++sp] = o;
                    break;
                }
                case Code.GET_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = ((Object[]) closure[arg])[0];
                    break;
                }
                case Code.GET_BOXED_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = closure[arg];
                    break;
                }
                case Code.LITERAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = constPool[arg];
                    break;
                }
                case Code.BOX_IT: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object[] box = {stack[sp - arg]};
                    stack[sp - arg] = box;
                    break;
                }
                case Code.DROP: {
                    --sp;
                    break;
                }
                case Code.NOT: {
                    stack[sp] = toBool(ls, stack, sp) ? LightScript.FALSE : LightScript.TRUE;
                    break;
                }
                case Code.NEG: {
                    Object o = stack[sp];
                    if (o instanceof Integer) {
                        o = new Integer(-((Integer) o).intValue());
                    } else {
                        o = Code.unop(ls, stack, sp, "-");
                    }
                    stack[sp] = o;
                    break;
                }
                case Code.ADD: {
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
                case Code.SUB: {
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
                case Code.SHIFT_RIGHT_ARITHMETIC: {
                    int result = toInt(ls, stack, sp);
                    result = toInt(ls, stack, --sp) >> result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case Code.MUL: {
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
                case Code.DIV: {
                    --sp;
                    stack[sp] = Code.binop(ls, stack, sp, "/");
                    break;
                }
                case Code.REM: {
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
                case Code.NOT_EQUALS: {
                    Object o = stack[sp];
                    --sp;
                    stack[sp] = (o == null)
                                ? (stack[sp] == null ? LightScript.FALSE : LightScript.TRUE)
                                : (o.equals(stack[sp]) ? LightScript.FALSE : LightScript.TRUE);
                    // CLASS DISPATCH
                    break;
                }
                case Code.EQUALS: {
                    Object o = stack[sp];
                    --sp;
                    stack[sp] = (o == null)
                                ? (stack[sp] == null ? LightScript.TRUE : LightScript.FALSE)
                                : (o.equals(stack[sp]) ? LightScript.TRUE : LightScript.FALSE);
                    // CLASS DISPATCH
                    break;
                }
                case Code.PUT: {
                    sp -= 2;
                    ls.getSetter(stack[sp].getClass()).apply(stack, sp, 2);
                    break;
                }
                case Code.SUBSCRIPT: {
                    sp -= 1;
                    stack[sp] = ls.getGetter(stack[sp].getClass()).apply(stack, sp, 1);
                    break;
                }
                case Code.PUSH: {
                    Object o = stack[sp];
                    ((Stack) stack[--sp]).push(o);
                    break;
                }
                case Code.LESS: {
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
                case Code.LESS_EQUAL: {
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
                case Code.JUMP: {
                    pc += readShort(pc, code) + 2;
                    if (__DO_YIELD__) {
                        Thread.yield();
                    }
                    break;
                }
                case Code.JUMP_IF_UNDEFINED: {
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
                case Code.JUMP_IF_FALSE: {
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
                case Code.JUMP_IF_TRUE: {
                    if (toBool(ls, stack, sp)) {
                        pc += readShort(pc, code) + 2;
                    } else {
                        pc += 2;
                    }
                    --sp;
                    break;
                }
                case Code.DUP: {
                    Object o = stack[sp];
                    stack[++sp] = o;
                    break;
                }
                case Code.NEW_LIST: {
                    stack[++sp] = ((Function)ls.newArrayFunctionBoxed[0]).apply(stack, sp, -1);
                    break;
                }
                case Code.NEW_DICT: {
                    stack[++sp] = ((Function)ls.newObjectFunctionBoxed[0]).apply(stack, sp, -1);
                    break;
                }
                case Code.SET_THIS: {
                    thisPtr = stack[sp];
                    --sp;
                    break;
                }
                case Code.THIS: {
                    stack[++sp] = thisPtr;
                    break;
                }
                case Code.SWAP: {
                    Object t = stack[sp];
                    stack[sp] = stack[sp - 1];
                    stack[sp - 1] = t;
                    break;
                }
                case Code.THROW: {
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
                case Code.TRY: {
                    stack[++sp] = closure;
                    stack[++sp] = constPool;
                    stack[++sp] = code;
                    stack[++sp] = new Integer(pc + readShort(pc, code) + 2);
                    stack[++sp] = new Integer(exceptionHandler);
                    exceptionHandler = sp;
                    pc += 2;
                    break;
                }
                case Code.UNTRY: {
                    exceptionHandler = ((Integer) stack[sp]).intValue();
                    sp -= TRY_FRAME_SIZE;
                    break;
                }
                case Code.NEW_ITER: {
                    stack[sp] = Code.unop(ls, stack, sp, "__iter__");
                    break;
                }
                case Code.NEXT: {
                    Function iter = (Function) stack[sp];
                    stack[++sp] = iter.apply(stack, sp, 0);
                    break;
                }
                case Code.GLOBAL: {
                    stack[++sp] = ls;
                    break;
                }
                case Code.DELETE: {
                    stack = Code.ensureSpace(stack, sp, 1);
                    stack[sp+1] = null;
                    --sp;
                    ls.getSetter(stack[sp].getClass()).apply(stack, sp, 2);
                    break;
                }
                case Code.SHIFT_RIGHT: {
                    int result = toInt(ls, stack, sp);
                    result = toInt(ls, stack, --sp) >>> result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case Code.SHIFT_LEFT: {
                    int result = toInt(ls, stack, sp);
                    result = toInt(ls, stack, --sp) << result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case Code.BITWISE_OR: {
                    int result = toInt(ls, stack, sp);
                    result = toInt(ls, stack, --sp) | result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case Code.BITWISE_XOR: {
                    int result = toInt(ls, stack, sp);
                    result = toInt(ls, stack, --sp) ^ result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case Code.BITWISE_AND: {
                    int result = toInt(ls, stack, sp);
                    result = toInt(ls, stack, --sp) & result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case Code.BITWISE_NOT: {
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

