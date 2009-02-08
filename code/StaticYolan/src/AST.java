
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

/**
 * Abstract Syntax Tree
 */
public class AST {
    // Opcodes

    private static final char OP_LITERAL = 0;
    private static final char OP_NOT = 1;
    private static final char OP_ADD = 2;
    private static final char OP_SUB = 3;
    private static final char OP_MUL = 4;
    private static final char OP_DIV = 5;
    private static final char OP_REM = 6;
    private static final char OP_IS_INT = 7;
    private static final char OP_IS_STR = 8;
    private static final char OP_IS_LIST = 9;
    private static final char OP_IS_DICT = 10;
    private static final char OP_IS_ITER = 11;
    private static final char OP_EQUAL = 12;
    private static final char OP_IS_EMPTY = 13;
    private static final char OP_PUT = 14;
    private static final char OP_GET = 15;
    private static final char OP_RAND = 16;
    private static final char OP_SIZE = 17;
    private static final char OP_LESS = 18;
    private static final char OP_LESSEQUAL = 19;
    private static final char OP_SUBSTR = 20;
    private static final char OP_RESIZE = 21;
    private static final char OP_PUSH = 22;
    private static final char OP_POP = 23;
    private static final char OP_KEYS = 24;
    private static final char OP_VALUES = 25;
    private static final char OP_NEXT = 26;
    private static final char OP_LOG = 27;
    private static final char OP_ASSERT = 28;
    private static final char OP_DROP = 29;
    private static final char OP_GET_GLOBAL = 30;
    private static final char OP_SET_GLOBAL = 31;
    private static final char OP_JUMP = 32;
    private static final char OP_JUMP_IF_TRUE = 33;
    private static final char OP_NIL = 34;
    private static final char OP_DUP = 35;
    private static final char OP_NEW_LIST = 36;
    private static final char OP_NEW_DICT = 37;
    private static final char OP_NEW_STRINGBUFFER = 38;
    private static final char OP_STR_APPEND = 39;
    private static final char OP_TO_STRING = 40;
    private static final char OP_NEW_COUNTER = 41;
    private static final char OP_SWAP = 42;
    private static final char OP_CALL_FUNCTION = 43;
    // AST type constants
    private static final int AST_BUILTIN_FUNCTION = 0x100;
    private static final int AST_IDENTIFIER = 0;
    private static final int AST_LITERAL = AST_IDENTIFIER + 1;
    private static final int AST_FN_LIST = AST_LITERAL + 1;
    private static final int AST_SET = AST_FN_LIST + 1;
    private static final int AST_GLOBAL_ID = AST_SET + 1;
    private static final int AST_CLOSURE_ID = AST_GLOBAL_ID + 1;
    private static final int AST_BOXED_ID = AST_CLOSURE_ID + 1;
    private static final int AST_LOCAL_ID = AST_BOXED_ID + 1;
    private static final int AST_IF = AST_LOCAL_ID + 1;
    private static final int AST_AND = AST_IF + 1;
    private static final int AST_OR = AST_AND + 1;
    private static final int AST_REPEAT = AST_OR + 1;
    private static final int AST_FOREACH = AST_REPEAT + 1;
    private static final int AST_WHILE = AST_FOREACH + 1;
    private static final int AST_DO = AST_WHILE + 1;
    private static final int AST_STRINGJOIN = AST_DO + 1;
    private static final int AST_LIST = AST_STRINGJOIN + 1;
    private static final int AST_DICT = AST_LIST + 1;
    private static final int AST_FUNCTION = AST_DICT + 1;
    // Opcode mask, to extract opcode from AST_FUNCTIION type
    private static final int MASK_OP = 0xFF;
    // Scope temp class

    private static class Scope {

        private Scope parent;
        // number of arguments
        public int argc;
        // names of variables in stack frame, including the arguments
        // locals.size() is the size of the stack frame
        private Stack locals;
        // names of the variables in the stack frame, which are boxed
        private Stack boxed;
        // names of the (boxed) variables in the closure
        private Stack closure;

