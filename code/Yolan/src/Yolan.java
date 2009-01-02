
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
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
    private static final int FN_USER_DEFINED_FUNCTION = 10;
    private static final int FN_LAMBDA = 11;
    private static final int FN_RESOLVE_SET = 12;
    private static final int FN_DO = 13;
    private static final int FN_WHILE = 14;
    private static final int FN_LESS_EQUAL = 15;
    private static final int FN_GET_VAR = 16;
    private static final int FN_SET = 17;
    private static final int FN_NATIVE = 18;
    private static final int FN_REM = 19;
    private static final int FN_DEFUN = 20;
    private static final int FN_RANDOM = 21;
    private static final int FN_IS_INTEGER = 22;
    private static final int FN_FOREACH = 23;
    private static final int FN_STRINGJOIN = 24;
    private static final int FN_SUBSTRING = 25;
    private static final int FN_SIZE = 26;
    private static final int FN_STRINGORDER = 27;
    private static final int FN_IS_STRING = 28;
    private static final int FN_NOT = 29;
    private static final int FN_AND = 30;
    private static final int FN_OR = 31;
    private static final int FN_EQUALS = 32;
    private static final int FN_PUT = 34;
    private static final int FN_GET = 35;
    private static final int FN_LIST = 38;
    private static final int FN_RESIZE = 39;
    private static final int FN_PUSH = 40;
    private static final int FN_POP = 41;
    private static final int FN_IS_EMPTY = 42;
    private static final int FN_IS_STACK = 44;
    private static final int FN_DICT = 45;
    private static final int FN_KEYS = 46;
    private static final int FN_IS_DICT = 47;
    private static final int FN_GET_NEXT = 48;
    private static final int FN_ELEMENTS = 49;
    private static final int FN_RESOLVE_FOREACH = 50;
    private static final int FN_RESOLVE_LOCALS = 52;
    private static final int FN_LOCALS = 53;
    private static final int FN_REPEAT = 54;
    //</editor-fold>
    private static Random random = new Random();

    /**
     * The number of arguments the function takes, 
     * if the delayed computation is an applicable
     * user defined function.
     *
     * @return the number of parameters, or -1 if not a function
     */
    public int nargs() {
        if (fn != FN_USER_DEFINED_FUNCTION) {
            return -1;
        }
        return ((Object[]) c).length - 1;
    }

    /**
     * if the computation is a variable or literal,
     * return its string representation
     */
    public String string() {
        if (fn == FN_RESOLVE_GET_VAR) {
            return (String) c;
        } else if (fn == FN_GET_VAR) {
            return (String) vars[((Integer) c).intValue() - 1];
        } else if (fn == FN_LITERAL) {
            if (c instanceof Integer) {
                return c.toString();
            } else if (c instanceof String) {
                return "\"" + c + "\"";
            }
        }
        return null;
    }

    /**
     * utility function for function application
     */
    private Object doApply(int n) {
        if (n != nargs()) {
            for (int i = 0; i < n; i++) {
                stack.pop();
            }
            return null;
        }
        return value();
    }

    /**
     * apply as a function without arguments
     */
    public Object apply() {
        return doApply(0);
    }

    /**
     * apply as a function with one argument
     */
    public Object apply(Object arg1) {
        stack.push(arg1);
        return doApply(1);
    }

    /**
     * apply as a function with two arguments
     */
    public Object apply(Object arg1, Object arg2) {
        stack.push(arg1);
        stack.push(arg2);
        return doApply(2);
    }

    /**
     * apply as a function with three arguments
     */
    public Object apply(Object arg1, Object arg2, Object arg3) {
        stack.push(arg1);
        stack.push(arg2);
        stack.push(arg3);
        return doApply(3);
    }

    /**
     * apply as a function with the arguments given as an array
     */
    public Object apply(Object args[]) {
        for (int i = 0; i < args.length; i++) {
            stack.push(args[i]);
        }
        return doApply(args.length);
    }

    /**
     * Evaluate the delayed computation
     * @return the result
     */
    public Object value() {
        switch (fn) {

            case FN_LITERAL: {
                return c;
            }

            case FN_RESOLVE_GET_VAR: {
                fn = FN_GET_VAR;
                c = new Integer(getVarId(c));
                return value();
            }

            case FN_RESOLVE_EVAL_LIST: {
                Object o = val0();
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
                    } else if (yl.fn == FN_USER_DEFINED_FUNCTION) {
                        Object args[] = (Object[]) c;
                        // evaluate arguments and push to stack
                        for (int i = 1; i < args.length; i++) {
                            stack.push(((Yolan) args[i]).value());
                        }

                        return yl.value();
                    }
                }
                throw new Error("Unknown function: " + ((Yolan) ((Object[]) c)[0]).string());
            }

            case FN_ADD: {
                return num(ival(0) + ival(1));
            }

            case FN_SUB: {
                return num(ival(0) - ival(1));
            }

            case FN_MUL: {
                return num(ival(0) * ival(1));
            }

            case FN_DIV: {
                return num(ival(0) / ival(1));
            }

            case FN_REM: {
                return num(ival(0) % ival(1));
            }

            case FN_LESS: {
                return ival(0) < ival(1) ? TRUE : null;
            }

            case FN_IF: {
                return (val0() != null) ? val(1) : val(2);
            }

            case FN_TO_STRING: {
                return to_string(new StringBuffer(), val0()).toString();
            }

            case FN_USER_DEFINED_FUNCTION: {
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

            case FN_LAMBDA: {
                Object[] lambda_expr = (Object[]) c;
                Object[] arguments = (Object[]) ((Yolan) lambda_expr[0]).c;

                Object[] function = new Object[arguments.length + 1];
                function[0] = lambda_expr[1];
                for (int i = 0; i < arguments.length; i++) {
                    function[i + 1] = num(getVarId(((Yolan) arguments[i]).c));
                }

                return new Yolan(FN_USER_DEFINED_FUNCTION, function);
            }

            case FN_DEFUN: {
                Object[] lambda_expr = (Object[]) c;
                Object[] arguments = (Object[]) ((Yolan) lambda_expr[0]).c;

                Object[] function = new Object[arguments.length];
                function[0] = lambda_expr[1];
                for (int i = 1; i < arguments.length; i++) {
                    function[i] = num(getVarId(((Yolan) arguments[i]).c));
                }

                Yolan fnc = new Yolan(FN_USER_DEFINED_FUNCTION, function);
                setVar(((Yolan) arguments[0]).c, fnc);
                return fnc;
            }

            case FN_RESOLVE_SET: {
                Object t[] = (Object[]) c;
                t[0] = new Integer(getVarId(((Yolan) t[0]).c));
                fn = FN_SET;
                return value();
            }

            case FN_DO: {
                Object result = null;
                int stmts = ((Object[]) c).length;
                for (int i = 0; i < stmts; i++) {
                    result = val(i);
                }
                return result;
            }

            case FN_WHILE: {
                Object result = null;
                int stmts = ((Object[]) c).length;
                while (val0() != null) {
                    for (int i = 1; i < stmts; i++) {
                        result = val(i);
                    }
                }
                return result;
            }
            
            case FN_REPEAT: {
                int count = ival(0);
                Object result = null;
                int stmts = ((Object[]) c).length;
                for(int i = 0; i < count; i++) {
                    for (int j = 1; j < stmts; j++) {
                        result = val(j);
                    }
                }
                return result;
            }

            case FN_LESS_EQUAL: {
                return ival(0) <= ival(1) ? TRUE : null;
            }

            case FN_GET_VAR: {
                int id = ((Integer) c).intValue();
                Object x = vars[id];

                return x;
            }

            case FN_SET: {
                Object o = val(1);
                vars[((Integer) ((Object[]) c)[0]).intValue()] = o;
                return o;
            }

            case FN_NATIVE: {
                Object objs[] = (Object[]) c;
                return ((Function) objs[0]).apply((Yolan[]) objs[1]);

            }

            case FN_RANDOM: {
                Object o = val0();
                int rnd = random.nextInt() & 0x7fffffff;
                if(o instanceof Integer) {
                    return num(rnd % ((Integer)o).intValue());
                } else if(o instanceof Stack) {
                    Stack s = (Stack) o;
                    return s.elementAt(rnd % s.size());
                } else {
                    return null;
                }
                    
            }

            case FN_IS_INTEGER: {
                return val0() instanceof Integer ? TRUE : null;
            }

            case FN_IS_STACK: {
                return val0() instanceof Stack ? TRUE : null;
            }

            case FN_IS_DICT: {
                return val0() instanceof Hashtable ? TRUE : null;
            }

            case FN_SUBSTRING: {
                return ((String) val0()).substring(ival(1), ival(2));
            }

            case FN_IS_STRING: {
                return val0() instanceof String ? TRUE : null;
            }

            case FN_SIZE: {
                Object o = val0();
                if (o instanceof String) {
                    return num(((String) o).length());
                } else if (o instanceof Stack) {
                    return num(((Stack) o).size());
                } else if (o instanceof Hashtable) {
                    return num(((Hashtable) o).size());
                } else {
                    return null;
                }
            }

            case FN_STRINGORDER: {
                return ((String) val0()).compareTo((String) val(1)) <= 0
                        ? TRUE : null;
            }

            case FN_NOT: {
                return val0() == null ? TRUE : null;
            }

            case FN_AND: {
                return val0() == null ? null : val(1);
            }

            case FN_OR: {
                Object o = val0();
                return o == null ? val(1) : o;
            }

            case FN_EQUALS: {
                Object o = val0();

                return o != null && o.equals(val(1)) ? TRUE : null;
            }

            case FN_GET: {
                Object o = val0();
                if (o instanceof Stack) {
                    return ((Stack) o).elementAt(ival(1));
                } else if (o instanceof Hashtable) {
                    return ((Hashtable) o).get(val(1));
                } else {
                    return null;
                }
            }

            case FN_PUT: {
                Object o = val0();
                if (o instanceof Stack) {
                    ((Stack) o).setElementAt(val(2), ival(1));
                } else if (o instanceof Hashtable) {
                    ((Hashtable) o).put(val(1), val(2));
                }
                return null;
            }

            case FN_LIST: {
                int len = ((Object[]) c).length;
                Stack result = new Stack();
                for (int i = 0; i < len; i++) {
                    result.push(val(i));
                }
                return result;
            }

            case FN_RESIZE: {
                Stack s = (Stack) val0();
                s.setSize(ival(1));
                return s;
            }

            case FN_PUSH: {
                Stack s = (Stack) val0();
                s.push(val(1));
                return s;
            }

            case FN_POP: {
                return ((Stack) val0()).pop();
            }

            case FN_IS_EMPTY: {
                Object o = val0();
                return (o instanceof Stack ? (((Stack) o).empty())
                        : o instanceof Hashtable ? (((Hashtable) o).isEmpty())
                        : o instanceof Enumeration ? (!((Enumeration) o).hasMoreElements())
                        : false)
                        ? TRUE : null;
            }

            case FN_DICT: {
                int len = ((Object[]) c).length;
                Hashtable h = new Hashtable();
                for (int i = 0; i < len; i += 2) {
                    h.put(val(i), val(i + 1));
                }
                return h;
            }

            case FN_ELEMENTS: {
                Object o = val0();
                if (o instanceof Stack) {
                    return ((Stack) o).elements();
                } else if (o instanceof Hashtable) {
                    return ((Hashtable) o).elements();
                } else {
                    return null;
                }
            }

            case FN_GET_NEXT: {
                return ((Enumeration) val0()).nextElement();
            }

            case FN_KEYS: {
                return ((Hashtable) val0()).keys();
            }

            case FN_STRINGJOIN: {
                Object os[] = (Object[]) c;
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < os.length; i++) {
                    stringjoin(sb, ((Yolan) os[i]).value());
                }
                return sb.toString();
            }

            case FN_RESOLVE_FOREACH: {
                Object t[] = (Object[]) c;
                t[0] = new Integer(getVarId(((Yolan) t[0]).c));
                fn = FN_FOREACH;
                return value();
            }

            case FN_FOREACH: {
                Object os[] = (Object[]) c;
                int id = ((Integer) os[0]).intValue();
                Enumeration e = (Enumeration) val(1);
                Object result = null;
                stack.push(vars[id]);
                while (e.hasMoreElements()) {
                    vars[id] = e.nextElement();
                    result = val(2);
                }
                vars[id] = stack.pop();
                return result;
            }

            case FN_RESOLVE_LOCALS: {
                Object[] locals = (Object[]) ((Yolan) ((Object[]) c)[0]).c;
                int locals_id[] = new int[locals.length];
                for (int i = 0; i < locals.length; i++) {
                    locals_id[i] = getVarId(((Yolan) locals[i]).c);
                }
                ((Object[]) c)[0] = locals_id;
                fn = FN_LOCALS;
                return value();

            }

            case FN_LOCALS: {
                int ids[] = (int[]) ((Object[]) c)[0];
                int len = ((Object[]) c).length;
                for (int i = 0; i < ids.length; i++) {
                    stack.push(vars[ids[i]]);
                }
                Object result = null;
                for (int i = 1; i < len; i++) {
                    result = val(i);
                }
                for (int i = ids.length - 1; i >= 0; i--) {
                    vars[ids[i]] = stack.pop();
                }
                return result;
            }

            default: {
                throw new Error("Unexpected case " + fn);
            }
        }
    }

    private static void stringjoin(StringBuffer sb, Object o) {
        if (o instanceof Stack) {
            for (int i = 0; i < ((Stack) o).size(); i++) {
                stringjoin(sb, ((Stack) o).elementAt(i));
            }
        } else {
            sb.append(o.toString());
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
    /**
     * Returnable true value
     */
    private static final Boolean TRUE = new Boolean(true);
    // Support for dynamic scoped variables
    // <editor-fold>
    /**
     * An array containing alternating 
     * variable names and values
     */
    private static Object vars[] = new Object[0];
    /** Stack used for function calls */
    private static Stack stack = new Stack();
    private static int varsize = 0;

    /**
     * Lookup the id of a variable
     * @param key the name of the variable
     * @return the id which maps into the vars-array
     */
    private static int getVarId(Object key) {
        int i = 0;
        while (i < varsize && !vars[i].equals(key)) {
            i += 2;
        }

        if (i == varsize) {
            if (varsize == vars.length) {
                // grow the var array exponential, and make sure
                // that the size is divisible by 2
                Object objs[] = new Object[(varsize * 5 / 4 + 4) & ~1];
                System.arraycopy(vars, 0, objs, 0, varsize);
                vars = objs;
            }
            vars[i] = key;
            varsize += 2;
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
     * @param i the index into the list
     * @return the integer result of evaluating the i'th element of c
     */
    private int ival(int i) {
        return ((Integer) ((Yolan) ((Object[]) c)[i]).value()).intValue();
    }

    /**
     * Evaluate yolan list closure into an object 
     * @param i the index into the list
     * @return the result of evaluating the i'th element of c
     */
    private Object val(int i) {
        return ((Yolan) ((Object[]) c)[i]).value();
    }

    /**
     * Evaluate yolan list closure into an object 
     * @return the result of evaluating the i'th element of c
     */
    private Object val0() {
        return ((Yolan) ((Object[]) c)[0]).value();
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
    public static void addFunction(String name, Function f) {
        int id = getVarId(name);
        vars[id] = new Yolan(FN_NATIVE_DUMMY, f);
    }
    // Register builtins
    

    static {
        addBuiltin(FN_ADD, "+");
        addBuiltin(FN_SUB, "-");
        addBuiltin(FN_MUL, "*");
        addBuiltin(FN_DIV, "/");
        addBuiltin(FN_REM, "%");
        addBuiltin(FN_LESS, "<");
        addBuiltin(FN_IF, "if");
        addBuiltin(FN_TO_STRING, "debug-string");
        addBuiltin(FN_LAMBDA, "lambda");
        addBuiltin(FN_RESOLVE_SET, "set");
        addBuiltin(FN_DO, "do");
        addBuiltin(FN_WHILE, "while");
        addBuiltin(FN_LESS_EQUAL, "<=");
        addBuiltin(FN_DEFUN, "defun");
        addBuiltin(FN_RANDOM, "random");
        addBuiltin(FN_IS_INTEGER, "is-integer");
        addBuiltin(FN_STRINGJOIN, "stringjoin");
        addBuiltin(FN_SUBSTRING, "substring");
        addBuiltin(FN_SIZE, "size");
        addBuiltin(FN_STRINGORDER, "stringorder");
        addBuiltin(FN_IS_STRING, "is-string");
        addBuiltin(FN_NOT, "not");
        addBuiltin(FN_AND, "and");
        addBuiltin(FN_OR, "or");
        addBuiltin(FN_EQUALS, "equals");
        addBuiltin(FN_PUT, "put");
        addBuiltin(FN_GET, "get");
        addBuiltin(FN_LIST, "list");
        addBuiltin(FN_RESIZE, "resize");
        addBuiltin(FN_PUSH, "push");
        addBuiltin(FN_POP, "pop");
        addBuiltin(FN_IS_EMPTY, "is-empty");
        addBuiltin(FN_IS_STACK, "is-list");
        addBuiltin(FN_DICT, "dict");
        addBuiltin(FN_KEYS, "keys");
        addBuiltin(FN_IS_DICT, "is-dict");
        addBuiltin(FN_GET_NEXT, "get-next");
        addBuiltin(FN_ELEMENTS, "elements");
        addBuiltin(FN_RESOLVE_FOREACH, "foreach");
        addBuiltin(FN_RESOLVE_LOCALS, "locals");
        addBuiltin(FN_REPEAT, "repeat");
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
