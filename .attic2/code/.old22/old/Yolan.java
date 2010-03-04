
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
                    // if builtin function
                    if (yl.fn == -1) {
                        this.fn = ((Integer) yl.c).intValue();
                        return e();
                    // userdefined function
                    }
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
                    for (int i = 2; i < stmts; i++) {
                        result = val(c, i);
                    }
                }
                return result;
            }

            // key-down-handler
            case 15: {
                return Main.gui.keydown[ival(c, 1)] = (Yolan) val(c, 2);
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
            print("( ");
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
            Main.print(o.toString());
        }
    }

    ////////////////////////////////////////
    // Runtime support for user-function calls,
    // and nested defines
    ////
    private static Object args[];
    private static Stack stack = new Stack();

    private void pushglobal(Object key, Object val) {
        stack.push(key);
        stack.push(globals.put(key, val));
    }

    private void popglobals(int n) {
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
        while (c != -1 && c != ')') {

            // Whitespace
            if (c <= ' ') {
                c = is.read();

            // Comment
            } else if (c == ';') {
                do {
                    c = is.read();
                } while (c > '\n');

            // List
            } else if (c == '(') {
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
                while (c > ' ' && c != '(' && c != ')') {
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
    // Parser

    private static StringBuffer tokenstring = new StringBuffer();
    private static Object[] literallist(Object o) {
        Object objs[] = {"literal", o};
        return objs;
    }
    public static Object[] parse2(InputStream is) throws IOException {
        int ch = is.read();

        Stack result = new Stack();
        
        while (ch >= 0 && ch != ')') {
            tokenstring.setLength(0);
            if (ch == ';') {
                do {
                    ch = is.read();
                } while (ch >= ' ');
            } else if (ch == '(') {
                result.push(parse2(is));
                ch = is.read();
            } else if (ch == '"') {
                ch = is.read();
                while (ch != '"' && ch >= 0) {
                    if (ch == '\\') {
                        ch = is.read();
                        if ('0' <= ch && ch <= '7') {
                            ch = ch * 64 + is.read() * 8 + is.read();
                            ch -= '0' * (64 + 8 + 1);
                        }
                    }
                    tokenstring.append((char) ch);
                    ch = is.read();
                }
                ch = is.read();
                result.push(literallist(tokenstring.toString()));
            } else if ('0' <= ch && ch <= '9') {
                int iresult = 0;
                do {
                    iresult = iresult * 10 + ch - '0';
                    ch = is.read();
                } while ('0' <= ch);
                result.push(literallist(num(iresult)));
            } else if ('*' <= ch) {
                do {
                    tokenstring.append((char) ch);
                    ch = is.read();
                } while ('*' <= ch);
                result.push(tokenstring.toString());
            } else {
                ch = is.read();
            }
        }
        Object objs[] = new Object[result.size()];
        result.copyInto(objs);
        return objs;
    }
    
    public static Yolan compile(Object o) {
        if(o instanceof Object[]) {
            Object[] code = (Object[])o;
        } else {
            
        }
        return null;
    }

    // Parse and evaluate code from input stream
    public static Object eval(InputStream is) throws IOException {
        print(parse2(is));
        /*
        Object exprs[] = (Object[]) parse(is).c;
        Object result = null;
        for (int i = 0; i < exprs.length; i++) {
            result = ((Yolan) exprs[i]).e();
        }
        return result;
         */
        return null;
    }
}