        public int varType(Object name) {
            if (boxed.contains(name)) {
                return AST_BOXED_ID;
            } else if (locals.contains(name)) {
                return AST_LOCAL_ID;
            } else if (closure.contains(name)) {
                return AST_CLOSURE_ID;
            } else if (parent != null) {
                return isClosured(this, name) ? AST_CLOSURE_ID : AST_GLOBAL_ID;
            } else {
                return AST_GLOBAL_ID;
            }
        }

        public int framePos(Object name) {
            return locals.indexOf(name);
        }

        public int frameSize() {
            return locals.size();
        }

        /**
         * Find out whether the variable "name" is put into the closure
         * of child, by this as parent. As a sideeffect this method
         * updates both the child and the parent-chain.
         * @param child
         * @param name
         * @return true if the variable is put in the closure of child, 
         *         or false if the variable is global.
         */
        private boolean isClosured(Scope child, Object name) {
            boolean in_closure;
            if (boxed.contains(name) || closure.contains(name)) {
                in_closure = true;
            } else if (locals.contains(name)) {
                in_closure = true;
                boxed.push(name);
            } else if (parent == null) {
                in_closure = false;
            } else {
                in_closure = isClosured(this, name);
            }
            if (in_closure) {
                child.closure.push(name);
            }
            return in_closure;
        }

        public Scope() {
            this.parent = null;
            locals = new Stack();
            boxed = new Stack();
            closure = new Stack();

        }

        public Scope(Scope parent, AST ast) {
            locals = new Stack();
            boxed = new Stack();
            closure = new Stack();
            this.parent = parent;
            for (int i = 0; i < ast.tree[1].tree.length; i++) {
                locals.push(ast.tree[1].tree[i].val);
            }
            for (int i = 0; i < ast.tree[2].tree.length; i++) {
                locals.push(ast.tree[2].tree[i].val);
            }
        }
    }
    // AST type tag
    private int type;
    // number of parameters if AST is a function
    // the value if AST is a literal or name if  identifier
    private Object val;
    // the children if AST is a list
    private AST[] tree;
    // scope data
    private Scope scope;
    // constant used when AST is not a list
    private static final AST[] emptytree = new AST[0];
    // truth value shorthand
    public static final Boolean TRUE = new Boolean(true);
    // 
    private static Random rnd = new Random();
    // definition of built in functions with fixed number of evaluated arguments
    private static String[] fn_names = {"not", "+", "-", "*", "/", "%", "is-integer", "is-string", "is-list", "is-dictionary", "is-iterator", "equals", "is-empty", "put", "get", "random", "size", "<", "<=", "substring", "resize", "push", "pop", "keys", "values", "get-next", "log", "assert"};
    private static int[] fn_arity = {1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 3, 2, 1, 1, 2, 2, 3, 2, 2, 1, 1, 1, 1, 1, 2};
    private static char[] fn_types = {OP_NOT, OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_REM, OP_IS_INT, OP_IS_STR, OP_IS_LIST, OP_IS_DICT, OP_IS_ITER, OP_EQUAL, OP_IS_EMPTY, OP_PUT, OP_GET, OP_RAND, OP_SIZE, OP_LESS, OP_LESSEQUAL, OP_SUBSTR, OP_RESIZE, OP_PUSH, OP_POP, OP_KEYS, OP_VALUES, OP_NEXT, OP_LOG, OP_ASSERT};
    // definition of builtins
    private static String[] builtin_names = {"set", "if", "and", "or", "repeat", "foreach", "while", "do", "stringjoin", "list", "dict", "function"};
    private static int[] builtin_type = {AST_SET, AST_IF, AST_AND, AST_OR, AST_REPEAT, AST_FOREACH, AST_WHILE, AST_DO, AST_STRINGJOIN, AST_LIST, AST_DICT, AST_FUNCTION};

