
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

public abstract class Ys {
    // Opcodes

    private static final char OP_ENSURE_STACKSPACE = 0;
    private static final char OP_INC_SP = 1;
    private static final char OP_RETURN = 2;
    private static final char OP_SAVE_PC = 3;
    private static final char OP_CALL_FN = 4;
    private static final char OP_BUILD_FN = 5;
    private static final char OP_SET_BOXED = 6;
    private static final char OP_SET_LOCAL = 7;
    private static final char OP_SET_CLOSURE = 8;
    private static final char OP_GET_BOXED = 9;
    private static final char OP_GET_LOCAL = 10;
    private static final char OP_GET_CLOSURE = 11;
    // get a value from the closure without unboxing it
    private static final char OP_GET_BOXED_CLOSURE = 12;
    private static final char OP_GET_LITERAL = 13;
    // box a value
    private static final char OP_BOX_IT = 14;
    private static final char OP_LOG = 15;
    private static final char OP_DROP = 16;
    private static final char OP_PUSH_NIL = 17;
    private static final char OP_NOT = 18;
    private static final char OP_ADD = 19;
    private static final char OP_SUB = 20;
    private static final char OP_MUL = 21;
    private static final char OP_DIV = 22;
    private static final char OP_REM = 23;
    private static final char OP_IS_INT = 24;
    private static final char OP_IS_STR = 25;
    private static final char OP_IS_LIST = 26;
    private static final char OP_IS_DICT = 27;
    private static final char OP_IS_ITER = 28;
    private static final char OP_EQUAL = 29;
    private static final char OP_IS_EMPTY = 30;
    private static final char OP_PUT = 31;
    private static final char OP_GET = 32;
    private static final char OP_RAND = 33;
    private static final char OP_SIZE = 34;
    private static final char OP_LESS = 35;
    private static final char OP_LESSEQUAL = 36;
    private static final char OP_SUBSTR = 37;
    private static final char OP_RESIZE = 38;
    private static final char OP_PUSH = 39;
    private static final char OP_POP = 40;
    private static final char OP_KEYS = 41;
    private static final char OP_VALUES = 43;
    private static final char OP_NEXT = 44;
    private static final char OP_ASSERT = 45;
    private static final char OP_JUMP = 46;
    private static final char OP_JUMP_IF_TRUE = 47;
    private static final char OP_DUP = 48;
    private static final char OP_NEW_LIST = 49;
    private static final char OP_NEW_DICT = 50;
    private static final char OP_NEW_STRINGBUFFER = 51;
    private static final char OP_STR_APPEND = 52;
    private static final char OP_TO_STRING = 53;
    private static final char OP_NEW_COUNTER = 54;
    private static final char OP_SWAP = 55;

    // size of the return frame
    private static final char RET_FRAME_SIZE = 3;

    // Syntax tree
    private static final int AST_BUILTIN_FUNCTION = 0x100;
    private static final int AST_DO = 0;
    private static final int AST_SET = 1;
    private static final int AST_IF = AST_SET + 1;
    private static final int AST_AND = AST_IF + 1;
    private static final int AST_OR = AST_AND + 1;
    private static final int AST_REPEAT = AST_OR + 1;
    private static final int AST_FOREACH = AST_REPEAT + 1;
    private static final int AST_WHILE = AST_FOREACH + 1;
    private static final int AST_STRINGJOIN = AST_WHILE + 1;
    private static final int AST_LIST = AST_STRINGJOIN + 1;
    private static final int AST_DICT = AST_LIST + 1;
    // Opcode mask, to extract opcode from AST_FUNCTIION type
    private static final int MASK_OP = 0xFF;
    private static String[] fnNames = {"not", "+", "-", "*", "/", "%", "is-integer", "is-string", "is-list", "is-dictionary", "is-iterator", "equals", "is-empty", "put", "get", "random", "size", "<", "<=", "substring", "resize", "push", "pop", "keys", "values", "get-next", "log", "assert"};
    private static int[] fnArity = {1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 3, 2, 1, 1, 2, 2, 3, 2, 2, 1, 1, 1, 1, 1, 2};
    private static char[] fnTypes = {OP_NOT, OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_REM, OP_IS_INT, OP_IS_STR, OP_IS_LIST, OP_IS_DICT, OP_IS_ITER, OP_EQUAL, OP_IS_EMPTY, OP_PUT, OP_GET, OP_RAND, OP_SIZE, OP_LESS, OP_LESSEQUAL, OP_SUBSTR, OP_RESIZE, OP_PUSH, OP_POP, OP_KEYS, OP_VALUES, OP_NEXT, OP_LOG, OP_ASSERT};
    private static String[] builtinNames = {"set", "if", "and", "or", "repeat", "foreach", "while", "do", "stringjoin", "list", "dict"};
    private static int[] builtinTypes = {AST_SET, AST_IF, AST_AND, AST_OR, AST_REPEAT, AST_FOREACH, AST_WHILE, AST_DO, AST_STRINGJOIN, AST_LIST, AST_DICT};
    private static Stack builtins;
    

