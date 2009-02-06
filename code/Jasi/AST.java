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
	private static final byte AST_IDENTIFIER = 0;
	private static final byte AST_LITERAL = 1;
	private static final byte AST_LIST = 2;
	private byte type;
	private Object val;
	private AST[] tree;

	private AST(AST[] t) {
		type = AST_LIST;
		tree = t;
	}

	private AST(String s) {
		type = AST_IDENTIFIER;
		val = s;
	}

	private AST(Object o) {
		type = AST_LITERAL;
		val = o;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(type == AST_IDENTIFIER) {
			sb.append(" id_").append(val);
		} else if(type == AST_LITERAL) {
			sb.append(" ").append(val);
		} else if(type == AST_LIST) {
			sb.append("[");
			for(int i = 0 ; i < tree.length; i++) {
				sb.append(tree[i].toString());
			}
			sb.append(" ]");
		} 
		return sb.toString();
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
                    result = new AST[0];
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