    private static void assertlength(AST ast, int n) {
        if (ast.tree.length != n) {
            throw new Error("Wrong number of parameters: " + ast.toString());
        }
    }
    // walk through the ast and set the type tag

    private static void pass_ast_type(AST ast) {
        for (int i = 0; i < ast.tree.length; i++) {
            pass_ast_type(ast.tree[i]);
        }
        if (ast.tree.length > 0) {
            Object id = ast.tree[0].val;
            for (int i = 0; i < fn_names.length; i++) {
                if (id.equals(fn_names[i])) {
                    assertlength(ast, fn_arity[i] + 1);
                    ast.type = AST_BUILTIN_FUNCTION | fn_types[i];
                    return;
                }
            }
            for (int i = 0; i < builtin_names.length; i++) {
                if (id.equals(builtin_names[i])) {
                    ast.type = builtin_type[i];
                    return;
                }
            }
        }
    }
    
    private static void pass_vartype1(AST ast, Scope scope) {
        if (ast.type == AST_FUNCTION) {
            scope = new Scope(scope, ast);
        }
        ast.scope = scope;
        if (ast.type == AST_IDENTIFIER) {
            ast.scope.varType(ast.val);
        }
        for (int i = 0; i < ast.tree.length; i++) {
            pass_vartype1(ast.tree[i], scope);
        }
    }

    private static void pass_vartype2(AST ast) {
        if (ast.type == AST_IDENTIFIER) {
            ast.type = ast.scope.varType(ast.val);
        }
        for (int i = 0; i < ast.tree.length; i++) {
            pass_vartype2(ast.tree[i]);
        }

    }

    public static class CompiledExpr {

        public Object literals[];
        public byte code[];

        public CompiledExpr(StringBuffer sb, Stack stack) {
            code = new byte[sb.length()];
            for (int i = 0; i < code.length; i++) {
                code[i] = (byte) sb.charAt(i);
            }
            literals = new Object[stack.size()];
            stack.copyInto(literals);
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
            if (count > 0) {
                count--;
                return TRUE;
            } else {
                return null;
            }
        }
    }

    private static int const_id(Stack const_pool, Object val) {
        int pos = const_pool.indexOf(val);
        if (pos < 0) {
            pos = const_pool.size();
            const_pool.push(val);
        }

        if (pos > 255) {
            throw new Error("Too many literals");
        }
        return pos;
    }

    // append opcodes for setting a variable to the top-most stack value
    private static void pass_emit_set(StringBuffer code_acc, Stack const_pool, AST id) {
        System.out.println("set type:" + id.type + " name:" + id.val);
        if (id.type == AST_GLOBAL_ID) {
            code_acc.append(OP_SET_GLOBAL);
            code_acc.append((char) const_id(const_pool, id.val));
        } else {
            code_acc.append(OP_SET_GLOBAL);
            code_acc.append((char) const_id(const_pool, id.val));
        // FIXME: add local vars
        //throw new Error("Unknown ID type: " + id.type);
        }

    }

