import java.io.*;
import java.util.*;

final class Yolan {


	//////////////////////////////////////
	// The object itself represents a closure/a delayed computation.
	////

	// closure
	private Object c;

	// code id;
	private int fn;

	//constructor
	private Yolan(int fn, Object c) {
		this.fn = fn;
		this.c = c;
	}

	// Eval function
	private Object e() {
		Object o;
		//System.out.println("e() fn: " + fn);
		switch(fn) {
			// builtin function dummy
			// case -1
			
			// Literal
			case 0: return c;

			// Id 
			case 1: return globals.get(c);

			// Expression list/function call
			case 2: {
				o = val(c, 0);
				if(o instanceof Yolan) {
					Yolan yl = (Yolan) o;
					// if builtin function
					if(yl.fn == -1) {
						this.fn = ((Integer)yl.c).intValue();
						return e();
					// userdefined function
					} 
					if(yl.fn == 10) {
						args = (Object []) c;
						return yl.e();
					}
				} else {
					// ...
				}
				print(o);
				System.out.println("Error, unexpected function type");
				return e();
			}

			// "+"
			case 3: return num(ival(c, 1) + ival(c, 2));
			// "-"
			case 4: return num(ival(c, 1) - ival(c, 2));
			// "*"
			case 5: return num(ival(c, 1) * ival(c, 2));
			// "/"
			case 6: return num(ival(c, 1) / ival(c, 2));
			// "<"
			case 7: return ival(c, 1) < ival(c, 2)? c : null;
			// "if"
			case 8: return (val(c, 1) != null)? val(c, 2) : val(c, 3);
			// "println"
			case 9: o = val(c, 1); print(o); System.out.println(); return o;
			// userdefined function
                        case 10: {
                                Object [] X = (Object [])c;
                                for(int i=1;i<X.length;i++) {
                                        pushglobal(X[i], ((Yolan)args[i]).e());
                                }
                                Object result = ((Yolan)X[0]).e();
                                popglobals(X.length - 1);
                                return result;
                        }
			// lambda
                        case 11: {
                                Object [] lambda_expr = (Object [])c;
				Object [] arguments = (Object[]) ((Yolan)lambda_expr[1]).c;

				print(lambda_expr); System.out.println();
                                Object [] function = new Object[arguments.length + 1];
				function[0] = lambda_expr[2];
				for(int i = 0; i < arguments.length; i++) {
					function[i+1] = ((Yolan)arguments[i]).c;
				}
	
                                return new Yolan(10, function);
                        }
			// "set!"
			case 12: return globals.put(((Yolan)((Object [])c)[1]).c, val(c, 2));
			// "do"
			case 13: {
				Object result = null;
				int stmts = ((Object[])c).length;
				for(int i = 1; i < stmts; i++) {
					result = val(c, i);
				}
				return result;
			}
			// "while"
			case 14: {
				Object result = null;
				while(val(c, 1) != null) {
					result = val(c, 2);
				}
				return result;
			}

		}
		return null;
	}

	////////////////////////////////////
	// Utility functions for evaluation
	////

	// Evaluate yolan list closure into an integer
	private static int ival(Object c, int i) {
		return ((Integer)((Yolan)((Object[])c)[i]).e()).intValue();
	}

	// Evaluate yolan list closure into an object
	private static Object val(Object c, int i) {
		return ((Yolan)((Object[])c)[i]).e();
	}

	// Shorthand conversion from integer to object
	private static Object num(int i) {
		return new Integer(i);
	}

	// print an object
	private static void print(Object o) {
		if(o instanceof Object[]) {
			Object os[] = (Object[])o;
			print("[");
			for(int i = 0; i< os.length; i++) {
				print(os[i]);
				print(" ");
			}
			print("]");
		} else if(o instanceof Yolan) {
			Yolan yl = (Yolan)o;
			print("Yolan" + yl.fn + "(");
			print(yl.c);
			print(")");
		} else {
			System.out.print(o);
		}
	}

	////////////////////////////////////////
	// Runtime support for user-function calls,
	// and nested defines
	////
	private static Object args[];
	private static Stack stack = new Stack();

        void pushglobal(Object key, Object val) {
                stack.push(key);
                stack.push(globals.put(key, val));
        }

        void popglobals(int n) {
                while(n > 0) {
                        Object val = stack.pop();
                        Object key = stack.pop();
                        if(val == null) {
                                globals.remove(key);
                        } else {
                                globals.put(key, val);
                        }
                        n--;
                }
        }


	///////////////////////////////////////
	// The static runtime
	////

	// Globals
	private static Hashtable globals = new Hashtable();

	// Register a builtin function
	private static void addBuiltin(int id, String name) {
		// the 1 indicates that it is a builtin function
		globals.put(name, new Yolan(-1, new Integer(id)));
	}

	// Constructor for runtime
	static {
		addBuiltin(3, "+");
		addBuiltin(4, "-");
		addBuiltin(5, "*");
		addBuiltin(6, "/");
		addBuiltin(7, "<");
		addBuiltin(8, "if");
		addBuiltin(9, "println");
		addBuiltin(11, "lambda");
		addBuiltin(12, "set!");
		addBuiltin(13, "do");
		addBuiltin(14, "while");
		// ...
	}


	/////////////////////////////////////
	// The parser
	////
	
	// The parser itself, parses a list
	private static Yolan parse(InputStream is) throws IOException {
		// Accumlator
		Stack s = new Stack();
		// Current char
		int c = is.read();
		while(c != -1 && c != ']') {

			// Whitespace
			if(c <= ' ') {
				c = is.read();

			// List
			} else if(c == '[') {
				s.push(parse(is));
				c = is.read();

			// Number
			} else if('0' <= c && c <= '9') {
				int i = 0;
				do {
					i = i * 10 + c - '0';
					c = is.read();
				} while('0' <= c && c <= '9');
				s.push(new Yolan(0, new Integer(i)));

			// String
			} else if(c == '"') {
				StringBuffer sb = new StringBuffer();
				c = is.read();
				while(c != '"' && c != -1) {
					if(c == '\\') {
						c = is.read();
					}
					sb.append((char) c);
					c = is.read();
				}
				c = is.read();
				s.push(new Yolan(0, sb.toString()));

			// Identifier
			} else {
				StringBuffer sb = new StringBuffer();
				while(c > ' ' && c != '[' && c!= ']') {
					sb.append((char) c);
					c = is.read();
				}
				s.push(new Yolan(1, sb.toString()));
			}
		}
		Object result[] = new Object[s.size()];
		s.copyInto(result);
		return new Yolan(2, result);
	}

	// Parse and evaluate code from input stream
	public static Object eval(InputStream is) throws IOException {
		Object exprs[] = (Object[])parse(is).c;
		Object result = null;
		for(int i = 0; i < exprs.length; i++) {
			result = ((Yolan)exprs[i]).e();
		}
		return result;
	}

	///////////////////////////
	// Main/test function
	////
	public static void main(String X[]) throws IOException {
		System.out.println(eval(new FileInputStream(new File(X[0]))));
	}
}
