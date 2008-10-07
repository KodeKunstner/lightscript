import java.io.*;
import java.util.*;

final class Yolan {
	private Object closure;
	private int fn;

	private Yolan(int fn) {
		this.fn = fn;
		closure = null;
	}
	private static Hashtable globals = new Hashtable();

	// Initialisation code, 
	// with compact representation of function names
	static {
		// space terminated names of builtin functions
		// in the same order as the implementation in apply
		String str = "println + - * / < num if while set! do str ";

		// parse the names, creat new yolan function, and add it to the globals
		int i, j, k;
		k = i = j = 0;
		while(i < str.length()) {
			if(str.charAt(i) == ' ') {
				globals.put(str.substring(j,i), new Yolan(k));
				i++; k++;
				j = i;
			}
			i++;
		}
	}

	// Builtin functions and macros
	private Object apply(Object X[]) {
		switch(fn) {
			// println
			case 0: { 	Object arg = e(X[1]); 
					System.out.println(arg); 
					return arg; }
			// +
			case 1: return num(ival(e(X[1])) + ival(e(X[2])));
			// -
			case 2: return num(ival(e(X[1])) + ival(e(X[2])));
			// *
			case 3: return num(ival(e(X[1])) + ival(e(X[2])));
			// /
			case 4: return num(ival(e(X[1])) + ival(e(X[2])));
			// <
			case 5: return ival(e(X[1])) < ival(e(X[2])) ? X:null;
			// num
			case 6: return Integer.valueOf((String)X[1]);
			// if
			case 7: return (e(X[1]) != null)?e(X[2]):e(X[3]);
			// while
			case 8: {	
					Object result = null;
					while(e(X[1]) != null) {
						result = e(X[2]);
					}
					return result; 
			}
			// set!
			case 9: {	
					Object o = e(X[2]);
					globals.put(X[1], o);
					return o; 
			}
			// do
			case 10: {	Object result = null;
					for(int i = 1; i < X.length; i++) {
						result = e(X[i]);
					}
					return result;
			}
			// str 
			case 11: return X[1];
		}
		return null;
	}
	

	// shorthand conversion from object to integer
	private static int ival(Object o) {
		return ((Integer)o).intValue();
	}

	// shorthand conversion from integer to object
	private static Object num(int i) {
		return new Integer(i);
	}

	// evaluate list or lookup symbol
	private static Object e(Object o) {
		if(o instanceof Object[]) {
			Object X[] = ((Object [])o);
			o = X[0];
			if(!(o instanceof Yolan)) {
				X[0] = o = globals.get(o);
			}
			return ((Yolan)o).apply(X);
		} else {
			return globals.get(o);
		}
	}

	// parse a list of expressions
	private static Object[] parse(InputStream is) throws Exception {
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

	// parse and evaluate code from input stream
	public static Object eval(InputStream is) throws Exception {
		Object exprs[] = parse(is);
		Object result = null;
		for(int i = 0; i < exprs.length; i++) {
			result = e(exprs[i]);
		}
		return result;
	}

	// main/test-function
	public static void main(String X[]) throws Exception {
		eval(new FileInputStream(new File(X[0])));
	}
}
