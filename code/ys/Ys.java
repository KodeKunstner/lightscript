import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

public abstract class Ys {
	public static String stringify(Object o) {
		if(o == null) {
			return "null";
		} if(o instanceof Literal) {
			return ((Literal)o).value.toString();
		} else if(o instanceof Object[]) {
			StringBuffer sb = new StringBuffer();
			Object[] os = (Object[]) o;
			sb.append("[");
			for(int i = 0; i < os.length; i++) {
				sb.append(" " + stringify(os[i]));
			}
			sb.append(" ]");
			return sb.toString();
		} else if(o instanceof Function) {
			Function f = (Function) o;
			return "Function( argc:" + f.argc
				+ ", locals:" + f.locals.toString()
				+ ", boxed:" + f.boxed.toString()
				+ ", closure:" + f.closure.toString()
				+ ", body:" + stringify(f.body);
		} else {
			return o.toString();
		}
	}
	private static class Literal {
		public Object value;
		public Literal(Object value) {
			this.value = value;
		}
	}
	private static class Id {
		public static byte LOCAL = 0;
		public static byte BOXED = 1;
		public static byte CLOSURE = 2;
		public byte type;
		public int pos;
		public Id(byte type, int pos) {
			this.type = type;
			this.pos = pos;
		}
		public String toString() {
			return (type == LOCAL ? "local" : type == BOXED ? "boxed" : "closure") + pos;
		}
	}
	private static class Function {
		public int argc;
		public Object[] body;
		public Stack locals;
		public Stack boxed;
		public Stack closure;
		public Function(Object[] list) {
			Enumeration e;
			body = new Object[list.length - 3];
			for(int i = 0; i < body.length; i++) {
				body[i] = list[i+3];
			}
			argc = ((Object[])list[1]).length;
			locals = new Stack();
			boxed = new Stack();
			closure = new Stack();

			findIds(locals, list[1]);
			findIds(locals, list[2]);

			findIds(closure, body);

			// local variables are not placed in the closure
			e = locals.elements();
			while(e.hasMoreElements()) {
				closure.removeElement(e.nextElement());
			}

			// closure variables are automagically boxed
			e = closure.elements();
			while(e.hasMoreElements()) {
				boxed.removeElement(e.nextElement());
			}

			updateIds(body);
		}
		private void updateIds(Object [] list) {
			for(int i = 0; i < list.length; i++) {
				Object o = list[i];
				if(o instanceof String) {
					int pos;
					pos = locals.indexOf(o);
					if(pos >= 0) {
						list[i] = new Id(
							boxed.contains(o) ? Id.BOXED : Id.LOCAL, 
							pos);
						continue;
					} 
					pos = closure.indexOf(o);
					if(pos >= 0) {
						list[i] = new Id(Id.CLOSURE, pos);
						continue;
					}
				} else if(o instanceof Object[]) {
					updateIds((Object[])o);
				}
			}
		}
		private void findIds(Stack s, Object o) {
			if(o instanceof Object[]) {
				Object[] os = (Object[])o;
				for(int i = 0; i < os.length; i++) {
					findIds(s, os[i]);
				}
			} else if(o instanceof String) {
				s.push(o);
			} else if(o instanceof Function) {
				Function f = (Function) o;
				f.addOuter(s);
				f.addOuter(boxed);
			}
		}
		private void addOuter(Stack s) {
			Enumeration e = closure.elements();
			while(e.hasMoreElements()) {
				Object o = e.nextElement();
				if(!s.contains(o)) {
					s.push(o);
				}
			}
		}
	}

	private static class Builtin {
		int opCode;
		public Builtin(String name) {
			opCode = builtins.indexOf(name);
		}
	}

	private static Stack builtins;

	static {
		builtins = new Stack();
		builtins.push("log");
		builtins.push("set");
	}

	/////////////////////////////////////
	// Factories
	////
	private static Object createId(String name) {
		if(builtins.contains(name)) {
			return new Builtin(name);
		} else {
			return name;
		}
	}
	private static Object create(Object[] list) {
		if(list.length > 0) {
			Object fn = list[0];
			if(fn.equals("function")) {
				return new Function(list);
			} 
		}
		return list;
	}

	/////////////////////////////////////
	// The parser
	////
	private static final Object[] emptylist = new Object[0];
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
}
