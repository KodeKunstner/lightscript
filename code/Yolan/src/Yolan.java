
import java.io.InputStream;
import java.io.IOException;
import java.util.Stack;

public final class Yolan {


    //////////////////////////////////////
    // The object itself represents a closure/a delayed computation.
    ////
    /** The closure */
    private Object c;
    /** The function id */
    private int fn;

    /**
     * Constructor for a new delayed computation
     * @param fn the function id
     * @param c the closure
     */
    private Yolan(int fn, Object c) {
        this.fn = fn;
        this.c = c;
    }

    /** Function ID constants */
    //<editor-fold>
    private static final int FN_NATIVE_DUMMY = -2;
    private static final int FN_BUILTIN_DUMMY = -1;
    private static final int FN_LITERAL = 0;
    private static final int FN_RESOLVE_GET_VAR = 1;
    private static final int FN_RESOLVE_EVAL_LIST = 2;
    private static final int FN_ADD = 3;
    private static final int FN_SUB = 4;
    private static final int FN_MUL = 5;
    private static final int FN_DIV = 6;
    private static final int FN_LESS = 7;
    private static final int FN_IF = 8;
    private static final int FN_TO_STRING = 9;
    private static final int FN_USER_DEFINED = 10;
    private static final int FN_LAMBDA = 11;
    private static final int FN_RESOLVE_SET = 12;
    private static final int FN_DO = 13;
    private static final int FN_WHILE = 14;
    private static final int FN_LESS_EQUAL = 15;
    private static final int FN_GET_VAR = 16;
    private static final int FN_SET = 17;
    private static final int FN_NATIVE = 18;
    //</editor-fold>

