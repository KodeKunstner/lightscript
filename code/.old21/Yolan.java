import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.StringBuffer;
import java.util.Stack;
import java.util.Hashtable;

/*
final class Yolan2 {


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
    public Object e() {
        Object o;
        //GUI.println("e() fn: " + fn);
        switch (fn) {
            // builtin function dummy
            // case -1

            // Literal
            case 0:
                return c;

            // Id 
            case 1:
                return globals.get(c);

            // Expression list/function call
            case 2: {
                o = val(c, 0);
                if (o instanceof Yolan) {
                    Yolan yl = (Yolan) o;
                    // builtin function
                    if (yl.fn == -1) {
                        this.fn = ((Integer) yl.c).intValue();
                        return e();
                    }
                    // userdefined function
                    if (yl.fn == 10) {
                        args = (Object[]) c;
                        return yl.e();
                    }
                } else {
                    // ...
                }
                print("Error, unexpected function type:" + o + "\n");
                return e();
            }

            // "+"
            case 3:
                return num(ival(c, 1) + ival(c, 2));
                
            // "-"
            case 4:
                return num(ival(c, 1) - ival(c, 2));
                
            // "*"
            case 5:
                return num(ival(c, 1) * ival(c, 2));
                
            // "/"
            case 6:
                return num(ival(c, 1) / ival(c, 2));
                
            // "<"
            case 7:
                return ival(c, 1) < ival(c, 2) ? c : null;
            
            // "if"
            case 8:
                return (val(c, 1) != null) ? val(c, 2) : val(c, 3);
                
            // "println"
            case 9:
                o = val(c, 1);
                print(o);
                print("\n");
                return o;
                
            // userdefined function
            case 10: {
                Object[] X = (Object[]) c;
                for (int i = 1; i < X.length; i++) {
                    pushglobal(X[i], ((Yolan) args[i]).e());
                }
                Object result = ((Yolan) X[0]).e();
                popglobals(X.length - 1);
                return result;
            }
            
            // lambda
            case 11: {
                Object[] lambda_expr = (Object[]) c;
                Object[] arguments = (Object[]) ((Yolan) lambda_expr[1]).c;

                //print(lambda_expr); print("\n");
                Object[] function = new Object[arguments.length + 1];
                function[0] = lambda_expr[2];
                for (int i = 0; i < arguments.length; i++) {
                    function[i + 1] = ((Yolan) arguments[i]).c;
                }

                return new Yolan(10, function);
            }
            
            // "set!"
            case 12:
                return globals.put(((Yolan) ((Object[]) c)[1]).c, val(c, 2));
                
            // "do"
            case 13: {
                Object result = null;
                int stmts = ((Object[]) c).length;
                for (int i = 1; i < stmts; i++) {
                    result = val(c, i);
                }
                return result;
            }
            
            // "while"
            case 14: {
                Object result = null;
                int stmts = ((Object[]) c).length;
                while (val(c, 1) != null) {
                    for(int i = 2; i < stmts; i++) {
                        result = val(c, i);
                    }
                }
                return result;
            }
            
            // key-down-handler
            case 15: {
                //return Main.gui.keydown[ival(c, 1)] = (Yolan) val(c, 2);
		throw new Error("Not implemented");
            }

        }
        return null;
    }

    ////////////////////////////////////
    // Utility functions for evaluation
    ////

    // Evaluate yolan list closure into an integer
    private static int ival(Object c, int i) {
        return ((Integer) ((Yolan) ((Object[]) c)[i]).e()).intValue();
    }

    // Evaluate yolan list closure into an object
    private static Object val(Object c, int i) {
        return ((Yolan) ((Object[]) c)[i]).e();
    }

    // Shorthand conversion from integer to object
    private static Object num(int i) {
        return new Integer(i);
    }

    // print an object
    private static void print(Object o) {
        if (o instanceof Object[]) {
            Object os[] = (Object[]) o;
            print("[");
            for (int i = 0; i < os.length; i++) {
                print(os[i]);
                print(" ");
            }
            print("]");
        } else if (o instanceof Yolan) {
            Yolan yl = (Yolan) o;
            print("Yolan" + yl.fn + "(");
            print(yl.c);
            print(")");
        } else {
            System.out.print(o.toString());
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
        while (n > 0) {
            Object val = stack.pop();
            Object key = stack.pop();
            if (val == null) {
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
        addBuiltin(15, "key-down-handler");
    // ...
    }


    /////////////////////////////////////
    // The parser
    ////
    // The parser itself, parses a list
    private static Yolan parse(InputStream is) throws IOException {
        // Accumulator
        Stack s = new Stack();
        // Current char
        int c = is.read();
        while (c != -1 && c != ']') {

            // Whitespace
            if (c <= ' ') {
                c = is.read();

            // List
            } else if (c == '[') {
                s.push(parse(is));
                c = is.read();

            // Number
            } else if ('0' <= c && c <= '9') {
                int i = 0;
                do {
                    i = i * 10 + c - '0';
                    c = is.read();
                } while ('0' <= c && c <= '9');
                s.push(new Yolan(0, new Integer(i)));

            // String
            } else if (c == '"') {
                StringBuffer sb = new StringBuffer();
                c = is.read();
                while (c != '"' && c != -1) {
                    if (c == '\\') {
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
                while (c > ' ' && c != '[' && c != ']') {
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
        Object exprs[] = (Object[]) parse(is).c;
        Object result = null;
        for (int i = 0; i < exprs.length; i++) {
            result = ((Yolan) exprs[i]).e();
        }
        return result;
    }
*/