    private static Object compiled_function(AST ast) {
        throw new Error("not implemented yet");
    }
    private static void pass_emit(StringBuffer code_acc, Stack const_pool, AST ast) {
        if ((ast.type & AST_BUILTIN_FUNCTION) != 0) {
            for (int i = 1; i < ast.tree.length; i++) {
                pass_emit(code_acc, const_pool, ast.tree[i]);
            }
            code_acc.append((char) (ast.type & MASK_OP));

        } else {
            switch (ast.type) {

                case AST_LITERAL: {
                    code_acc.append(OP_LITERAL);
                    code_acc.append((char) const_id(const_pool, ast.val));
                    break;
                }
                case AST_FN_LIST: {
                    for (int i = 0; i < ast.tree.length; i++) {
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                    }
                    code_acc.append(OP_CALL_FUNCTION);
                    code_acc.append((char) ast.tree.length);
                    break;
                }
                case AST_FUNCTION: {
                    code_acc.append(OP_LITERAL);
                    code_acc.append((char) const_id(const_pool, compiled_function(ast)));
                    break;
                }
                case AST_SET: {
                    // value
                    pass_emit(code_acc, const_pool, ast.tree[2]);
                    // set
                    pass_emit_set(code_acc, const_pool, ast.tree[1]);
                    break;
                }
                case AST_GLOBAL_ID: {
                    code_acc.append(OP_GET_GLOBAL);
                    code_acc.append((char) const_id(const_pool, ast.val));
                    break;
                }
                case AST_IF: {
                    //    code for ast.tree[1]
                    //    jump_if_true -> label1
                    //    code for ast.tree[3]
                    //    jump -> label2
                    // label1: 
                    //    code for ast.tree[2]
                    // label2:

                    assertlength(ast, 4);
                    int pos0,  pos1,  len;
                    pass_emit(code_acc, const_pool, ast.tree[1]);

                    code_acc.append(OP_JUMP_IF_TRUE);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos0 = code_acc.length();

                    code_acc.append(OP_LITERAL);
                    code_acc.append((char) const_id(const_pool, null));

                    code_acc.append(OP_JUMP);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos1 = code_acc.length();
                    len = pos1 - pos0;
                    code_acc.setCharAt(pos0 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos0 - 2, (char) ((len >> 8) & 0xff));

                    pass_emit(code_acc, const_pool, ast.tree[2]);
                    len = code_acc.length() - pos1;
                    code_acc.setCharAt(pos1 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos1 - 2, (char) ((len >> 8) & 0xff));
                    break;
                }
                case AST_AND: {
                    assertlength(ast, 3);
                    int pos0,  pos1,  len;

                    pass_emit(code_acc, const_pool, ast.tree[1]);

                    code_acc.append(OP_JUMP_IF_TRUE);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos0 = code_acc.length();

                    code_acc.append(OP_NIL);

                    code_acc.append(OP_JUMP);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos1 = code_acc.length();
                    len = pos1 - pos0;
                    code_acc.setCharAt(pos0 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos0 - 2, (char) ((len >> 8) & 0xff));

                    pass_emit(code_acc, const_pool, ast.tree[2]);
                    len = code_acc.length() - pos1;
                    code_acc.setCharAt(pos1 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos1 - 2, (char) ((len >> 8) & 0xff));
                    break;
                }
                case AST_OR: {
                    assertlength(ast, 3);
                    int pos0,  pos1,  len;

                    pass_emit(code_acc, const_pool, ast.tree[1]);

                    code_acc.append(OP_DUP);

                    code_acc.append(OP_JUMP_IF_TRUE);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos0 = code_acc.length();

                    code_acc.append(OP_DROP);

                    pass_emit(code_acc, const_pool, ast.tree[2]);

                    pos1 = code_acc.length();
                    len = pos1 - pos0;
                    code_acc.setCharAt(pos0 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos0 - 2, (char) ((len >> 8) & 0xff));

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
                    int pos0,  pos1,  len;
                    code_acc.append(OP_NIL);
                    pass_emit(code_acc, const_pool, ast.tree[1]);
                    code_acc.append(OP_NEW_COUNTER);

                    code_acc.append(OP_JUMP);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos0 = code_acc.length();

                    code_acc.append(OP_SWAP);
                    for (int i = 2; i < ast.tree.length; i++) {
                        code_acc.append(OP_DROP);
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                    }
                    code_acc.append(OP_SWAP);

                    pos1 = code_acc.length();
                    len = pos1 - pos0;
                    code_acc.setCharAt(pos0 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos0 - 2, (char) ((len >> 8) & 0xff));

                    code_acc.append(OP_DUP);

                    code_acc.append(OP_NEXT);

                    code_acc.append(OP_JUMP_IF_TRUE);
                    len = pos0 - (code_acc.length() + 2);
                    code_acc.append((char) ((len >> 8) & 0xff));
                    code_acc.append((char) (len & 0xff));

                    code_acc.append(OP_DROP);

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
                    int pos0,  pos1,  len;
                    code_acc.append(OP_NIL);

                    pass_emit(code_acc, const_pool, ast.tree[2]);

                    code_acc.append(OP_JUMP);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos0 = code_acc.length();

                    code_acc.append(OP_SWAP);
                    for (int i = 3; i < ast.tree.length; i++) {
                        code_acc.append(OP_DROP);
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                    }
                    code_acc.append(OP_SWAP);

                    pos1 = code_acc.length();
                    len = pos1 - pos0;
                    code_acc.setCharAt(pos0 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos0 - 2, (char) ((len >> 8) & 0xff));

                    code_acc.append(OP_DUP);

                    code_acc.append(OP_NEXT);

                    pass_emit_set(code_acc, const_pool, ast.tree[1]);

                    code_acc.append(OP_JUMP_IF_TRUE);
                    len = pos0 - (code_acc.length() + 2);
                    code_acc.append((char) ((len >> 8) & 0xff));
                    code_acc.append((char) (len & 0xff));

                    code_acc.append(OP_DROP);

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

                    int pos0,  pos1,  len;
                    code_acc.append(OP_NIL);

                    code_acc.append(OP_JUMP);
                    code_acc.append((char) 0);
                    code_acc.append((char) 0);
                    pos0 = code_acc.length();

                    for (int i = 2; i < ast.tree.length; i++) {
                        code_acc.append(OP_DROP);
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                    }

                    pos1 = code_acc.length();
                    len = pos1 - pos0;
                    code_acc.setCharAt(pos0 - 1, (char) (len & 0xff));
                    code_acc.setCharAt(pos0 - 2, (char) ((len >> 8) & 0xff));

                    pass_emit(code_acc, const_pool, ast.tree[1]);

                    code_acc.append(OP_JUMP_IF_TRUE);
                    len = pos0 - (code_acc.length() + 2);
                    code_acc.append((char) ((len >> 8) & 0xff));
                    code_acc.append((char) (len & 0xff));

                    break;
                }
                case AST_DO: {
                    for (int i = 1; i < ast.tree.length; i++) {
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                        if (i < ast.tree.length - 1) {
                            code_acc.append(OP_DROP);
                        }
                    }
                    break;
                }
                case AST_STRINGJOIN: {
                    code_acc.append(OP_NEW_STRINGBUFFER);

                    for (int i = 1; i < ast.tree.length; i++) {
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                        code_acc.append(OP_STR_APPEND);
                    }
                    code_acc.append(OP_TO_STRING);
                    break;
                }
                case AST_LIST: {
                    code_acc.append(OP_NEW_LIST);

                    for (int i = 1; i < ast.tree.length; i++) {
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                        code_acc.append(OP_PUSH);
                    }
                    break;
                }
                case AST_DICT: {
                    code_acc.append(OP_NEW_DICT);

                    if (ast.tree.length % 2 == 1) {
                        throw new Error("Unmatched key/value: " + ast.toString());
                    }
                    for (int i = 1; i < ast.tree.length; i++) {
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                        i++;
                        pass_emit(code_acc, const_pool, ast.tree[i]);
                        code_acc.append(OP_PUSH);
                    }
                    break;
                }
                default: {
                    throw new Error("Unknown ast type: " + ast.type);
                }
            }
        }
    }