    /**
     * Evaluate the delayed computation
     * @return the result
     */
    public Object value() {
        //GUI.println("e() fn: " + fn);
        switch (fn) {
            // foreign function dummy
            // case -2

            // builtin function dummy
            // case -1

            // Literal
            case FN_LITERAL:
                return c;

            // Id 
            case FN_RESOLVE_GET_VAR: {
                fn = FN_GET_VAR;
                c = new Integer(getVarId(c));
                return value();
            }

            // Expression list/function call
            case FN_RESOLVE_EVAL_LIST: {
                Object o = val(c, 0);
                if (o instanceof Yolan) {
                    Yolan yl = (Yolan) o;

                    // builtin function
                    if (yl.fn == FN_BUILTIN_DUMMY) {
                        this.fn = ((Integer) yl.c).intValue();
                        Object args[] = (Object[]) c;
                        Object newargs[] = new Object[args.length - 1];
                        for (int i = 0; i < newargs.length; i++) {
                            newargs[i] = args[i + 1];
                        }
                        c = newargs;
                        return value();

                    // native function implementing Function-interface;
                    } else if (yl.fn == FN_NATIVE_DUMMY) {
                        this.fn = FN_NATIVE;
                        Object args[] = (Object[]) c;
                        Yolan params[] = new Yolan[args.length - 1];
                        for (int i = 0; i < params.length; i++) {
                            params[i] = (Yolan) args[i + 1];
                        }
                        args[0] = yl.c;
                        args[1] = params;
                        return value();

                    // user defined function
                    } else if (yl.fn == FN_USER_DEFINED) {
                        Object args[] = (Object[]) c;
                        // evaluate arguments and push to stack
                        for (int i = 1; i < args.length; i++) {
                            stack.push(((Yolan) args[i]).value());
                        }

                        return yl.value();
                    }
                } else {
                    // ...
                }
                throw new Error("Unexpected list type");
            }

            // "+"
            case FN_ADD:
                return num(ival(c, 0) + ival(c, 1));

            // "-"
            case FN_SUB:
                return num(ival(c, 0) - ival(c, 1));

            // "*"
            case FN_MUL:
                return num(ival(c, 0) * ival(c, 1));

            // "/"
            case FN_DIV:
                return num(ival(c, 0) / ival(c, 1));

            // "<"
            case FN_LESS:
                return ival(c, 0) < ival(c, 1) ? c : null;

            // "if"
            case FN_IF:
                return (val(c, 0) != null) ? val(c, 1) : val(c, 2);

            // to-string
            case FN_TO_STRING: {
                return to_string(new StringBuffer(), val(c, 0)).toString();
            }

            // userdefined function
            case FN_USER_DEFINED: {
                Object args[] = (Object[]) c;
                // swap argument values on stack with local values
                int spos = stack.size();
                for (int i = 1; i < args.length; i++) {
                    int pos = ((Integer) args[args.length - i]).intValue();
                    spos--;
                    Object t = stack.elementAt(spos);
                    stack.setElementAt(vars[pos], spos);
                    vars[pos] = t;
                }

                // evaluate the result
                Object result = ((Yolan) args[0]).value();

                // restore previous values
                for (int i = args.length - 1; i > 0; i--) {
                    vars[((Integer) args[i]).intValue()] = stack.pop();
                }
                return result;

            }


            // lambda
            case FN_LAMBDA: {
                Object[] lambda_expr = (Object[]) c;
                Object[] arguments = (Object[]) ((Yolan) lambda_expr[0]).c;

                //print(lambda_expr); print("\n");
                Object[] function = new Object[arguments.length + 1];
                function[0] = lambda_expr[1];
                for (int i = 0; i < arguments.length; i++) {
                    function[i + 1] = num(getVarId(((Yolan) arguments[i]).c));
                }

                return new Yolan(FN_USER_DEFINED, function);
            }

            // "set!"
            case FN_RESOLVE_SET: {
                Object t[] = (Object[]) c;
                t[0] = new Integer(getVarId(((Yolan) t[0]).c));
                fn = FN_SET;
                return value();
            }

            // "do"
            case FN_DO: {
                Object result = null;
                int stmts = ((Object[]) c).length;
                for (int i = 0; i < stmts; i++) {
                    result = val(c, i);
                }
                return result;
            }

            // "while"
            case FN_WHILE: {
                Object result = null;
                int stmts = ((Object[]) c).length;
                while (val(c, 0) != null) {
                    for (int i = 1; i < stmts; i++) {
                        result = val(c, i);
                    }
                }
                return result;
            }

            // "<="
            case FN_LESS_EQUAL:
                return ival(c, 0) <= ival(c, 1) ? c : null;

            // Get value - specialised result of variable name
            case FN_GET_VAR: {
                int id = ((Integer) c).intValue();
                Object x = vars[id];

                return x;
            }

            // Set value - specialised result of set!
            case FN_SET: {
                Object o = val(c, 1);
                vars[((Integer) ((Object[]) c)[0]).intValue()] = o;
                return o;
            }
            // Native Function
            case FN_NATIVE: {
                Object objs[] = (Object[]) c;
                return ((Yoco) objs[0]).apply((Yolan[]) objs[1]);

            }
            // 
            default: {
                throw new Error("Unexpected case " + fn);

            }

        }
    }

    /**
     * Override the toString function
     * @return
     */
    public String toString() {
        return to_string(new StringBuffer(), this).toString();
    }
    
    ////////////////////////////////////
    // Utility functions for evaluation
    // <editor-fold>
    ////

    // Support for dynamic scoped variables
    // <editor-fold>
    /**
     * An array containing alternating 
     * variable names and values
     */
    private static Object vars[] = new Object[0];
    /** Stack used for function calls */
    private static Stack stack = new Stack();

    /**
     * Lookup the id of a variable
     * @param key the name of the variable
     * @return the id which maps into the vars-array
     */
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

    /**
     * Set the value of a variable
     * @param key the variable name
     * @param val the new value
     */
    private static void setVar(Object key, Object val) {
        int id = getVarId(key);
        vars[id] = val;
    }
    // </editor-fold>

