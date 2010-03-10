/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solsort.lightscript;

//</editor-fold>
import java.util.Stack;
import java.util.Hashtable;

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
                Object o = h.get(args[argpos + 1]);
                if(o == null) {
                    return ((LightScriptFunction) closure).apply(args, argpos, argcount);
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
        }
        return LightScript.UNDEFINED;
    }

    public static void register(LightScript ls) {
        ls.set("Object", new StdLib(7));
        Class objectClass = (new Hashtable()).getClass();
        ls.setTypeMethod(objectClass, "__getter__", new StdLib(8, ls.getTypeMethod(objectClass, "__getter__")));
        ls.setTypeMethod(objectClass, "__setter__", new StdLib(9, ls.getTypeMethod(objectClass, "__setter__")));

        ls.set("Array", new StdLib(6));
        Class arrayClass = (new Stack()).getClass();
        ls.setTypeMethod(arrayClass, "__getter__", new StdLib(0, ls.getTypeMethod(arrayClass, "__getter__")));
        ls.setTypeMethod(arrayClass, "__setter__", new StdLib(10, ls.getTypeMethod(arrayClass, "__setter__")));
        ls.setTypeMethod(arrayClass, "__iter__", new StdLib(1));
        ls.setTypeMethod(arrayClass, "push", new StdLib(4));
        ls.setTypeMethod(arrayClass, "pop", new StdLib(5));

        ls.setTypeMethod(null, "+", new StdLib(3));

        Object ls_getter_args[] = {ls, ls.getTypeMethod(ls.getClass(), "__getter__")};
        ls.setTypeMethod(ls.getClass(), "__getter__", new StdLib(11, ls_getter_args));
        ls.setTypeMethod(ls.getClass(), "__setter__", new StdLib(12, ls));
    }
}