class Yolan implements Function {
	private Object c;
	private int fn;

    //constructor
    private Yolan(int fn, Object c) {
        this.fn = fn;
        this.c = c;
    }

    // Eval function
    public Object e() {

    }

    ////////////////////////////////////
    // Utility functions for evaluation
    ////

    // Evaluate yolan list closure into an integer
    private static int ival(Object c, int i) {
        return ((Integer) ((Yolan) ((Object[]) c)[i]).e()).intValue();
    }

    // Evaluate yolan list closure into an object
    private static Object val(Object c, int i) {
        return ((Yolan) ((Object[]) c)[i]).e();
    }

    // Shorthand conversion from integer to object
    private static Object num(int i) {
        return new Integer(i);
    }

    // print an object
    private static void print(Object o) {
        if (o instanceof Object[]) {
            Object os[] = (Object[]) o;
            print("(");
            for (int i = 0; i < os.length; i++) {
                print(os[i]);
                print(" ");
            }
            print(")");
        } else if (o instanceof Yolan) {
            Yolan yl = (Yolan) o;
            print("Yolan" + yl.fn + "[");
            print(yl.c);
            print("]");
        } else {
            System.out.print(o.toString());
        }
    }


	private static String[] names = {"println", "to-string", "open-input-stream", "parse", "compile"};
	private static Function functions[] = null;

	public Object apply(Object [] args) {
		switch(fn) 
	{ /* println*/ case 0: {
		String s = args[1].toString();
		System.out.println(s);
		return s;
	} /* to-string*/ case 1: {
		Object o = args[1];
		if(o instanceof Object[]) {
			Object[] os = (Object[]) o;
			StringBuffer sb = new StringBuffer("(");
			for(int i = 0; i < os.length; i++) {
				sb.append(" ");
				args[1] = os[i];
				sb.append(this.apply(args));
			}
			sb.append(" )");
			return sb.toString();
		} else {
			return o.toString();
		}
	} /* open-input-stream */ case 2: {
		try {
			return new FileInputStream(new File(args[1].toString()));
		} catch(IOException e) {
			return null;
		}
	} /* parse */ case 3: {
		try {
			InputStream is = (InputStream)args[1];
			int ch = is.read();
	
			Stack result = new Stack();
	
			for(;;) {
				StringBuffer str = new StringBuffer();
	
				if(ch < 0 || ch == ')') {
					return stack2objs(result);
				} else if(ch == ';') {
					do {
						ch = is.read();
					} while(ch >= ' ');
				} else if(ch == '(') {
					result.push(this.apply(args));
					ch = is.read();
				} else if(ch == '"') {
					ch = is.read();
					while(ch != '"' && ch >= 0) {
						if(ch == '\\') {
							ch = is.read();
							if('0' <= ch && ch <= '7') {
								ch = ch * 64 + is.read() * 8 + is.read();
								ch -= '0' * (64 + 8 + 1);
							}
						}
						str.append((char)ch);
						ch = is.read();
					}
					ch = is.read();
					Object os[] = new Object[2];
					os[0] = "string";
					os[1] = str.toString();
					result.push(os);
				} else if('0' <= ch && ch <= '9') {
					do {
						str.append((char)ch);
						ch = is.read();
					} while('0' <= ch);
					Object os[] = new Object[2];
					os[0] = "number";
					os[1] = str.toString();
					result.push(os);
				} else if('*' <= ch) {
					do {
						str.append((char)ch);
						ch = is.read();
					} while('*' <= ch);
					result.push(str.toString());
				} else {
					ch = is.read();
				}
			}
		} catch(IOException e) {
			return null;
		}

	} /* compile*/ case 4: {
		// compiler args: [parent, ["compiler", macro-hashtable, code-accumulator,...], code]
		Object code = args[2];
		Hashtable macros = (Hashtable) ((Object [])args[1])[1];
		Function macro = null;

		if(code instanceof Object[]) {
			if(((Object[])code).length > 0) {
				macro = (Function) macros.get(((Object[])code)[0]);
			} else {
				macro = (Function) macros.get("()");
			}


		} else if(code instanceof String) {
			macro = (Function) macros.get("symbol macro");
		}

		if(macro == null) {
			macro = (Function) macros.get("default macro");
		}

		return macro.apply(args);

	} /* wrong function id */ default: {
		throw new Error("Undefined function");
	}}}

	private Yolan(int fn) {
		this.fn = fn;
	}

	public static Function getFunction(String s) {
		if(functions == null) {
			int count = names.length;
			functions = new Function[count];
			for(int i = 0; i < count; i++) {
				functions[i] = new Yolan(i);
			}
		}

		int i = 0;
		while(!s.equals(names[i])) {
			i++;
		}
		return functions[i];
	}

	public static Object call(String s, Object o1) {
		Object os[] = { null, o1 };
		return getFunction(s).apply(os);
	}

	public static Object call(String s, Object o1, Object o2) {
		Object os[] = { null, o1, o2 };
		return getFunction(s).apply(os);
	}

	public static Object call(String s, Object o1, Object o2, Object o3) {
		Object os[] = { null, o1, o2, o3 };
		return getFunction(s).apply(os);
	}



	//
	// Utility functions
	//
	public static Object[] stack2objs(Stack s) {
		Object os[] = new Object[s.size()];
		s.copyInto(os);
		return os;
	}

	public static void main(String[] args) throws java.io.IOException {
		call("println", call("to-string", call("parse", call("open-input-stream", args[0]))));

		//InputStream is = new FileInputStream(new File(args[0]));
		//System.out.println(print_r(parse(is)));
	}
}