    // Shorthands
    /**
     * Evaluate yolan list closure into an integer
     * @param c the closure which must be a list
     * @param i the index into the list
     * @return the integer result of evaluating the i'th element of c
     */
    private static int ival(Object c, int i) {
        return ((Integer) ((Yolan) ((Object[]) c)[i]).value()).intValue();
    }

    /**
     * Evaluate yolan list closure into an object 
     * @param c the closure which must be a list
     * @param i the index into the list
     * @return the result of evaluating the i'th element of c
     */
    private static Object val(Object c, int i) {
        return ((Yolan) ((Object[]) c)[i]).value();
    }

    /**
     * Shorthand for wrapping a number into an Integer object
     * @param i the number
     * @return the Integer
     */
    private static Object num(int i) {
        return new Integer(i);
    }

    // Conversion to string
    /**
     * Convert an object to a string
     * @param sb the accumulator variable - the string i added to this buffer
     * @param o the object to convert
     * @return the accumulator (same as the paremeter)
     */
    private static StringBuffer to_string(StringBuffer sb, Object o) {
        if (o instanceof Object[]) {
            Object os[] = (Object[]) o;
            sb.append("[ ");
            for (int i = 0; i < os.length; i++) {
                to_string(sb, os[i]);
                sb.append(" ");
            }
            sb.append("]");
        } else if (o instanceof Yolan) {
            Yolan yl = (Yolan) o;
            sb.append("Yolan" + yl.fn + "(");
            to_string(sb, yl.c);
            sb.append(")");
        } else {
            sb.append(o.toString());
        }
        return sb;
    }
    
    // </editor-fold>

    
    ////////////////////////////////////
    // Adding native+builtin functions to the runtime
    //<editor-fold>
    
    /**
     * Register a builtin function
     * @param val the function id
     * @param name the name in the runtime
     */
    private static void addBuiltin(int val, String name) {
        int id = getVarId(name);
        vars[id] = new Yolan(FN_BUILTIN_DUMMY, new Integer(val));

    }

    /**
     * Register a native function
     * @param name the name of the function
     * @param f the function itself
     */
    public static void addFunction(String name, Yoco f) {
        int id = getVarId(name);
        vars[id] = new Yolan(FN_NATIVE_DUMMY, f);
    }

    // Register builtins
    static {
        addBuiltin(FN_ADD, "+");
        addBuiltin(FN_SUB, "-");
        addBuiltin(FN_MUL, "*");
        addBuiltin(FN_DIV, "/");
        addBuiltin(FN_LESS, "<");
        addBuiltin(FN_IF, "if");
        addBuiltin(FN_TO_STRING, "to-string");
        addBuiltin(FN_LAMBDA, "lambda");
        addBuiltin(FN_RESOLVE_SET, "set!");
        addBuiltin(FN_DO, "do");
        addBuiltin(FN_WHILE, "while");
        addBuiltin(FN_LESS_EQUAL, "<=");
        // ...
    }
    // </editor-fold>

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
                s.push(new Yolan(FN_LITERAL, sb.toString()));

            // Identifier
            } else {
                StringBuffer sb = new StringBuffer();
                while (c > ' ' && c != '[' && c != ']') {
                    sb.append((char) c);
                    c = is.read();
                }
                s.push(new Yolan(FN_RESOLVE_GET_VAR, sb.toString()));
            }
        }
        Object result[] = new Object[s.size()];
        s.copyInto(result);
        return new Yolan(FN_RESOLVE_EVAL_LIST, result);
    }

    // Parse and evaluate code from input stream
    public static Object eval(InputStream is) throws IOException {
        Object exprs[] = (Object[]) parse(is).c;
        Object result = null;
        //print(exprs);
        for (int i = 0; i < exprs.length; i++) {
            result = ((Yolan) exprs[i]).value();
        }
        return result;
    }
}
