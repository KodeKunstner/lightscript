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

	private static final char OP_LITERAL = 'l';
	private static final char OP_ADD = 'a';
	private static final char OP_SUB = 'b';
	private static final char OP_RET = 0xFF;

	private static final int MASK_OP = 0xFF;
	private static final int MASK_BUILTIN_BINOP = 0x100;
	private static final int MASK_BUILTIN_UNOP = 0x200;

	private static final int AST_IDENTIFIER = 0;
	private static final int AST_LITERAL = AST_IDENTIFIER + 1;
	private static final int AST_LIST = AST_LITERAL + 1;
	private static final int AST_ADD = MASK_BUILTIN_BINOP | OP_ADD;
	private static final int AST_SUB = MASK_BUILTIN_BINOP | OP_SUB;

	private int type;
	private Object val;
	private AST[] tree;
	private static final AST[] emptytree = new AST[0];


	private static String[] builtin_names = {"+", "-"};
	private static int[] builtin_types = {AST_ADD, AST_SUB};

	private static void pass_ast_type(AST ast) {
		for(int i = 0; i < ast.tree.length; i++) {
			pass_ast_type(ast.tree[i]);
		}
		if(ast.tree.length > 0) {
			for(int i = 0; i < builtin_names.length; i++) {
				if(ast.tree[0].val.equals(builtin_names[i])) {
					ast.type = builtin_types[i];
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
			for(int i = 0; i< code.length; i++) {
				code[i] = (byte)sb.charAt(i);
			}
			literals = new Object[stack.size()];
			stack.copyInto(literals);
		}
	}

	private static void pass_emit(StringBuffer code_acc, Stack literals, AST ast) {
		if((ast.type & MASK_BUILTIN_BINOP) != 0) {
			pass_emit(code_acc, literals, ast.tree[1]);
			pass_emit(code_acc, literals, ast.tree[2]);
			code_acc.append((char)(ast.type & MASK_OP));

		} else if((ast.type & AST_LITERAL) != 0) {
			code_acc.append((char)OP_LITERAL);

			int pos = literals.indexOf(ast.val);
			if(pos < 0) {
				pos = literals.size();
				literals.push(ast.val);
			}

			if(pos > 255) {
				throw new Error("Too many literals in expression");
			}
			code_acc.append((char)pos);
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
		for(int pc=0; pc < code.length; pc++) {
			System.out.println("pc:" + pc + " op:" + code[pc]);
			switch(code[pc]) {
				case OP_LITERAL: 
					stack.push(literals[code[++pc]]);
					break;
				case OP_ADD:
					stack.push(new Integer(
						((Integer)stack.pop()).intValue() +
						((Integer)stack.pop()).intValue()));
					break;
				case OP_SUB:
					stack.push(new Integer(
						- ((Integer)stack.pop()).intValue() +
						((Integer)stack.pop()).intValue()));
					break;
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
		if(type == AST_IDENTIFIER) {
			return " id(" + val + ")";
		} else if(type == AST_LITERAL) {
			return " literal(" + val + ")";
		} else if(type == AST_LIST) {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			for(int i = 0 ; i < tree.length; i++) {
				sb.append(tree[i].toString());
			}
			sb.append(" ]");
			return sb.toString();
		} 
		throw new java.lang.Error("Unhandled case");
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
                        result[pos] = (AST)stack.pop();
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