    static {
        builtins = new Stack();
        builtins.push("do");
        builtins.push("log");
        builtins.push("set");
        builtins.push("-");
    }
    public static final Boolean TRUE = new Boolean(true);
    private static Random rnd = new Random();

    private static int builtinId(String name) {
        for (int i = 0; i < fnNames.length; i++) {
            if (name.equals(fnNames[i])) {
                return fnTypes[i] | AST_BUILTIN_FUNCTION;
            }

        }
        for (int i = 0; i < builtinNames.length; i++) {
            if (name.equals(builtinNames[i])) {
                return builtinTypes[i];
            }

        }
        return -1;
    }

    private static int builtinArity(int id) {
        for (int i = 0; i < fnNames.length; i++) {
            if (fnTypes[i] == id) {
                return fnArity[i];
            }
        }
        return -1;
    }

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
                sb.append(code[i]);
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
            //System.out.println("pc:" + pc + " op:" + code[pc] + " sp:" + sp + " stack.length:" + stack.length);
            switch (code[pc]) {
                case OP_ENSURE_STACKSPACE: {
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
                case OP_INC_SP: {
                    sp += code[++pc];
                    break;
                }
                case OP_RETURN: {
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
                case OP_SAVE_PC: {
                    stack[++sp] = closure;
                    stack[++sp] = constPool;
                    stack[++sp] = code;
                    break;
                }
                case OP_CALL_FN: {
                    int argc = code[++pc];
                    Closure fn = (Closure) stack[sp - argc];
                    stack[sp - argc] = new Integer(pc);
                    pc = -1;
                    code = fn.code;
                    constPool = fn.constPool;
                    closure = fn.closure;
                    break;
                }
                case OP_BUILD_FN: {
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
                case OP_SET_BOXED: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    ((Object[]) stack[sp - arg])[0] = stack[sp];
                    break;
                }
                case OP_SET_LOCAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[sp - arg] = stack[sp];
                    break;
                }
                case OP_SET_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    ((Object[]) closure[arg])[0] = stack[sp];
                    break;
                }
                case OP_GET_BOXED: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object o = ((Object[]) stack[sp - arg])[0];
                    stack[++sp] = o;
                    break;
                }
                case OP_GET_LOCAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object o = stack[sp - arg];
                    stack[++sp] = o;
                    break;
                }
                case OP_GET_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = ((Object[]) closure[arg])[0];
                    break;
                }
                case OP_GET_BOXED_CLOSURE: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = closure[arg];
                    break;
                }
                case OP_GET_LITERAL: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    stack[++sp] = constPool[arg];
                    break;
                }
                case OP_BOX_IT: {
                    int arg = readShort(pc, code);
                    pc += 2;
                    Object[] box = {stack[sp - arg]};
                    stack[sp - arg] = box;
                    break;
                }
                case OP_LOG: {
                    System.out.println(stringify(stack[sp]));
                    break;
                }
                case OP_DROP: {
                    --sp;
                    break;
                }
                case OP_PUSH_NIL: {
                    stack[++sp] = null;
                    break;
                }
                case OP_NOT: {
                    stack[sp] = stack[sp] == null ? TRUE : null;
                    break;
                }
                case OP_ADD: {
                    int result = ((Integer) stack[sp]).intValue();
                    result += ((Integer) stack[--sp]).intValue();
                    stack[sp] = new Integer(result);
                    break;
                }
                case OP_SUB: {
                    int result = ((Integer) stack[sp]).intValue();
                    result = ((Integer) stack[--sp]).intValue() - result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case OP_MUL: {
                    int result = ((Integer) stack[sp]).intValue();
                    result *= ((Integer) stack[--sp]).intValue();
                    stack[sp] = new Integer(result);
                    break;
                }
                case OP_DIV: {
                    int result = ((Integer) stack[sp]).intValue();
                    result = ((Integer) stack[--sp]).intValue() / result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case OP_REM: {
                    int result = ((Integer) stack[sp]).intValue();
                    result = ((Integer) stack[--sp]).intValue() % result;
                    stack[sp] = new Integer(result);
                    break;
                }
                case OP_IS_INT: {
                    stack[sp] = stack[sp] instanceof Integer
                            ? TRUE : null;
                    break;
                }
                case OP_IS_STR: {
                    stack[sp] = stack[sp] instanceof String
                            ? TRUE : null;
                    break;
                }
                case OP_IS_LIST: {
                    stack[sp] = stack[sp] instanceof Stack
                            ? TRUE : null;
                    break;
                }
                case OP_IS_DICT: {
                    stack[sp] = stack[sp] instanceof Hashtable
                            ? TRUE : null;
                    break;
                }
                case OP_IS_ITER: {
                    stack[sp] = stack[sp] instanceof Enumeration
                            ? TRUE : null;
                    break;
                }
                case OP_EQUAL: {
                    Object o = stack[sp];
                    --sp;
                    stack[sp] = (o == null)
                            ? (stack[sp] == null ? TRUE : null)
                            : (o.equals(stack[sp]) ? TRUE : null);
                    break;
                }
                case OP_IS_EMPTY: {
                    Object o = stack[sp];
                    if (o instanceof Hashtable) {
                        stack[sp] = ((Hashtable) o).isEmpty() ? TRUE : null;
                    } else if (o instanceof Stack) {
                        stack[sp] = ((Stack) o).empty() ? TRUE : null;
                    } else if (o instanceof Enumeration) {
                        stack[sp] = ((Enumeration) o).hasMoreElements() ? null : TRUE;
                    } else {
                        stack[sp] = null;
                    }
                    break;
                }
                case OP_PUT: {
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
                case OP_GET: {
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
                case OP_RAND: {
                    Object o = stack[sp];
                    if (o instanceof Integer) {
                        stack[sp] = new Integer((rnd.nextInt() & 0x7fffffff) % ((Integer) o).intValue());
                    } else if (o instanceof Stack) {
                        Stack s = (Stack) o;
                        stack[sp] = s.elementAt((rnd.nextInt() & 0x7fffffff) % s.size());
                    } else {
                        stack[sp] = null;
                    }
                    break;
                }
                case OP_SIZE: {
                    Object o = stack[sp];
                    int size;
                    if (o instanceof Stack) {
                        size = ((Stack) o).size();
                    } else if (o instanceof String) {
                        size = ((String) o).length();
                    } else if (o instanceof Hashtable) {
                        size = ((Hashtable) o).size();
                    } else {
                        size = 0;
                    }
                    stack[sp] = new Integer(size);
                    break;
                }
                case OP_LESS: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = ((Integer) o1).intValue() < ((Integer) o2).intValue() ? TRUE : null;
                    } else {
                        stack[sp] = o1.toString().compareTo(o2.toString()) < 0 ? TRUE : null;
                    }
                    break;
                }
                case OP_LESSEQUAL: {
                    Object o2 = stack[sp];
                    Object o1 = stack[--sp];
                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        stack[sp] = ((Integer) o1).intValue() <= ((Integer) o2).intValue() ? TRUE : null;
                    } else {
                        stack[sp] = o1.toString().compareTo(o2.toString()) <= 0 ? TRUE : null;
                    }
                    break;
                }
                case OP_SUBSTR: {
                    int start = ((Integer) stack[sp]).intValue();
                    int end = ((Integer) stack[--sp]).intValue();
                    --sp;
                    stack[sp] = ((String) stack[sp]).substring(start, end);
                    break;
                }
                case OP_RESIZE: {
                    int newsize = ((Integer) stack[sp]).intValue();
                    ((Stack) stack[--sp]).setSize(newsize);
                    break;
                }
                case OP_PUSH: {
                    Object o = stack[sp];
                    ((Stack) stack[--sp]).push(o);
                    break;
                }
                case OP_POP: {
                    stack[sp] = ((Stack) stack[sp]).pop();
                    break;
                }
                case OP_KEYS: {
                    stack[sp] = ((Hashtable) stack[sp]).keys();
                    break;
                }
                case OP_VALUES: {
                    Object container = stack[sp];
                    if (container instanceof Stack) {
                        stack[sp] = ((Stack) container).elements();
                    } else if (container instanceof Hashtable) {
                        stack[sp] = ((Hashtable) container).elements();
                    } else {
                        stack[sp] = null;
                    }
                    break;
                }
                case OP_NEXT: {
                    Enumeration e = (Enumeration) stack[sp];
                    stack[sp] = e.hasMoreElements()
                            ? e.nextElement()
                            : null;
                    break;
                }
                case OP_ASSERT: {
                    if (stack[sp] == null) {
                        throw new Error("Assert error: " + stack[--sp].toString());
                    }
                    --sp;
                    break;
                }
                case OP_JUMP: {
                    pc += readShort(pc, code) + 2;
                    break;
                }
                case OP_JUMP_IF_TRUE: {
                    if (stack[sp] != null) {
                        pc += readShort(pc, code) + 2;
                    } else {
                        pc += 2;
                    }
                    --sp;
                    break;
                }
                case OP_DUP: {
                    Object o = stack[sp];
                    stack[++sp] = o;
                    break;
                }
                case OP_NEW_LIST: {
                    stack[++sp] = new Stack();
                    break;
                }
                case OP_NEW_DICT: {
                    stack[++sp] = new Hashtable();
                    break;
                }
                case OP_NEW_STRINGBUFFER: {
                    stack[++sp] = new StringBuffer();
                    break;
                }
                case OP_STR_APPEND: {
                    String s = stack[sp].toString();
                    ((StringBuffer) stack[--sp]).append(s);
                    break;
                }
                case OP_TO_STRING: {
                    stack[sp] = stack[sp].toString();
                    break;
                }
                case OP_NEW_COUNTER: {
                    stack[sp] = new Counter(((Integer) stack[sp]).intValue());
                    break;
                }
                case OP_SWAP: {
                    Object t = stack[sp - 1];
                    stack[sp - 1] = stack[sp];
                    stack[sp] = t;
                    break;
                }
            }
        }
    }

    public static class Counter implements Enumeration {

        private int count;

        public Counter(int i) {
            count = i;
        }

        public boolean hasMoreElements() {
            return count > 0;
        }

        public Object nextElement() {
            count--;
            return TRUE;
        }
    }

    private static class Function {

        private int argc;
        private Object[] body;
        private Stack locals;
        private Stack boxed;
        private Stack closure;
        private Stack constPool;
        private int maxDepth;
        private int depth;
        private StringBuffer code;

        private Function(Object[] list) {
            Enumeration e;
            body = new Object[list.length - 2];
            for (int i = 1; i < body.length; i++) {
                body[i] = list[i + 2];
            }
            body[0] = new Integer(AST_DO);
            argc = ((Object[]) list[1]).length;
            locals = new Stack();
            boxed = new Stack();
            closure = new Stack();

            findIds(locals, list[1]);
            findIds(locals, list[2]);

            findIds(closure, body);

            // local variables are not placed in the closure
            e = locals.elements();
            while (e.hasMoreElements()) {
                closure.removeElement(e.nextElement());
            }

            // closure variables are automatically boxed
            e = closure.elements();
            while (e.hasMoreElements()) {
                boxed.removeElement(e.nextElement());
            }

            compile();
        //System.out.println(this.toString());
        }

        public static Closure create(Object[] list) {
            Function f = new Function(list);
            return new Closure(f.argc, f.code, f.constPool, f.closure);
        }

        private void pushShort(int i) {
            code.append((char) ((i >> 8) & 0xff));
            code.append((char) (i & 0xff));
        }

        private void setShort(int pos, int i) {
            code.setCharAt(pos - 2, (char) ((i >> 8) & 0xff));
            code.setCharAt(pos - 1, (char) (i & 0xff));
        }

        private void compile() {
            constPool = new Stack();
            code = new StringBuffer();

            // make sure that there are sufficient stack space for the function
            code.append(OP_ENSURE_STACKSPACE);
            pushShort(0);
            int spacePos = code.length();

            // allocate space for local vars
            maxDepth = depth = locals.size();
            int framesize = depth - argc;
            while (framesize >= 127) {
                code.append(OP_INC_SP);
                code.append((char) 127);
                framesize -= 127;
            }
            if (framesize > 0) {
                code.append(OP_INC_SP);
                code.append((char) framesize);
            }

            // box boxed values in frame
            Enumeration e = boxed.elements();
            while (e.hasMoreElements()) {
                code.append(OP_BOX_IT);
                pushShort(depth - locals.indexOf(e.nextElement()) - 1);
            }

            compile(body);

            // emit return code, including current stack depth to drop
            code.append(OP_RETURN);
            pushShort(depth);

            // patch amount of stack space needed
            maxDepth -= argc;
            setShort(spacePos, maxDepth);
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

        private int constPoolId(Object o) {
            int pos = constPool.indexOf(o);
            if (pos < 0) {
                pos = constPool.size();
                constPool.push(o);
            }
            return pos;
        }

        private void compile(Object o) {
            if (o instanceof Object[]) {
                Object[] list = (Object[]) o;
                Object head = list[0];
                // builtin operator
                if (head instanceof Integer) {
                    int id = ((Integer) head).intValue();

                    if ((id & AST_BUILTIN_FUNCTION) != 0) {
                        char opcode = (char) (id & MASK_OP);
                        assertLength(list, builtinArity(opcode) + 1);
                        for (int i = 1; i < list.length; i++) {
                            compile(list[i]);
                        }
                        addDepth(2 - list.length);
                        code.append(opcode);
                    } else {
                        switch (id) {
                            case AST_DO: {
                                int i;
                                for (i = 1; i < list.length - 1; i++) {
                                    compile(list[i]);
                                    code.append(OP_DROP);
                                    addDepth(-1);
                                }
                                if (i < list.length) {
                                    compile(list[i]);
                                } else {
                                    code.append(OP_PUSH_NIL);
                                    addDepth(1);
                                }

                                break;
                            }
                            case AST_SET: {
                                assertLength(list, 3);
                                String name = (String) list[1];
                                compile(list[2]);
                                int pos = closure.indexOf(name);
                                if (pos >= 0) {
                                    code.append(OP_SET_CLOSURE);
                                    pushShort(pos);
                                } else {
                                    pos = locals.indexOf(name);
                                    if (boxed.contains(name)) {
                                        code.append(OP_SET_BOXED);
                                    } else {
                                        code.append(OP_SET_LOCAL);
                                    }
                                    pushShort(depth - pos - 1);
                                }
                                break;
                            }
                            case AST_IF: {
                                //    code for list[1]
                                //    jump_if_true -> label1
                                //    code for list[3]
                                //    jump -> label2
                                // label1:
                                //    code for list[2]
                                // label2:

                                assertLength(list, 4);

                                int pos0, pos1, len;
                                compile(list[1]);

                                code.append(OP_JUMP_IF_TRUE);
                                pushShort(0);
                                pos0 = code.length();
                                addDepth(-1);

                                compile(list[3]);

                                code.append(OP_JUMP);
                                pushShort(0);
                                pos1 = code.length();
                                len = pos1 - pos0;
                                setShort(pos0, len);

                                addDepth(-1);

                                compile(list[2]);
                                len = code.length() - pos1;
                                setShort(pos1, len);
                                break;
                            }
                            case AST_AND: {
                                assertLength(list, 3);
                                int pos0, pos1, len;

                                compile(list[1]);

                                code.append(OP_JUMP_IF_TRUE);
                                code.append((char) 0);
                                code.append((char) 0);
                                pos0 = code.length();
                                addDepth(-1);

                                code.append(OP_PUSH_NIL);

                                code.append(OP_JUMP);
                                code.append((char) 0);
                                code.append((char) 0);
                                pos1 = code.length();
                                len = pos1 - pos0;
                                setShort(pos0, len);
                                addDepth(-1);

                                compile(list[2]);
                                len = code.length() - pos1;
                                setShort(pos1, len);
                                break;
                            }
                            case AST_OR: {
                                assertLength(list, 3);
                                int pos0, pos1, len;

                                compile(list[1]);

                                code.append(OP_DUP);
                                addDepth(1);

                                code.append(OP_JUMP_IF_TRUE);
                                code.append((char) 0);
                                code.append((char) 0);
                                pos0 = code.length();
                                addDepth(1);

                                code.append(OP_DROP);
                                addDepth(-1);

                                compile(list[2]);

                                pos1 = code.length();
                                len = pos1 - pos0;
                                setShort(pos0, len);


                                break;
                            }
                            case AST_REPEAT: {
                                //   push nil
                                //   evaluate count
                                //   new Countdown
                                //   jump -> labelRepeat
                                // labelTop:
                                //   swap
                                //   drop
                                //   code for stmt1
                                //   ...
                                //   drop
                                //   code for stmtn
                                //   swap
                                // labelRepeat:
                                //   dup
                                //   get_next
                                //   jump if true -> labelTop
                                //   drop
                                int pos0, pos1, len;
                                code.append(OP_PUSH_NIL);
                                addDepth(1);
                                compile(list[1]);
                                code.append(OP_NEW_COUNTER);

                                code.append(OP_JUMP);
                                code.append((char) 0);
                                code.append((char) 0);
                                pos0 = code.length();

                                code.append(OP_SWAP);
                                for (int i = 2; i < list.length; i++) {
                                    code.append(OP_DROP);
                                    addDepth(-1);
                                    compile(list[i]);
                                }
                                code.append(OP_SWAP);


                                pos1 = code.length();
                                len = pos1 - pos0;
                                setShort(pos0, len);

                                code.append(OP_DUP);
                                addDepth(1);

                                code.append(OP_NEXT);

                                code.append(OP_JUMP_IF_TRUE);
                                len = pos0 - (code.length() + 2);
                                code.append((char) ((len >> 8) & 0xff));
                                code.append((char) (len & 0xff));
                                addDepth(-1);

                                code.append(OP_DROP);
                                addDepth(-1);

                                break;
                            }
                            case AST_FOREACH: {
                                //   push nil
                                //   get iterator
                                //   jump -> labelNext
                                // labelStmts:
                                //   swap
                                //   drop
                                //   code for stmt1
                                //   ...
                                //   drop
                                //   code for stmtn
                                //   swap
                                // labelNext:
                                //   dup
                                //   get-next
                                //   set counter-var
                                //   jump if true labelStmts
                                //   drop
                                int pos0, pos1, len;
                                code.append(OP_PUSH_NIL);
                                addDepth(1);

                                compile(list[2]);

                                code.append(OP_JUMP);
                                code.append((char) 0);
                                code.append((char) 0);
                                pos0 = code.length();

                                code.append(OP_SWAP);
                                for (int i = 3; i < list.length; i++) {
                                    code.append(OP_DROP);
                                    addDepth(-1);
                                    compile(list[i]);
                                }
                                code.append(OP_SWAP);

                                pos1 = code.length();
                                len = pos1 - pos0;
                                setShort(pos0, len);

                                code.append(OP_DUP);
                                addDepth(1);

                                code.append(OP_NEXT);

                                {
                                    String name = (String) list[1];
                                    int pos = closure.indexOf(name);
                                    if (pos >= 0) {
                                        code.append(OP_SET_CLOSURE);
                                        pushShort(pos);
                                    } else {
                                        pos = locals.indexOf(name);
                                        if (boxed.contains(name)) {
                                            code.append(OP_SET_BOXED);
                                        } else {
                                            code.append(OP_SET_LOCAL);
                                        }
                                        pushShort(depth - pos - 1);
                                    }
                                }

                                code.append(OP_JUMP_IF_TRUE);
                                len = pos0 - (code.length() + 2);
                                code.append((char) ((len >> 8) & 0xff));
                                code.append((char) (len & 0xff));
                                addDepth(-1);

                                code.append(OP_DROP);
                                addDepth(-1);


                                break;
                            }
                            case AST_WHILE: {
                                //   push nil
                                //   jump -> labelCond:
                                // labelBody:
                                //   drop
                                //   code for stmt1
                                //   ...
                                //   drop
                                //   code for stmtn
                                // labelCond:
                                //   code for condition
                                //   jump if true -> labelBody

                                int pos0, pos1, len;
                                code.append(OP_PUSH_NIL);
                                addDepth(1);

                                code.append(OP_JUMP);
                                code.append((char) 0);
                                code.append((char) 0);
                                pos0 = code.length();

                                for (int i = 2; i < list.length; i++) {
                                    code.append(OP_DROP);
                                    addDepth(-1);
                                    compile(list[i]);
                                }


                                pos1 = code.length();
                                len = pos1 - pos0;
                                setShort(pos0, len);

                                compile(list[1]);

                                code.append(OP_JUMP_IF_TRUE);
                                len = pos0 - (code.length() + 2);
                                code.append((char) ((len >> 8) & 0xff));
                                code.append((char) (len & 0xff));
                                addDepth(-1);


                                break;
                            }
                            case AST_STRINGJOIN: {
                                code.append(OP_NEW_STRINGBUFFER);
                                addDepth(1);

                                for (int i = 1; i < list.length; i++) {
                                    compile(list[i]);
                                    code.append(OP_STR_APPEND);
                                    addDepth(-1);
                                }
                                code.append(OP_TO_STRING);

                                break;
                            }
                            case AST_LIST: {
                                code.append(OP_NEW_LIST);
                                addDepth(1);

                                for (int i = 1; i < list.length; i++) {
                                    compile(list[i]);
                                    code.append(OP_PUSH);
                                    addDepth(-1);
                                }

                                break;
                            }
                            case AST_DICT: {
                                code.append(OP_NEW_DICT);
                                addDepth(1);

                                if (list.length % 2 == 0) {
                                    throw new Error("Unmatched key/value: " + stringify(list));
                                }
                                for (int i = 1; i < list.length; i++) {
                                    compile(list[i]);
                                    i++;
                                    compile(list[i]);
                                    code.append(OP_PUT);
                                    addDepth(-2);
                                }

                                break;
                            }
                        }
                    // function evaluation
                    }
                } else {
                    // save program counter
                    code.append(OP_SAVE_PC);
                    addDepth(RET_FRAME_SIZE);

                    // find function and evaluate parameters
                    for (int i = 0; i < list.length; i++) {
                        compile(list[i]);
                    }

                    // call the function
                    code.append(OP_CALL_FN);
                    if (list.length > 128) {
                        throw new Error("too many parameters");
                    }
                    code.append((char) (list.length - 1));
                    addDepth(1 - list.length - RET_FRAME_SIZE);
                }
            // Identifier
            } else if (o instanceof String) {
                String name = (String) o;
                int pos = closure.indexOf(name);
                if (pos >= 0) {
                    code.append(OP_GET_CLOSURE);
                    pushShort(pos);
                } else {
                    pos = locals.indexOf(name);
                    if (boxed.contains(name)) {
                        code.append(OP_GET_BOXED);
                    } else {
                        code.append(OP_GET_LOCAL);
                    }
                    pushShort(depth - pos - 1);
                }
                addDepth(1);

            // Literal
            } else if (o instanceof Literal) {
                code.append(OP_GET_LITERAL);
                pushShort(constPoolId(((Literal) o).value));
                addDepth(1);

            // Function creation
            } else if (o instanceof Closure) {
                Object[] vars = ((Closure) o).closure;
                for (int i = 0; i < vars.length; i++) {
                    String name = (String) vars[i];
                    if (boxed.contains(name)) {
                        code.append(OP_GET_LOCAL);
                        pushShort(depth - locals.indexOf(name) - 1);
                    } else {
                        code.append(OP_GET_BOXED_CLOSURE);
                        pushShort(closure.indexOf(name));
                    }
                    addDepth(1);
                }
                code.append(OP_GET_LITERAL);
                pushShort(constPoolId(o));
                addDepth(1);
                code.append(OP_BUILD_FN);
                pushShort(vars.length);
                addDepth(-vars.length);

            // Should not happen
            } else {
                throw new Error("wrong kind of node:" + o.toString());
            }
        }

        private void findIds(Stack s, Object o) {
            if (o instanceof Object[]) {
                Object[] os = (Object[]) o;
                for (int i = 0; i < os.length; i++) {
                    findIds(s, os[i]);
                }
            } else if (o instanceof String) {
                if (!s.contains(o)) {
                    s.push(o);
                }
            } else if (o instanceof Closure) {
                Object[] closure_vars = ((Closure) o).closure;
                for (int i = 0; i < closure_vars.length; i++) {
                    String name = (String) closure_vars[i];
                    if (!s.contains(name)) {
                        s.push(name);
                    }
                    if (!boxed.contains(name)) {
                        boxed.push(name);
                    }
                }
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("\n\tFunction( argc:" + argc + ", locals:" + locals.toString() + ", boxed:" + boxed.toString() + ", closure:" + closure.toString() + ", body:" + stringify(body) + ", code:");
            for (int i = 0; i < code.length(); i++) {
                sb.append((int) code.charAt(i));
                sb.append(" ");
            }
            sb.append(" )");
            return sb.toString();
        }
    }

    private static class Literal {

        public Object value;

        public Literal(Object value) {
            this.value = value;
        }
    }

    /////////////////////////////////////
    // Factories and constants used by the parser
    ////
    private static final Object[] emptylist = new Object[0];

    private static Object createId(String name) {
        int id = builtinId(name);
        if (id != -1) {
            return new Integer(id);
        } else {
            return name;
        }
    }

    private static Object create(Object[] list) {
        if (list.length > 0) {
            Object fn = list[0];
            if (fn.equals("function")) {
                return Function.create(list);
            }
        }
        return list;
    }

    /////////////////////////////////////
    // The parser
    ////
    public static Object readExpression(InputStream is) throws IOException {
        Stack stack = new Stack();
        int c = is.read();
        do {
            // end of file
            if (c == -1) {
                return null;

            // end of list
            } else if (c == ']') {
                Object result[];
                c = is.read();
                // find out how much of the stack
                // is a part of the terminated list.
                // null indicates a "["
                int pos = stack.search(null);
                // ] with no [ begun
                if (pos == -1) {
                    result = emptylist;
                } else {
                    // stack search includes the null, which we want to skip
                    pos--;
                    // move the elements from the stack
                    result = new Object[pos];
                    while (pos > 0) {
                        pos--;
                        result[pos] = stack.pop();
                    }
                    // pop the null
                    stack.pop();
                }
                // create the list obj
                stack.push(create(result));


            // Whitespace
            } else if (c <= ' ') {
                c = is.read();

            // Comment
            } else if (c == ';') {
                do {
                    c = is.read();
                } while (c > '\n');

            // List
            } else if (c == '[') {
                // null is a marker of "["
                stack.push(null);
                c = is.read();

            // Number
            } else if ('0' <= c && c <= '9') {
                int i = 0;
                do {
                    i = i * 10 + c - '0';
                    c = is.read();
                } while ('0' <= c && c <= '9');
                stack.push(new Literal(new Integer(i)));

            // String
            } else if (c == '"') { // (comment ends '"' when prettyprinting)

                StringBuffer sb = new StringBuffer();
                c = is.read();
                while (c != '"' && c != -1) { // (comment ends '"' when prettyprinting)

                    if (c == '\\') {
                        c = is.read();
                    }
                    sb.append((char) c);
                    c = is.read();
                }
                c = is.read();
                stack.push(new Literal(sb.toString()));

            // Identifier
            } else {
                StringBuffer sb = new StringBuffer();
                while (c > ' ' && c != '[' && c != ']') {
                    sb.append((char) c);
                    c = is.read();
                }
                stack.push(createId(sb.toString()));
            }
        } while (stack.empty() || stack.size() > 1 || stack.elementAt(0) == null);
        return stack.pop();
    }

    ///////////////////////////////////////////
    // Utility for generating strings
    ////
    public static String stringify(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof Literal) {
            return ((Literal) o).value.toString();
        } else if (o instanceof Object[]) {
            StringBuffer sb = new StringBuffer();
            Object[] os = (Object[]) o;
            sb.append("[");
            for (int i = 0; i < os.length; i++) {
                sb.append(" " + stringify(os[i]));
            }
            sb.append(" ]");
            return sb.toString();
        } else {
            return o.toString();
        }
    }
}
