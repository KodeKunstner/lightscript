
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
    
    // AST type constants
    private static final int AST_IDENTIFIER = 0;
    private static final int AST_LITERAL = AST_IDENTIFIER + 1;
    private static final int AST_LIST = AST_LITERAL + 1;
    private static final int AST_FUNCTION = 0x100;
    
    // Opcode mask, to extract opcode from AST_FUNCTIION type
    private static final int MASK_OP = 0xFF;
    
    // AST type tag
    private int type;
    // number of parameters if AST is a function
    private int arity;
    // the value if AST is a literal or name if  identifier
    private Object val;
    // the children if AST is a list
    private AST[] tree;
    // constant used when AST is not a list
    private static final AST[] emptytree = new AST[0];
    // truth value shorthand
    public static final Boolean TRUE = new Boolean(true);
    
    // definition of builtin in functions with fixed number of evaluated arguments
    private static String[] fn_names = {"not", "+", "-", "*", "/", "%", "is-integer", "is-string", "is-list", "is-dictionary", "is-iterator", "equals", "is-empty", "put", "get", "random", "size", "<", "<=", "substring", "resize", "push", "pop", "keys", "values", "get-next", "log", "assert"};
    private static int[] fn_arity = {1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 3, 2, 1, 1, 2, 2, 3, 2, 2, 1, 1, 1, 1, 1, 2};
    private static char[] fn_types = {OP_NOT, OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_REM, OP_IS_INT, OP_IS_STR, OP_IS_LIST, OP_IS_DICT, OP_IS_ITER, OP_EQUAL, OP_IS_EMPTY, OP_PUT, OP_GET, OP_RAND, OP_SIZE, OP_LESS, OP_LESSEQUAL, OP_SUBSTR, OP_RESIZE, OP_PUSH, OP_POP, OP_KEYS, OP_VALUES, OP_NEXT, OP_LOG, OP_ASSERT};

    private static void pass_ast_type(AST ast) {
        for (int i = 0; i < ast.tree.length; i++) {
            pass_ast_type(ast.tree[i]);
        }
        if (ast.tree.length > 0) {
            for (int i = 0; i < fn_names.length; i++) {
                if (ast.tree[0].val.equals(fn_names[i])) {
                    if (ast.tree.length != fn_arity[i] + 1) {
                        throw new Error("Wrong number of arguments: " + ast.toString());
                    }
                    ast.type = AST_FUNCTION | fn_types[i];
                    ast.arity = fn_arity[i];
                    return;
                }
            }
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

    private static void pass_emit(StringBuffer code_acc, Stack literals, AST ast) {
        if ((ast.type & AST_FUNCTION) != 0) {
            for (int i = 1; i <= ast.arity; i++) {
                pass_emit(code_acc, literals, ast.tree[i]);
            }
            code_acc.append((char) (ast.type & MASK_OP));

        } else if (ast.type == AST_LITERAL) {
            code_acc.append((char) OP_LITERAL);

            int pos = literals.indexOf(ast.val);
            if (pos < 0) {
                pos = literals.size();
                literals.push(ast.val);
            }

            if (pos > 255) {
                throw new Error("Too many literals in expression: " + ast.toString());
            }
            code_acc.append((char) pos);
        }
    }

    public static CompiledExpr compile(AST ast) {
        pass_ast_type(ast);

        StringBuffer code_acc = new StringBuffer();
        Stack literals = new Stack();
        pass_emit(code_acc, literals, ast);
        System.out.println("code:" + code_acc.toString());

        return new CompiledExpr(code_acc, literals);
    }

    public static Object execute(CompiledExpr coexp) {
        Stack stack = new Stack();
        byte code[] = coexp.code;
        Object literals[] = coexp.literals;
        for (int pc = 0; pc < code.length; pc++) {
            System.out.println("pc:" + pc + " op:" + code[pc]);
            switch (code[pc]) {
                case OP_LITERAL: {
                    stack.push(literals[code[++pc]]);
                    break;
                }
                case OP_NOT: {
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
                    stack.push(stack.pop().equals(stack.pop()) ? TRUE : null);
                    break;
                }
                case OP_IS_EMPTY: {
                    Object o = stack.pop();
                    if(o instanceof Stack) {
                        stack.push(((Stack)o).empty() ? TRUE : null);
                    } else if(o instanceof Hashtable) {
                        stack.push(((Hashtable)o).isEmpty() ? TRUE : null);
                    } else if(o instanceof Enumeration) {
                        stack.push(((Enumeration)o).hasMoreElements() ? null : TRUE);
                    } else {
                        stack.push(null);
                    }
                    break;
                }
                case OP_PUT: {
                    Object val = stack.pop();
                    Object key = stack.pop();
                    Object container = stack.peek();
                    if(container instanceof Stack) {
                        ((Stack)container).setElementAt(val, ((Integer)key).intValue());
                    } else if(container instanceof Hashtable) {
                        if(val == null) {
                            ((Hashtable)container).remove(key);
                        } else {
                            ((Hashtable)container).put(key, val);
                        }
                    }
                    break;
                }
                case OP_GET: {
                    Object key = stack.pop();
                    Object container = stack.pop();
                    if(container instanceof Stack) {
                        stack.push(((Stack)container).elementAt(((Integer)key).intValue()));
                    } else if(container instanceof Hashtable) {
                        stack.push(((Hashtable)container).get(key));                        
                    }
                    break;
                }
                case OP_RAND: {
                    break;
                }
                case OP_SIZE: {
                    break;
                }
                case OP_LESS: {
                    break;
                }
                case OP_LESSEQUAL: {
                    break;
                }
                case OP_SUBSTR: {
                    break;
                }
                case OP_RESIZE: {
                    break;
                }
                case OP_PUSH: {
                    break;
                }
                case OP_POP: {
                    break;
                }
                case OP_KEYS: {
                    break;
                }
                case OP_VALUES: {
                    break;
                }
                case OP_NEXT: {
                    break;
                }
                case OP_LOG: {
                    System.out.println(stack.peek());
                    break;
                }
                case OP_ASSERT: {
                }
            }
        }
        return stack.pop();
    }

    private AST(AST[] t) {
        type = AST_LIST;
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
        } else if (type == AST_LIST) {
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
