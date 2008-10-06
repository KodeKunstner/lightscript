import java.io.*;
import java.util.*;

interface Yoco {
	public Object eval(Object X[]);
}

final class Yolan {
	/**
	 * The global variables in the context
	 */
	public Hashtable globals;

	/**
	 * Implementation of builtin unary eager functions.
	 */
	class BuiltinUnary implements Yoco {
		public int fn;
		public BuiltinUnary(int fn) {
			this.fn = fn;
		}
		public Object eval(Object X[]) {
			Object a0 = e(X[1]);
			switch(fn) {
				case 0: System.out.println(a0); return a0;
			}
			return null;
		}
	}

	/**
	 * Implementation of builtin binary eager functions.
	 */
	class BuiltinBinary implements Yoco {
		int fn;
		public BuiltinBinary(int fn) {
			this.fn = fn;
		}
		public Object eval(Object X[]) {
			Object a0 = e(X[1]);
			Object a1 = e(X[2]);
			switch(fn) {
				case 0: return num(ival(a0) + ival(a1));
				case 1: return num(ival(a0) - ival(a1));
				case 2: return num(ival(a0) * ival(a1));
				case 3: return num(ival(a0) / ival(a1));
				case 4: return ival(a0) < ival(a1)?a0:null;
			}
			return null;
		}
	}

	/**
	 * Implementation of functions which evalutes their parameters on demand.
	 */
	class BuiltinLazy implements Yoco {
		int fn;
		public BuiltinLazy(int fn) {
			this.fn = fn;
		}
		public Object eval(Object X[]) {
			switch(fn) {
				case 0: return Integer.valueOf((String)X[1]);
				case 1: return (e(X[1]) != null)?e(X[2]):e(X[3]);
				case 2: {	
						Object result = null;
						while(e(X[1]) != null) {
							result = e(X[2]);
						}
						return result; 
				}
				case 3: {	
						Object o = e(X[2]);
						globals.put(X[1], o);
						return o; 
				}
				case 4: {	
						Object result = null;
						for(int i = 1; i < X.length; i++) {
							result = e(X[i]);
						}
						return result;
				}
			}
			return null;
		}
	}

	/**
	 * Utility function for converting from object to integer
	 */
	public static int ival(Object o) {
		return ((Integer)o).intValue();
	}

	/**
	 * Utility function for converting from integer to object
	 */
	public static Object num(int i) {
		return new Integer(i);
	}

	/**
	 * Evaluate a an expression if it is a list, or just lookup the symbol.
	 * @param o the expression. 
	 */
	public Object e(Object o) {
		if(o instanceof Object[]) {
			Object X[] = ((Object [])o);
			o = X[0];
			if(!(o instanceof Yoco)) {
				X[0] = o = globals.get(o);
			}
			return ((Yoco)o).eval(X);
		} else {
			Object tmp;
			tmp = globals.get(o);
			if(tmp == null) {
				System.out.println("Error: " + o.toString() + " not defined!");
			}
			return tmp;
		}
	}


	/**
	 * Utility function for adding a global variable
	 */
	public Yolan add(Object str, Object val) {
		globals.put(str, val);
		return this;
	}

	/**
	 * Create a new Yolan execution context and add builtin functions to it.
	 */
	public Yolan() {
		globals = new Hashtable();

		this	.add("println", new BuiltinUnary(0))
			.add("+", new BuiltinBinary(0))
			.add("-", new BuiltinBinary(1))
			.add("*", new BuiltinBinary(2))
			.add("/", new BuiltinBinary(3))
			.add("<", new BuiltinBinary(4))
			.add("num", new BuiltinLazy(0))
			.add("if", new BuiltinLazy(1))
			.add("while", new BuiltinLazy(2))
			.add("set!", new BuiltinLazy(3))
			.add("do", new BuiltinLazy(4))
		;

	}

	/**
	 * Parse a series of expressions
	 */
	public static Object[] parse(InputStream is) throws Exception {
		int c;
		Stack s = new Stack();
		c = is.read();
		while(c != -1 && c != ']') {
			if(c <= ' ') {
				c = is.read();
			} else if(c == '[') {
				s.push(parse(is));
				c = is.read();
			} else {
				StringBuffer sb = new StringBuffer();
				boolean quoted = false;
				while((quoted && c != 0) || (c > ' ' && c != '[' && c!= ']')) {
					if(c == '"') {
						quoted = !quoted;
						c = is.read();
					} else {
						if(c == '@') {
							c = is.read();
						}
						sb.append((char) c);
						c = is.read();
					}
				}
				s.push(sb.toString());
			}
		}
		Object result[] = new Object[s.size()];
		s.copyInto(result);
		return result;
	}

	public static void main(String X[]) throws Exception {
		InputStream is = new FileInputStream(new File(X[0]));
		Yolan yl = new Yolan();
		yl.e(parse(is)[0]);
		System.out.println();
	}
}
