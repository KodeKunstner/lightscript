/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solsort.lightscript;

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
                o[1] = ((Stack)args[argpos]).size();
                return new StdLib(2, o);
            }
            case 2: { // Stack iterator object
                int[] is = (int[])closure;
                ++is[0];
                if(is[0] < is[1]) {
                    return new Integer(is[0]);
                } else {
                    return LightScript.UNDEFINED;
                }
            }
            case 3: { // default add
                return args[argpos].toString() + args[argpos+1].toString();
            }
            case 4: { // stack push
                return ((Stack)args[argpos]).push(args[argpos+1]);
            }
            case 5: { // stack pop
                return ((Stack)args[argpos]).pop();
            }
            case 6: { // new Array
                return new Stack();
            }
            case 7: { // new Object
                return new Hashtable();
            }
            case 8: { // hashtable getter
                Hashtable h = (Hashtable) args[argpos];
                for(;;) {
                    Object o = h.get(args[argpos + 1]);
                    if(o != null) {
                        return o;
                    }
                    o = h.get("__prototype__");
                    if(o == null || ! (h instanceof Hashtable)) {
                        return ((LightScriptFunction) closure).apply(args, argpos, argcount);
                    }
                    h = (Hashtable)o;
                }
            }
            case 9: { // hashtable setter
                Hashtable h = (Hashtable) args[argpos];
                h.put(args[argpos + 1], args[argpos + 2]);
                return h;
            }
            case 10: { // stack setter
                Stack s = (Stack) args[argpos];
                if(args[argpos + 1] instanceof Integer) {
                    int pos = ((Integer) args[argpos+1]).intValue();
                    if(pos >= s.size()) {
                        int i = s.size();
                        s.setSize(pos +1);
                        while(i < pos) {
                            s.setElementAt(LightScript.UNDEFINED, i);
                            ++i;
                        }
                    }
                    if(pos >= 0) {
                        s.setElementAt(args[argpos+2], pos);
                    }
                }
                return s;
            }
            case 11: { // lightscript global getter
                LightScript ls = (LightScript)((Object [])closure)[0];
                Object o = ls.get(args[argpos + 1]);
                if(o == null) {
                    return ((LightScriptFunction) ((Object[])closure)[1]).apply(args, argpos, argcount);
                }
                return o;
            }
            case 12: { // lightscript global setter
                LightScript ls = (LightScript)closure;
                ls.set(args[argpos+1], args[argpos +2]);
                return ls;
            }
            case 13: { // String.toInt
                return Integer.valueOf((String)args[argpos]);
            }
            case 14: { // Return constant, (new String)
                return closure;
            }
            case 15: { // print
                System.out.println(((LightScript)closure).callMethod(args[argpos+1],"toString"));
                return args[argpos+1];
            }
            case 16: { // toString
                return args[argpos].toString();
            }
            case 17: { // clone
                Hashtable h = new Hashtable();
                h.put("__prototype__", args[argpos+1]);
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
                return ((Hashtable)args[argpos]).containsKey(args[argpos+1])?LightScript.TRUE:LightScript.FALSE;
            }
            case 20: { // array join
                StringBuffer sb = new StringBuffer();
                String sep = "";
                Stack s = (Stack)args[argpos];
                String sep2;
                if(argcount > 0) {
                    sep2 = (String)((LightScript)closure).callMethod(args[argpos+1], "toString");
                } else {
                    sep2 = ",";
                }
                for(int i=0; i<s.size(); ++i) {
                    sb.append(sep);
                    sb.append((String)((LightScript)closure).callMethod(s.elementAt(i), "toString"));
                    sep = sep2;
                }
                return sb.toString();
            }
            case 21: { // hashtable __iter__
                return new StdLib(22, ((Hashtable)args[argpos]).keys());
            }
            case 22: { // hashtable iterator
                Enumeration e = (Enumeration)closure;
                Object o;
                do {
                    if(!e.hasMoreElements()) {
                        return LightScript.UNDEFINED;
                    }
                    o = e.nextElement();
                } while("__proto__".equals(o));
                return o;
            }
            case 23: { // parseint
                int base;
                LightScript ls = (LightScript)closure;
                if(argcount == 2) {
                    base = ((Integer)ls.callMethod(args[argpos + 2], "toInt")).intValue();
                } else {
                    base = 10;
                }
                String str = (String)ls.callMethod(args[argpos + 1], "toString");
                return Integer.valueOf(str, base);
            }
            case 24: { // identity, Integer.toInt
                return args[argpos];
            }
            // TODO:
            // array.concat
            // array.sort
            // array.slice
            // string.charcodeat
            // string.fromchar
            // string.concat
            // string getter
            // string slice
        }
        return LightScript.UNDEFINED;
    }

    public static void register(LightScript ls) {

        ls.setMethod(null, "+", new StdLib(3));
        ls.setMethod(null, "toString", new StdLib(16));

        ls.set("print", new StdLib(15));
        ls.set("clone", new StdLib(17));
        ls.set("parseint", new StdLib(23, ls));

        ls.set("Object", new StdLib(7));
        Class objectClass = (new Hashtable()).getClass();
        ls.setMethod(objectClass, "__getter__", new StdLib(8, ls.getMethod(objectClass, "__getter__")));
        ls.setMethod(objectClass, "__setter__", new StdLib(9, ls.getMethod(objectClass, "__setter__")));
        ls.setMethod(objectClass, "hasOwnProperty", new StdLib(19));
        ls.setMethod(objectClass, "__iter__", new StdLib(21));

        ls.set("Array", new StdLib(6));
        Class arrayClass = (new Stack()).getClass();
        ls.setMethod(arrayClass, "__getter__", new StdLib(0, ls.getMethod(arrayClass, "__getter__")));
        ls.setMethod(arrayClass, "__setter__", new StdLib(10, ls.getMethod(arrayClass, "__setter__")));
        ls.setMethod(arrayClass, "__iter__", new StdLib(1));
        ls.setMethod(arrayClass, "push", new StdLib(4));
        ls.setMethod(arrayClass, "pop", new StdLib(5));
        ls.setMethod(arrayClass, "join", new StdLib(20, ls));

        ls.set("String", new StdLib(14, ""));
        Class stringClass = "".getClass();
        ls.setMethod(stringClass, "toInt", new StdLib(13));

        ls.set("global", ls);
        Class globalClass = ls.getClass();
        Object ls_getter_args[] = {ls, ls.getMethod(globalClass, "__getter__")};
        ls.setMethod(globalClass, "__getter__", new StdLib(11, ls_getter_args));
        ls.setMethod(globalClass, "__setter__", new StdLib(12, ls));

        Class numberClass = (new Integer(0)).getClass();
        ls.setMethod(numberClass, "toInt", new StdLib(24));
    }
}


