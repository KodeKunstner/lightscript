/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solsort.mobile;

//</editor-fold>
import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *
 * @author rje
 */
class StdLib implements LightScriptFunction {

    //<editor-fold desc="properties and constructors">
    private int fn;
    private Object closure;

    private StdLib(int fn) {
        this.fn = fn;
    }

    private StdLib(int fn, Object closure) {
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
                    return ((LightScriptFunction) closure).apply(args, argpos, argcount);
                }
            }
            case 1: { // Stack __iter__
                int[] o = new int[2];
                o[0] = -1;
                o[1] = ((Stack) args[argpos]).size();
                return new StdLib(2, o);
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
                return args[argpos].toString() + args[argpos + 1].toString();
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
                    o = h.get("__prototype__");
                    if (o == null || !(h instanceof Hashtable)) {
                        return ((LightScriptFunction) closure).apply(args, argpos, argcount);
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
                    return ((LightScriptFunction) ((Object[]) closure)[1]).apply(args, argpos, argcount);
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
                return args[argpos].toString();
            }
            case 17: { // clone
                Hashtable h = new Hashtable();
                h.put("__prototype__", args[argpos + argcount]);
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
                return new StdLib(22, ((Hashtable) args[argpos]).keys());
            }
            case 22: { // hashtable iterator
                Enumeration e = (Enumeration) closure;
                Object o;
                do {
                    if (!e.hasMoreElements()) {
                        return LightScript.UNDEFINED;
                    }
                    o = e.nextElement();
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
                Stack result = new Stack();
                for (int i = 0; i <= argcount; ++i) {
                    if (!(i == 0 && args[argpos] instanceof StdLib)) {
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
                Object inner = ((StdLib) args[argpos]).closure;
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
                    Stack result = new Stack();
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
                LightScriptFunction cmp;
                if (argcount > 0 && args[argpos + 1] instanceof LightScriptFunction) {
                    cmp = (LightScriptFunction) args[argpos + 1];
                } else {
                    cmp = new StdLib(30);
                }
                qsort(s, 0, s.size() - 1, cmp);
                return s;
            }
            case 30: { // default sort comparison
                return args[argpos + 1].toString().compareTo(args[argpos + 2].toString()) >= 0 ? one : minusOne;
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
                    return ((LightScriptFunction) closure).apply(args, argpos, argcount);
                }
            }
            case 33: { // string.concat
                LightScript ls = (LightScript) closure;
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i <= argcount; ++i) {
                    if (!(i == 0 && args[argpos] instanceof StdLib)) {
                        sb.append(ls.toString(args[argpos + i]));
                    }
                }
                return sb.toString();
            }
            case 34: { // string.fromCharCode
                LightScript ls = (LightScript) closure;
                return String.valueOf((char) ls.toInt(args[argpos + 1]));
            }
        }
        return LightScript.UNDEFINED;
    }
    private static final Integer one = new Integer(1);
    private static final Integer minusOne = new Integer(-1);

    public static void register(LightScript ls) {

        ls.setMethod(null, "+", new StdLib(3));
        ls.setMethod(null, "toString", new StdLib(16));

        ls.set("print", new StdLib(15, ls));
        ls.set("clone", new StdLib(17));
        ls.set("parseint", new StdLib(23, ls));

        Hashtable object = new Hashtable();
        ls.set("Object", new StdLib(28, object));
        Class objectClass = (new Hashtable()).getClass();
        ls.setMethod(objectClass, "__getter__", new StdLib(8, ls.getMethod(objectClass, "__getter__")));
        ls.setMethod(objectClass, "__setter__", new StdLib(9, ls.getMethod(objectClass, "__setter__")));
        ls.setMethod(objectClass, "hasOwnProperty", new StdLib(19));
        ls.setMethod(objectClass, "__iter__", new StdLib(21));

        Hashtable array = new Hashtable();
        ls.set("Array", new StdLib(6, array));
        Class arrayClass = (new Stack()).getClass();
        ls.setMethod(arrayClass, "__getter__", new StdLib(0, ls.getMethod(arrayClass, "__getter__")));
        ls.setMethod(arrayClass, "__setter__", new StdLib(10, ls.getMethod(arrayClass, "__setter__")));
        ls.setMethod(arrayClass, "__iter__", new StdLib(1));
        ls.setMethod(arrayClass, "push", new StdLib(4));
        ls.setMethod(arrayClass, "pop", new StdLib(5));
        ls.setMethod(arrayClass, "join", new StdLib(20, ls));
        ls.setMethod(arrayClass, "slice", new StdLib(27, ls));
        ls.setMethod(arrayClass, "concat", new StdLib(25, ls));
        array.put("concat", new StdLib(25, ls));
        ls.setMethod(arrayClass, "sort", new StdLib(29));

        Hashtable string = new Hashtable();
        ls.set("String", new StdLib(7, string));
        Class stringClass = "".getClass();
        ls.setMethod(stringClass, "__getter__", new StdLib(32, ls.getMethod(stringClass, "__getter__")));
        ls.setMethod(stringClass, "toInt", new StdLib(13));
        ls.setMethod(stringClass, "slice", new StdLib(27, ls));
        ls.setMethod(stringClass, "charCodeAt", new StdLib(31, ls));
        ls.setMethod(stringClass, "concat", new StdLib(33, ls));
        string.put("fromCharCode", new StdLib(34, ls));

        ls.set("global", ls);
        Class globalClass = ls.getClass();
        Object ls_getter_args[] = {ls, ls.getMethod(globalClass, "__getter__")};
        ls.setMethod(globalClass, "__getter__", new StdLib(11, ls_getter_args));
        ls.setMethod(globalClass, "__setter__", new StdLib(12, ls));

        Class numberClass = (new Integer(0)).getClass();
        ls.setMethod(numberClass, "toInt", new StdLib(24));

        Class stdlibClass = (new StdLib(0)).getClass();
        ls.setMethod(stdlibClass, "__getter__", new StdLib(26, ls));
    }

    private static void qsort(Stack arr, int first, int last, LightScriptFunction cmp) throws LightScriptException {
        Object args[] = {arr, null, null};
        while (first < last) {
            int l = first;
            int r = last;
            Object pivot = arr.elementAt((l + r) / 2);
            arr.setElementAt(arr.elementAt(r), (l + r) / 2);
            arr.setElementAt(pivot, r);

            while (l < r) {
                --l;
                do {
                    ++l;
                    args[1] = arr.elementAt(l);
                    args[2] = pivot;
                } while (((Integer) cmp.apply(args, 0, 2)).intValue() <= 0 && l < r);
                if (l < r) {
                    arr.setElementAt(arr.elementAt(l), r);
                    r--;
                }
                ++r;
                do {
                    r--;
                    args[1] = pivot;
                    args[2] = arr.elementAt(r);
                } while (((Integer) cmp.apply(args, 0, 2)).intValue() <= 0 && l < r);
                if (l < r) {
                    arr.setElementAt(arr.elementAt(r), l);
                    l++;
                }
            }
            arr.setElementAt(pivot, r);
            qsort(arr, l + 1, last, cmp);
            last = l - 1;
        }
    }
}


