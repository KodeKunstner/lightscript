package com.solsort.lightscript;

import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

public final class Util implements Function {
    //<editor-fold desc="constants">

    static final int ENUMERATION_ITERATOR = 22;

    /** new Object[0]. Defined as property for caching. */
    public static final Object emptyTuple[] = new Object[0];
    /** new Integer(1). Defined as property for caching. */
    public static final Integer integerOne = new Integer(1);
    /** new Integer(-1). Defined as property for caching. */
    public static final Integer integerMinusOne = new Integer(-1);
    //</editor-fold>

    //<editor-fold desc="utility-functions">
    /** Push a value into a stack if it is not already there */
    public static void stackPushUnique(Stack s, Object val) {
        if (s == null) {
            return;
        }
        int pos = s.indexOf(val);
        if (pos == -1) {
            pos = s.size();
            s.push(val);
        }
    }

    public static int tupleIndexOf(Object[] tuple, Object o) {
        return tupleIndexOf(tuple, o, 0);
    }

    public static int tupleIndexOf(Object[] tuple, Object o, int fromIndex) {
        if (tuple == null) {
            return -1;
        }
        for (int i = fromIndex; i < tuple.length; ++i) {
            if (tuple[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public static Object[] stackToTuple(Stack s) {
        if (s.empty()) {
            return emptyTuple;
        }
        Object[] result = new Object[s.size()];
        s.copyInto(result);
        return result;
    }

    public static void tupleCopyInto(Object tuple[], Stack stack) {
        stack.setSize(tuple.length);
        for (int i = tuple.length - 1; i >= 0; --i) {
            stack.setElementAt(tuple[i], i);
        }
    }

    static void convertToString(Object o, StringBuffer sb) {
        if (o instanceof Object[]) {
            String sep = "";
            Object[] os = (Object[]) o;

            sb.append("[");
            for (int i = 0; i < os.length; ++i) {
                sb.append(sep);
                convertToString(os[i], sb);
                sep = ", ";
            }
            sb.append("]");
        } else if (o instanceof Stack) {
            String sep = "";
            Stack s = (Stack) o;

            sb.append("[");
            for (int i = 0; i < s.size(); ++i) {
                sb.append(sep);
                convertToString(s.elementAt(i), sb);
                sep = ", ";
            }
            sb.append("]");
        } else if (o instanceof Hashtable) {
            String sep = "";
            Hashtable h = (Hashtable) o;
            sb.append("{");
            for (Enumeration e = h.keys(); e.hasMoreElements();) {
                Object key = e.nextElement();
                sb.append(sep);
                convertToString(key, sb);
                sb.append(": ");
                convertToString(h.get(key), sb);
                sep = ", ";
            }
            sb.append("}");
        } else if (o instanceof String) {
            sb.append("\"");
            sb.append(o);
            sb.append("\"");
        } else {
            sb.append(o);
        }
    }

    public static void qsort(Object tuple[], Function cmp, int first, int last) throws LightScriptException {
        Object args[] = {tuple, null, null};
        --last;
        while (first < last) {
            int l = first;
            int r = last;
            Object pivot = tuple[(l + r) / 2];
            tuple[(l + r) / 2] = tuple[r];
            tuple[r] = pivot;

            while (l < r) {
                --l;
                do {
                    ++l;
                    args[1] = tuple[l];
                    args[2] = pivot;
                } while (((Integer) cmp.apply(args, 0, 2)).intValue() <= 0 && l < r);
                if (l < r) {
                    tuple[r] = tuple[l];
                    r--;
                }
                ++r;
                do {
                    r--;
                    args[1] = pivot;
                    args[2] = tuple[r];
                } while (((Integer) cmp.apply(args, 0, 2)).intValue() <= 0 && l < r);
                if (l < r) {
                    tuple[l] = tuple[r];
                    l++;
                }
            }
            tuple[r] = pivot;
            qsort(tuple, cmp, l + 1, last + 1);
            last = l - 1;
        }
    }
    //</editor-fold>
    //<editor-fold desc="properties and constructors">
    private int fn;
    private Object closure;

    Util(int fn) {
        this.fn = fn;
    }

    Util(int fn, Object closure) {
        this.fn = fn;
        this.closure = closure;
    }
    //</editor-fold>

    public Object apply(Object[] args, int argpos, int argcount) throws LightScriptException {
        switch (fn) {
        case 0: { // Stack getter
            if (args[argpos + 1] instanceof Integer) {
                Stack s = (Stack) args[argpos];
                int pos = ((Integer) args[argpos + 1]).intValue();
                if (pos >= 0 && pos < s.size()) {
                    return s.elementAt(pos);
                } else {
                    return LightScript.UNDEFINED;
                }
            } else if ("length".equals(args[argpos + 1])) {
                return new Integer(((Stack) args[argpos]).size());
            } else {
                return ((Function) closure).apply(args, argpos, argcount);
            }
        }
        case 1: { // Stack __iter__
            int[] o = new int[2];
            o[0] = -1;
            o[1] = ((Stack) args[argpos]).size();
            return new Util(2, o);
        }
        case 2: { // Stack iterator object
            int[] is = (int[]) closure;
            ++is[0];
            if (is[0] < is[1]) {
                return new Integer(is[0]);
            } else {
                return LightScript.UNDEFINED;
            }
        }
        case 3: { // default add
            LightScript ls = (LightScript) closure;
            return ls.toString(args[argpos]) + ls.toString(args[argpos + 1]);
        }
        case 4: { // stack push
            return ((Stack) args[argpos]).push(args[argpos + 1]);
        }
        case 5: { // stack pop
            return ((Stack) args[argpos]).pop();
        }
        case 6: { // new Array
            return new Stack();
        }
        case 7: { // new String
            return "";
        }
        case 8: { // hashtable getter
            Hashtable h = (Hashtable) args[argpos];
            for (;;) {
                Object o = h.get(args[argpos + 1]);
                if (o != null) {
                    return o;
                }
                o = h.get("__proto__");
                if (o == null || !(h instanceof Hashtable)) {
                    return ((Function) closure).apply(args, argpos, argcount);
                }
                h = (Hashtable) o;
            }
        }
        case 9: { // hashtable setter
            Hashtable h = (Hashtable) args[argpos];
            h.put(args[argpos + 1], args[argpos + 2]);
            return h;
        }
        case 10: { // stack setter
            Stack s = (Stack) args[argpos];
            if (args[argpos + 1] instanceof Integer) {
                int pos = ((Integer) args[argpos + 1]).intValue();
                if (pos >= s.size()) {
                    int i = s.size();
                    s.setSize(pos + 1);
                    while (i < pos) {
                        s.setElementAt(LightScript.UNDEFINED, i);
                        ++i;
                    }
                }
                if (pos >= 0) {
                    s.setElementAt(args[argpos + 2], pos);
                }
            }
            return s;
        }
        case 11: { // lightscript global getter
            LightScript ls = (LightScript) ((Object[]) closure)[0];
            Object o = ls.get(args[argpos + 1]);
            if (o == null) {
                return ((Function) ((Object[]) closure)[1]).apply(args, argpos, argcount);
            }
            return o;
        }
        case 12: { // lightscript global setter
            LightScript ls = (LightScript) closure;
            ls.set(args[argpos + 1], args[argpos + 2]);
            return ls;
        }
        case 13: { // String.toInt
            return Integer.valueOf((String) args[argpos]);
        }
        case 14: { // Return constant, (new String)
            return closure;
        }
        case 15: { // print
            System.out.println(((LightScript) closure).toString(args[argpos + 1]));
            return args[argpos + 1];
        }
        case 16: { // toString
            return String.valueOf(args[argpos]);
        }
        case 17: { // clone
            Hashtable h = new Hashtable();
            h.put("__proto__", args[argpos + argcount]);
            return h;
        }
        case 18: { // typeof
            Object arg1 = args[argpos + 1];
            if (arg1 instanceof Hashtable) {
                return "object";
            } else if (arg1 instanceof Stack) {
                return "array";
            } else if (arg1 instanceof Integer) {
                return "number";
            } else if (arg1 == LightScript.UNDEFINED) {
                return "undefined";
            } else if (arg1 == LightScript.NULL) {
                return "null";
            } else if (arg1 == LightScript.TRUE || arg1 == LightScript.FALSE) {
                return "boolean";
            } else if (arg1 instanceof Object[]) {
                return "tuple";
            } else {
                return "builtin";
            }
        }
        case 19: { // hashtable hasOwnProperty
            return ((Hashtable) args[argpos]).containsKey(args[argpos + 1]) ? LightScript.TRUE : LightScript.FALSE;
        }
        case 20: { // array join
            StringBuffer sb = new StringBuffer();
            String sep = "";
            Stack s = (Stack) args[argpos];
            String sep2;
            if (argcount > 0) {
                sep2 = ((LightScript) closure).toString(args[argpos + 1]);
            } else {
                sep2 = ",";
            }
            for (int i = 0; i < s.size(); ++i) {
                sb.append(sep);
                sb.append(((LightScript) closure).toString(s.elementAt(i)));
                sep = sep2;
            }
            return sb.toString();
        }
        case 21: { // hashtable __iter__
            return new Util(ENUMERATION_ITERATOR, ((Hashtable) args[argpos]).keys());
        }
        case ENUMERATION_ITERATOR: { // iterator, should have an enumeration as closure
            Enumeration e = (Enumeration) closure;
            Object o;
            do {
                if (!e.hasMoreElements()) {
                    return LightScript.UNDEFINED;
                }
                o = e.nextElement();

                // skip the "__proto__" property
            } while ("__proto__".equals(o));
            return o;
        }
        case 23: { // parseint
            int base;
            LightScript ls = (LightScript) closure;
            if (argcount == 2) {
                base = ls.toInt(args[argpos + 2]);
            } else {
                base = 10;
            }
            String str = ls.toString(args[argpos + 1]);
            return Integer.valueOf(str, base);
        }
        case 24: { // identity, Integer.toInt
            return args[argpos];
        }
        case 25: { // array.concat
            LightScript ls = (LightScript) closure;
            Stack result = (Stack) ls.call("Array");
            for (int i = 0; i <= argcount; ++i) {
                if (!(i == 0 && args[argpos] instanceof Util)) {
                    Object o = args[argpos + i];
                    if (o instanceof Stack) {
                        Stack s = (Stack) o;
                        for (int j = 0; j < s.size(); ++j) {
                            result.push(s.elementAt(j));
                        }
                    } else {
                        result.push(o);
                    }
                }
            }
            return result;
        }
        case 26: { // StdLib.subscript
            LightScript ls = (LightScript) closure;
            Object inner = ((Util) args[argpos]).closure;
            return ls.callMethod(inner, "__getter__", args[argpos + 1]);
        }
        case 27: { // (string|array).slice
            LightScript ls = (LightScript) closure;
            Object o = args[argpos];
            int len = o instanceof Stack ? ((Stack) o).size() : ((String) o).length();
            int start = ls.toInt(args[argpos + 1]);
            if (start < 0) {
                start = len + start;
            }
            int end;
            if (argcount > 1) {
                end = ls.toInt(args[argpos + 2]);
            } else {
                end = len;
            }
            if (end < 0) {
                end = len + end;
            } else if (end > len) {
                end = len;
            }
            if (o instanceof Stack) {
                Stack s = (Stack) o;
                Stack result = (Stack) ls.call("Array");
                while (start < end) {
                    result.push(s.elementAt(start));
                    ++start;
                }
                return result;
            } else {
                return ((String) o).substring(start, end);
            }
        }
        case 28: { // new object
            return new Hashtable();
        }
        case 29: { // array.sort
            Stack s = (Stack) args[argpos];
            Function cmp;
            if (argcount > 0 && args[argpos + 1] instanceof Function) {
                cmp = (Function) args[argpos + 1];
            } else {
                cmp = new Util(30);
            }
            Object[] t = stackToTuple(s);
            qsort(t, cmp, 0, t.length);
            tupleCopyInto(t, s);
            return s;
        }
        case 30: { // default sort comparison
            return args[argpos + 1].toString().compareTo(args[argpos + 2].toString()) >= 0 ? integerOne : integerMinusOne;
        }
        case 31: { // string.charCodeAt
            LightScript ls = (LightScript) closure;
            return new Integer(((String) args[argpos]).charAt(ls.toInt(args[argpos + 1])));
        }
        case 32: { // string __getter__
            if (args[argpos + 1] instanceof Integer) {
                String s = (String) args[argpos];
                int pos = ((Integer) args[argpos + 1]).intValue();
                if (pos >= 0 && pos < s.length()) {
                    return String.valueOf(s.charAt(pos));
                } else {
                    return LightScript.UNDEFINED;
                }
            } else if ("length".equals(args[argpos + 1])) {
                return new Integer(((String) args[argpos]).length());
            } else {
                return ((Function) closure).apply(args, argpos, argcount);
            }
        }
        case 33: { // string.concat
            LightScript ls = (LightScript) closure;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i <= argcount; ++i) {
                if (!(i == 0 && args[argpos] instanceof Util)) {
                    sb.append(ls.toString(args[argpos + i]));
                }
            }
            return sb.toString();
        }
        case 34: { // string.fromCharCode
            LightScript ls = (LightScript) closure;
            return String.valueOf((char) ls.toInt(args[argpos + 1]));
        }
        case 35: { // Tuple
            Object result[] = new Object[argcount];
            for (int i = result.length - 1; i >= 0; --i) {
                result[i] = args[argpos + i + 1];
            }
            return result;
        }
        case 36: { // tuple.sort
            LightScript ls = (LightScript) closure;
            Object tuple[] = (Object[]) args[argpos];
            Function cmp;
            if (argcount > 0 && args[argpos + 1] instanceof Function) {
                cmp = (Function) args[argpos + 1];
            } else {
                cmp = new Util(30);
            }
            int start = 0;
            if (argcount > 1) {
                start = ls.toInt(args[argpos + 2]);
            }
            if (start < 0) {
                start = start + tuple.length;
            }
            int end = -1;
            if (argcount > 2) {
                end = ls.toInt(args[argpos + 2]);
            }
            if (end < 0) {
                end = end + tuple.length;
            }

            qsort(tuple, cmp, start, end);
            return tuple;
        }
        case 37: { // stack.toTuple
            return stackToTuple((Stack) args[argpos]);
        }
        case 38: { // tuple.toArray
            LightScript ls = (LightScript) closure;
            Stack result = (Stack)ls.call("Array");
            tupleCopyInto((Object[]) args[argpos], result);
            return result;
        }
        case 39: {
            Function ls[] = (Function[])closure;
            Object result = ls[0].apply(args, argpos, argcount);
            if (result == null) {
                result = ls[1].apply(args, argpos, argcount);
            }
            return result;
        }
        }
        return LightScript.UNDEFINED;
    }

    // TODO use newGetter for getters here
    static void setGetter(LightScript ls, Class cls, Function fn) {
        Function closure[] = { fn, ls.getDefaultGetter(cls) };
        ls.setMethod(cls, "__getter__", new Util(39, closure));
    }
    /*
    static void setGetter(LightScript ls, Class cls, Function fn) {
        Function closure[] = { fn, ls.getDefaultGetter() };
        ls.setMethod(cls, "__getter__", new StdLib(39, closure));
    }

     */
    static void register(LightScript ls) {

        ls.set("global", ls);
        Class globalClass = ls.getClass();
        Object ls_getter_args[] = {ls, ls.getMethod(globalClass, "__getter__")};
        ls.setMethod(globalClass, "__getter__", new Util(11, ls_getter_args));
        ls.setMethod(globalClass, "__setter__", new Util(12, ls));

        ls.setMethod(null, "+", new Util(3, ls));
        ls.setMethod(null, "toString", new Util(16));

        ls.set("print", new Util(15, ls));
        ls.set("parseint", new Util(23, ls));

        Hashtable object = new Hashtable();
        ls.set("Object", new Util(28, object));
        object.put("create", new Util(17));
        Class objectClass = (new Hashtable()).getClass();
        ls.setMethod(objectClass, "__getter__", new Util(8, ls.getMethod(objectClass, "__getter__")));
        ls.setMethod(objectClass, "__setter__", new Util(9, ls.getMethod(objectClass, "__setter__")));
        ls.setMethod(objectClass, "hasOwnProperty", new Util(19));
        ls.setMethod(objectClass, "__iter__", new Util(21));

        Hashtable array = new Hashtable();
        ls.set("Array", new Util(6, array));
        Class arrayClass = (new Stack()).getClass();
        ls.setMethod(arrayClass, "__getter__", new Util(0, ls.getMethod(arrayClass, "__getter__")));
        ls.setMethod(arrayClass, "__setter__", new Util(10, ls.getMethod(arrayClass, "__setter__")));
        ls.setMethod(arrayClass, "__iter__", new Util(1));
        ls.setMethod(arrayClass, "push", new Util(4));
        ls.setMethod(arrayClass, "pop", new Util(5));
        ls.setMethod(arrayClass, "join", new Util(20, ls));
        ls.setMethod(arrayClass, "slice", new Util(27, ls));
        ls.setMethod(arrayClass, "concat", new Util(25, ls));
        array.put("concat", new Util(25, ls));
        ls.setMethod(arrayClass, "sort", new Util(29));
        ls.setMethod(arrayClass, "toTuple", new Util(37));

        Hashtable string = new Hashtable();
        ls.set("String", new Util(7, string));
        Class stringClass = "".getClass();
        ls.setMethod(stringClass, "__getter__", new Util(32, ls.getMethod(stringClass, "__getter__")));
        ls.setMethod(stringClass, "toInt", new Util(13));
        ls.setMethod(stringClass, "slice", new Util(27, ls));
        ls.setMethod(stringClass, "charCodeAt", new Util(31, ls));
        ls.setMethod(stringClass, "concat", new Util(33, ls));
        string.put("fromCharCode", new Util(34, ls));

        Hashtable tuple = new Hashtable();
        ls.set("Tuple", new Util(35, tuple));
        Class tupleClass = emptyTuple.getClass();
        ls.setMethod(tupleClass, "sort", new Util(36, ls));
        ls.setMethod(tupleClass, "toArray", new Util(38, ls));

        Class numberClass = integerOne.getClass();
        ls.setMethod(numberClass, "toInt", new Util(24));

        Class stdlibClass = (new Util(0)).getClass();
        ls.setMethod(stdlibClass, "__getter__", new Util(26, ls));
    }
}