    public static CompiledExpr compile(AST ast) {
        pass_ast_type(ast);

        pass_vartype1(ast, new Scope());
        pass_vartype2(ast);

        StringBuffer code_acc = new StringBuffer();
        Stack literals = new Stack();
        pass_emit(code_acc, literals, ast);

        return new CompiledExpr(code_acc, literals);
    }
    
    private static class ReturnAddress {
        public int pc;
        public byte[] code;
        ReturnAddress(int pc, byte[] code) {
            this.pc = pc;
            this.code = code;
        }
    }

    public static Object execute(CompiledExpr coexp, Hashtable globals) {
        Stack stack = new Stack();
        byte code[] = coexp.code;
        Object literals[] = coexp.literals;
        {
            StringBuffer sb = new StringBuffer();
            for (int pc = 0; pc < code.length; pc++) {
                sb.append(code[pc] + " ");
            }
            System.out.println("code: " + sb.toString());
        }
        for (int pc = 0; pc < code.length; pc++) {
            //System.out.println("pc: " + pc + " code:" + code[pc]);
            switch (code[pc]) {
                case OP_LITERAL: {
                    stack.push(literals[code[++pc]]);
                    break;
                }
                case OP_NOT: {
                    stack.push(stack.pop() == null ? TRUE : null);
                    break;
                }
                case OP_ADD: {
                    stack.push(new Integer(
                            ((Integer) stack.pop()).intValue() +
                            ((Integer) stack.pop()).intValue()));
                    break;
                }
                case OP_SUB: {
                    stack.push(new Integer(
                            -((Integer) stack.pop()).intValue() +
                            ((Integer) stack.pop()).intValue()));
                    break;
                }
                case OP_MUL: {
                    stack.push(new Integer(
                            ((Integer) stack.pop()).intValue() *
                            ((Integer) stack.pop()).intValue()));
                    break;
                }
                case OP_DIV: {
                    stack.push(new Integer(
                            ((Integer) stack.pop()).intValue() /
                            ((Integer) stack.pop()).intValue()));
                    break;
                }
                case OP_REM: {
                    stack.push(new Integer(
                            ((Integer) stack.pop()).intValue() %
                            ((Integer) stack.pop()).intValue()));
                    break;
                }
                case OP_IS_INT: {
                    stack.push(stack.pop() instanceof Integer ? TRUE : null);
                    break;
                }
                case OP_IS_STR: {
                    stack.push(stack.pop() instanceof String ? TRUE : null);
                    break;
                }
                case OP_IS_LIST: {
                    stack.push(stack.pop() instanceof Stack ? TRUE : null);
                    break;
                }
                case OP_IS_DICT: {
                    stack.push(stack.pop() instanceof Hashtable ? TRUE : null);
                    break;
                }
                case OP_IS_ITER: {
                    stack.push(stack.pop() instanceof Enumeration ? TRUE : null);
                    break;
                }
                case OP_EQUAL: {
                    Object o = stack.pop();
                    if (o == null) {
                        stack.push(stack.pop() == null ? TRUE : null);
                    } else {
                        stack.push(o.equals(stack.pop()) ? TRUE : null);
                    }
                    break;
                }
                case OP_IS_EMPTY: {
                    Object o = stack.pop();
                    if (o instanceof Stack) {
                        stack.push(((Stack) o).empty() ? TRUE : null);
                    } else if (o instanceof Hashtable) {
                        stack.push(((Hashtable) o).isEmpty() ? TRUE : null);
                    } else if (o instanceof Enumeration) {
                        stack.push(((Enumeration) o).hasMoreElements() ? null : TRUE);
                    } else {
                        stack.push(null);
                    }
                    break;
                }
                case OP_PUT: {
                    Object val = stack.pop();
                    Object key = stack.pop();
                    Object container = stack.peek();
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
                    Object key = stack.pop();
                    Object container = stack.pop();
                    if (container instanceof Stack) {
                        stack.push(((Stack) container).elementAt(((Integer) key).intValue()));
                    } else if (container instanceof Hashtable) {
                        stack.push(((Hashtable) container).get(key));
                    } else {
                        stack.push(null);
                    }
                    break;
                }
                case OP_RAND: {
                    Object o = stack.pop();
                    if (o instanceof Integer) {
                        stack.push(new Integer((rnd.nextInt() & 0x7fffffff) % ((Integer) o).intValue()));
                    } else if (o instanceof Stack) {
                        Stack s = (Stack) o;
                        stack.push(s.elementAt((rnd.nextInt() & 0x7fffffff) % s.size()));
                    } else {
                        stack.push(null);
                    }
                    break;
                }
                case OP_SIZE: {
                    Object o = stack.pop();
                    if (o instanceof Stack) {
                        stack.push(new Integer(((Stack) o).size()));
                    } else if (o instanceof String) {
                        stack.push(new Integer(((String) o).length()));
                    } else if (o instanceof Hashtable) {
                        stack.push(new Integer(((Hashtable) o).size()));
                    } else {
                        stack.push(null);
                    }
                    break;
                }
                case OP_LESS: {
                    Object o2 = stack.pop();
                    Object o1 = stack.pop();
                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        stack.push(((Integer) o1).intValue() < ((Integer) o2).intValue() ? TRUE : null);
                    } else {
                        stack.push(o1.toString().compareTo(o2.toString()) < 0 ? TRUE : null);
                    }
                    break;
                }
                case OP_LESSEQUAL: {
                    Object o2 = stack.pop();
                    Object o1 = stack.pop();
                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        stack.push(((Integer) o1).intValue() <= ((Integer) o2).intValue() ? TRUE : null);
                    } else {
                        stack.push(o1.toString().compareTo(o2.toString()) <= 0 ? TRUE : null);
                    }
                    break;
                }
                case OP_SUBSTR: {
                    int end = ((Integer) stack.pop()).intValue();
                    int start = ((Integer) stack.pop()).intValue();
                    stack.push(((String) stack.pop()).substring(start, end));
                    break;
                }
                case OP_RESIZE: {
                    int newsize = ((Integer) stack.pop()).intValue();
                    ((Stack) stack.peek()).setSize(newsize);
                    break;
                }
                case OP_PUSH: {
                    Object val = stack.pop();
                    ((Stack) stack.peek()).push(val);
                    break;
                }
                case OP_POP: {
                    stack.push(((Stack) stack.pop()).pop());
                    break;
                }
                case OP_KEYS: {
                    stack.push(((Hashtable) stack.pop()).keys());
                    break;
                }
                case OP_VALUES: {
                    Object o = stack.pop();
                    if (o instanceof Stack) {
                        stack.push(((Stack) o).elements());
                    } else if (o instanceof Hashtable) {
                        stack.push(((Hashtable) o).elements());
                    } else {
                        stack.push(null);
                    }
                    break;
                }
                case OP_NEXT: {
                    Enumeration e = (Enumeration) stack.pop();
                    if (e.hasMoreElements()) {
                        stack.push(e.nextElement());
                    } else {
                        stack.push(null);
                    }
                    break;
                }
                case OP_LOG: {
                    System.out.println(stack.peek());
                    break;
                }
                case OP_ASSERT: {
                    if (stack.pop() == null) {
                        throw new Error("Assert error: " + stack.pop().toString());
                    }
                    break;
                }
                case OP_DROP: {
                    stack.pop();
                    break;
                }
                case OP_GET_GLOBAL: {
                    stack.push(globals.get(literals[code[++pc]]));
                    break;
                }
                case OP_SET_GLOBAL: {
                    Object o = stack.peek();
                    Object key = literals[code[++pc]];
                    if (o == null) {
                        globals.remove(key);
                    } else {
                        globals.put(key, o);
                    }
                    break;
                }
                case OP_JUMP: {
                    int delta = (code[++pc] << 8) | (0xff & code[++pc]);
                    pc += delta;
                    break;
                }
                case OP_JUMP_IF_TRUE: {
                    int delta = (code[++pc] << 8) | (0xff & code[++pc]);
                    if (stack.pop() != null) {
                        pc += delta;
                    }
                    break;
                }
                case OP_NIL: {
                    stack.push(null);
                    break;
                }
                case OP_DUP: {
                    stack.push(stack.peek());
                    break;
                }
                case OP_NEW_LIST: {
                    stack.push(new Stack());
                    break;
                }
                case OP_NEW_DICT: {
                    stack.push(new Hashtable());
                    break;
                }
                case OP_NEW_STRINGBUFFER: {
                    stack.push(new StringBuffer());
                    break;
                }
                case OP_STR_APPEND: {
                    Object o = stack.pop();
                    ((StringBuffer) stack.peek()).append(o.toString());
                    break;
                }
                case OP_TO_STRING: {
                    stack.push(stack.pop().toString());
                    break;
                }
                case OP_NEW_COUNTER: {
                    stack.push(new Counter(((Integer) stack.pop()).intValue()));
                    break;
                }
                case OP_SWAP: {
                    Object o1 = stack.pop();
                    Object o2 = stack.pop();
                    stack.push(o1);
                    stack.push(o2);
                    break;
                }
                case OP_CALL_FUNCTION: {
                    int argc = code[pc++];
                    Object returnaddress = new ReturnAddress(pc, code);
                    
                    break;
                }
                default: {
                    throw new Error("Unknown opcode");
                }
            }
        }
        return stack.pop();
    }

    private AST(AST[] t) {
        type = AST_FN_LIST;
        val = null;
        tree = t;
    }

    private AST(String s) {
        type = AST_IDENTIFIER;
        val = s;
        tree = emptytree;
    }

    private AST(Object o) {
        type = AST_LITERAL;
        val = o;
        tree = emptytree;
    }

    public String toString() {
        if (type == AST_IDENTIFIER) {
            return " id(" + val + ")";
        } else if (type == AST_LITERAL) {
            return " literal(" + val + ")";
        } else if (type == AST_FN_LIST) {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            for (int i = 0; i < tree.length; i++) {
                sb.append(tree[i].toString());
            }
            sb.append(" ]");
            return sb.toString();
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("{" + type);
            for (int i = 0; i < tree.length; i++) {
                sb.append(tree[i].toString());
            }
            sb.append(" }");
            return sb.toString();
        }
    }

    /////////////////////////////////////
    // The parser
    ////
    /**
     * Parse the next Josi expression from an input stream.
     * @param is the input stream to read from
     * @return a new Josi expression as an abstract syntax tree
     * @throws java.io.IOException
     */
    public static AST readExpression(InputStream is) throws IOException {
        Stack stack = new Stack();
        int c = is.read();
        do {
            // end of file
            if (c == -1) {
                return null;

            // end of list
            } else if (c == ']') {
                AST result[];
                c = is.read();
                // find out how much of the stack
                // is a part of the terminated list.
                // null indicates a "["
                int pos = stack.search(null);
                // ] with no [ begun
                if (pos == -1) {
                    result = emptytree;
                } else {
                    // stack search includes the null, which we want to skip
                    pos--;
                    // move the elements from the stack
                    result = new AST[pos];
                    while (pos > 0) {
                        pos--;
                        result[pos] = (AST) stack.pop();
                    }
                    // pop the null
                    stack.pop();
                }
                // create the list obj
                stack.push(new AST(result));


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
                stack.push(new AST(new Integer(i)));

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
                stack.push(new AST((Object) sb.toString()));

            // Identifier
            } else {
                StringBuffer sb = new StringBuffer();
                while (c > ' ' && c != '[' && c != ']') {
                    sb.append((char) c);
                    c = is.read();
                }
                stack.push(new AST(sb.toString()));
            }
        } while (stack.empty() || stack.size() > 1 || stack.elementAt(0) == null);
        return (AST) stack.pop();
    }
}
