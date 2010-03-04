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

    // vars
    private static Object vars[] = new Object[0];
    private static Stack stack = new Stack();
    

    private static int getVarId(Object key) {
        int i = 0;
        while (i < vars.length && !vars[i].equals(key)) {
            i += 2;
        }

        if (i == vars.length) {
            Object objs[] = new Object[i + 2];
            System.arraycopy(vars, 0, objs, 0, vars.length);
            vars = objs;
            vars[i] = key;
        }
        return i + 1;
    }

    private static void setVar(Object key, Object val) {
        int id = getVarId(key);
        vars[id] = val;
    }
    
    // Eval function
    public Object e() {
        //GUI.println("e() fn: " + fn);
        switch (fn) {
            // builtin function dummy
            // case -1

            // Literal
            case 0:
                return c;

            // Id 
            case 1: {
                fn = 16;
                c = new Integer(getVarId(c));
                return e();
            }

            // Expression list/function call
            case 2: {
                Object o = val(c, 0);
                if (o instanceof Yolan) {
                    Yolan yl = (Yolan) o;
                    
                    // builtin function
                    if (yl.fn == -1) {
                        this.fn = ((Integer) yl.c).intValue();
                        return e();    
                    
                    // user defined function
                    } else if (yl.fn == 10) {
                        Object args[] = (Object[]) c;
                        Object function[] = (Object []) yl.c;
                        
                        // evaluate arguments and push to stack
                        for(int i = 1; i < args.length; i++) {
                            stack.push(((Yolan)args[i]).e());
                        }
                        
                        // swap argument values on stack with local values
                        int spos = stack.size();
                        for(int i = 1; i < args.length; i++) {
                            int pos = ((Integer)function[function.length - i]).intValue();
                            spos--;
                            Object t = stack.elementAt(spos);
                            stack.setElementAt(vars[pos], spos);
                            vars[pos] = t;
                        }
                        
                        // evaluate the result
                        Object result = ((Yolan)function[0]).e();
                        
                        // restore previous values
                        for(int i = args.length - 1; i > 0; i--) {
                            vars[((Integer)function[i]).intValue()] = stack.pop();
                        }
                        
                        return result;
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
            case 9: {
                Object o = val(c, 1);
                print(o);
                print("\n");
                return o;
            }

            // userdefined function
            // case 10: 
                // dummy - not called, but see case 2.
            

            // lambda
            case 11: {
                Object[] lambda_expr = (Object[]) c;
                Object[] arguments = (Object[]) ((Yolan) lambda_expr[1]).c;

                //print(lambda_expr); print("\n");
                Object[] function = new Object[arguments.length + 1];
                function[0] = lambda_expr[2];
                for (int i = 0; i < arguments.length; i++) {
                    function[i + 1] = num(getVarId(((Yolan) arguments[i]).c));
                }

                return new Yolan(10, function);
            }

            // "set!"
            case 12: {
                Object t[] = (Object[]) c;
                t[1] = new Integer(getVarId(((Yolan) t[1]).c));
                fn = 17;
                return e();
            }

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
                    for (int i = 2; i < stmts; i++) {
                        result = val(c, i);
                    }
                }
                return result;
            }

            // "<="
            case 15:
                return ival(c, 1) <= ival(c, 2) ? c : null;

            // Get value
            case 16: {
                int id = ((Integer) c).intValue();
                Object x = vars[id];

                return vars[((Integer) c).intValue()];
            }

            // Set value
            case 17: {
                vars[((Integer) ((Object[]) c)[1]).intValue()] = val(c, 2);
                return null;
            }
            // 
            default: {
                throw new Error("Unexpected case "+fn);
                
            }

        }
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
            Main.print(o.toString());
        }
    }

    // Register a builtin function
    private static void addBuiltin(int val, String name) {
        int id = getVarId(name);
        vars[id] = new Yolan(-1, new Integer(val));

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
        addBuiltin(15, "<=");
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
        while (c != -1 && c != ']') {

            // Whitespace
            if (c <= ' ') {
                c = is.read();

            // Comment
            } else if (c == ';') {
                do {
                    c = is.read();
                } while (c > '\n');

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
        //print(exprs);
        for (int i = 0; i < exprs.length; i++) {
            result = ((Yolan) exprs[i]).e();
        }
        return result;
    }
}
