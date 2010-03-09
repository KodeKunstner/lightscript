/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.solsort.mobile;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

class StdLib implements LightScriptFunction, LightScriptObject {

    private static final int EC_GLOBALS = 0;
    private static final int EC_OBJECT_PROTOTYPE = 1;
    private static final int EC_ARRAY_PROTOTYPE = 2;
    private static final int EC_FUNCTION_PROTOTYPE = 3;
    private static final int EC_STRING_PROTOTYPE = 4;
    /* Index in executionContext for the default setter function,
     * which is called when a property is set on
     * an object which is neither a Stack, Hashtable nor LightScriptObject
     *
     * The apply method of the setter gets the container as thisPtr,
     * and takes the key and value as arguments
     */
    private static final int EC_SETTER = 5;
    /* Index in executionContext for the default getter function,
     * called when subscripting an object
     * which is not a Stack, Hashtable, String nor LightScriptObject
     * or when the subscripting of any of those objects returns null.
     * (non-integer on stacks/strings, keys not found in Hashtable or
     * its prototypes, when LightScriptObject.get returns null)
     *
     * The apply method of the getter gets the container as thisPtr,
     * and takes the key as argument
     */
    private static final int EC_GETTER = 6;
    private static final int EC_WRAPPED_GLOBALS = 7;
    private static final int EC_NEW_ITER = 8;

    private int id;
    private Object closure[];
    private static Random rnd = new Random();
    // globally named functions
    private static final int STD_PRINT = (0);
    private static final int STD_TYPEOF = (1);
    private static final int STD_PARSEINT = (2);
    private static final String[] names = {"print", "gettype", "parseint"};
    // methods and other stuff added manually to lightscript
    private static final int STD_GLOBALLY_NAMED = (3);
    private static final int STD_HAS_OWN_PROPERTY = (STD_GLOBALLY_NAMED + 0);
    private static final int STD_ARRAY_PUSH = (STD_GLOBALLY_NAMED + 1);
    private static final int STD_ARRAY_POP = (STD_GLOBALLY_NAMED + 2);
    private static final int STD_ARRAY_JOIN = (STD_GLOBALLY_NAMED + 3);
    private static final int STD_DEFAULT_SETTER = (STD_GLOBALLY_NAMED + 4);
    private static final int STD_DEFAULT_GETTER = (STD_GLOBALLY_NAMED + 5);
    private static final int STD_NEW_ITERATOR = (STD_GLOBALLY_NAMED + 6);
    private static final int STD_INTEGER_ITERATOR = (STD_GLOBALLY_NAMED + 7);
    private static final int STD_ENUMERATION_ITERATOR = (STD_GLOBALLY_NAMED + 8);
    private static final int STD_GLOBAL_WRAPPER = (STD_GLOBALLY_NAMED + 9);
    private static final int STD_OBJECT_CONSTRUCTOR = (STD_GLOBALLY_NAMED + 10);
    private static final int STD_ARRAY_CONSTRUCTOR = (STD_GLOBALLY_NAMED + 11);
    private static final int STD_ARRAY_CONCAT = (STD_GLOBALLY_NAMED + 12);
    private static final int STD_ARRAY_SORT = (STD_GLOBALLY_NAMED + 13);
    private static final int STD_ARRAY_SLICE = (STD_GLOBALLY_NAMED + 14);
    private static final int STD_STRING_CHARCODEAT = (STD_GLOBALLY_NAMED + 15);
    private static final int STD_STRING_FROMCHARCODE = (STD_GLOBALLY_NAMED + 16);
    private static final int STD_STRING_CONCAT = (STD_GLOBALLY_NAMED + 17);
    private static final int STD_STRING_SLICE = (STD_GLOBALLY_NAMED + 18);
    private static final int STD_CLONE = (STD_GLOBALLY_NAMED + 19);
    private static final int STD_RANDOM = (STD_GLOBALLY_NAMED + 20);
    private static final int STD_FLOOR = (STD_GLOBALLY_NAMED + 21);
    private static final int STD_TO_STRING = (STD_GLOBALLY_NAMED + 22);
    private static final int[] argcs = {1, 1, 2 // not named
        , 0, 1, 0, 1 // hasown, push, pop, join
        , 2, 1 // default- setter getter
        , 0, 0, 0 // new iter int-iter enum-iter
        , 0 // globalwrapper
        , 0, -1 // object-constructor, array-constructor
        , -1, 1, 2 // array-concat, sort, slice,
        , 1, 1, -1, 2 // charcodeat, fromcharcode, strconcat, string_slice
        , 1, 0, 1 // clone, random, floor
        , 0// toString
    };

    public void set(Object key, Object value) {
        if (id == STD_GLOBAL_WRAPPER) {
            Object[] box = (Object[]) ((Hashtable) closure[EC_GLOBALS]).get(key);
            if (box == null) {
                box = new Object[1];
                ((Hashtable) closure[EC_GLOBALS]).put(key, box);
            }
            box[0] = value;
        }
    }

    public Object get(Object key) {
        if (id == STD_GLOBAL_WRAPPER) {
            Object[] box = (Object[]) ((Hashtable) closure[EC_GLOBALS]).get(key);
            if (box == null) {
                return null;
            } else {
                return box[0];
            }
        } else if ("length".equals(key)) {
            return new Integer(argcs[id]);
        } else {

            return null;
        }
    }

    private static Hashtable clone(Object o) {
        Hashtable result = new Hashtable();
        result.put("__proto__", o);
        return result;
    }

    private StdLib(int id) {
        this.id = id;
    }

    public Object apply(Object[] args, int argpos, int argcount) throws LightScriptException {
        Object thisPtr = args[argpos];
        Object arg1 = argcount < 1 ? LightScript.UNDEFINED : args[argpos + 1];
        Object arg2 = argcount < 2 ? LightScript.UNDEFINED : args[argpos + 2];
        if (argcs[id] >= 0 && argcount != argcs[id]) {
            throw new LightScriptException("Error: Wrong number of arguments");
        }
        switch (id) {
            case STD_PRINT: {
                System.out.println(arg1);
                break;
            }
            case STD_TYPEOF: {
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
                    return "const_array";
                } else {
                    return "builtin";
                }
            }
            case STD_PARSEINT: {
                return Integer.valueOf(arg1.toString(), ((Integer) arg2).intValue());
            }
            case STD_CLONE: {
                return clone((Hashtable) arg1);
            }
            case STD_HAS_OWN_PROPERTY: {
                if (thisPtr instanceof Hashtable) {
                    return ((Hashtable) thisPtr).contains(arg1)
                            ? LightScript.TRUE
                            : LightScript.FALSE;
                }
                break;
            }
            case STD_ARRAY_PUSH: {
                ((Stack) thisPtr).push(arg1);
                break;
            }
            case STD_ARRAY_POP: {
                ((Stack) thisPtr).pop();
                break;
            }
            case STD_ARRAY_JOIN: {
                Stack s = (Stack) thisPtr;
                if (s.size() == 0) {
                    return "";
                }
                StringBuffer sb = new StringBuffer();
                sb.append(s.elementAt(0).toString());
                String sep = arg1.toString();
                for (int i = 1; i < s.size(); i++) {
                    sb.append(sep);
                    sb.append(s.elementAt(i));
                }
                return sb.toString();
            }
            case STD_DEFAULT_SETTER: {
                if (thisPtr instanceof Object[] && arg1 instanceof Integer) {
                    ((Object[]) thisPtr)[((Integer) arg1).intValue()] = arg2;
                    break;
                }
                // implementation like "thisPtr[key] = val"
                break;
            }
            case STD_DEFAULT_GETTER: {
                if (thisPtr instanceof Object[]) {
                    if (arg1 instanceof Integer) {
                        return ((Object[]) thisPtr)[((Integer) arg1).intValue()];
                    } else if ("length".equals(arg1)) {
                        return new Integer(((Object[]) thisPtr).length);
                    } else if (((Hashtable) closure[0]).containsKey(arg1)) {
                        return ((Hashtable) closure[0]).get(arg1);
                    }
                }

                // implementation like "return thisPtr[key]"
                break;
            }
            case STD_NEW_ITERATOR: {
                if (thisPtr instanceof Hashtable) {
                    StdLib result;
                    result = new StdLib(STD_ENUMERATION_ITERATOR);
                    result.closure = new Object[1];
                    result.closure[0] = ((Hashtable) thisPtr).keys();
                    return result;
                }
                if (thisPtr instanceof Stack) {
                    StdLib result;
                    result = new StdLib(STD_INTEGER_ITERATOR);
                    result.closure = new Object[2];
                    result.closure[0] = new Integer(-1);
                    result.closure[1] = new Integer(((Stack) thisPtr).size() - 1);
                    return result;
                }
                break;
            }
            case STD_INTEGER_ITERATOR: {
                if (closure[0].equals(closure[1])) {
                    return LightScript.UNDEFINED;
                }
                int current = ((Integer) closure[0]).intValue();
                current = current + 1;
                Object result = new Integer(current);
                closure[0] = result;
                return result;
            }
            case STD_ENUMERATION_ITERATOR: {
                Enumeration e = (Enumeration) closure[0];
                if (!e.hasMoreElements()) {
                    return LightScript.UNDEFINED;
                }
                return e.nextElement();
            }
            case STD_GLOBAL_WRAPPER: {
                break;
            }
            case STD_OBJECT_CONSTRUCTOR: {
                if (thisPtr instanceof Hashtable) {
                    Hashtable result = new Hashtable();
                    Object prototype = ((Hashtable) thisPtr).get("__proto__");
                    if (prototype != null) {
                        result.put("__proto__", prototype);
                    }
                    return result;
                }
                break;
            }
            case STD_ARRAY_CONSTRUCTOR: {
                Stack result = new Stack();
                for (int i = 1; i <= argcount; ++i) {
                    result.push(args[argpos + i]);
                }
                return result;
            }
            case STD_ARRAY_CONCAT: {
                Stack result = new Stack();
                for (int i = 1; i <= argcount; ++i) {
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
                return result;
            }
            case STD_ARRAY_SORT: {
                Stack s = (Stack) thisPtr;
                Util.qsort(s, 0, s.size() - 1, (LightScriptFunction) arg1);
                return thisPtr;
            }
            case STD_ARRAY_SLICE: {
                int i = ((Integer) arg1).intValue();
                int j = ((Integer) arg2).intValue();
                Stack result = new Stack();
                Stack s = (Stack) thisPtr;
                while (i < j) {
                    result.push(s.elementAt(i));
                    ++i;
                }
                return result;
            }
            case STD_STRING_CHARCODEAT: {
                return new Integer(((String) thisPtr).charAt(((Integer) arg1).intValue()));
            }
            case STD_STRING_FROMCHARCODE: {
                return String.valueOf((char) ((Integer) arg1).intValue());
            }
            case STD_STRING_CONCAT: {

                StringBuffer sb = new StringBuffer();
                for (int i = 1; i <= argcount; ++i) {
                    sb.append(args[argpos + i].toString());
                }
                return sb.toString();
            }
            case STD_STRING_SLICE: {
                int i = ((Integer) arg1).intValue();
                int j = ((Integer) arg2).intValue();
                return ((String) thisPtr).substring(i, j);
            }
            case STD_RANDOM: {
                return new FixedPoint(0xffffffffl & rnd.nextInt());
            }
            case STD_FLOOR: {
                return new Integer(LightScript.toInt(arg1));
            }
            case STD_TO_STRING: {
                StringBuffer sb = new StringBuffer();
                Util.convertToString(thisPtr, sb);
                return sb.toString();
            }
        }
        return LightScript.UNDEFINED;
    }

    public static void register(LightScript ls) {

        Hashtable objectPrototype = new Hashtable();
        ls.executionContext[EC_OBJECT_PROTOTYPE] = objectPrototype;

        Hashtable arrayPrototype = new Hashtable();
        ls.executionContext[EC_ARRAY_PROTOTYPE] = arrayPrototype;

        Hashtable stringPrototype = clone(objectPrototype);
        ls.executionContext[EC_STRING_PROTOTYPE] = stringPrototype;

        Hashtable functionPrototype = clone(objectPrototype);
        ls.executionContext[EC_FUNCTION_PROTOTYPE] = functionPrototype;

        ls.executionContext[EC_SETTER] = new StdLib(STD_DEFAULT_SETTER);


        StdLib defaultGetter = new StdLib(STD_DEFAULT_GETTER);
        defaultGetter.closure = new Object[1];
        defaultGetter.closure[0] = objectPrototype;
        ls.executionContext[EC_GETTER] = defaultGetter;

        ls.executionContext[EC_NEW_ITER] = new StdLib(STD_NEW_ITERATOR);

        StdLib globalWrapper = new StdLib(STD_GLOBAL_WRAPPER);
        globalWrapper.closure = ls.executionContext;
        ls.executionContext[EC_WRAPPED_GLOBALS] = globalWrapper;


        for (int i = 0; i < names.length; i++) {
            ls.set(names[i], new StdLib(i));
        }

        objectPrototype.put("hasOwnProperty", new StdLib(STD_HAS_OWN_PROPERTY));
        objectPrototype.put("toString", new StdLib(STD_TO_STRING));
        Hashtable object = clone(objectPrototype);
        object.put("create", new StdLib(STD_CLONE));

        // Create members for array
        arrayPrototype.put("push", new StdLib(STD_ARRAY_PUSH));
        arrayPrototype.put("pop", new StdLib(STD_ARRAY_POP));
        arrayPrototype.put("join", new StdLib(STD_ARRAY_JOIN));
        Hashtable array = clone(arrayPrototype);

        Hashtable string = clone(stringPrototype);

        Hashtable function = clone(stringPrototype);

        Hashtable math = clone(objectPrototype);
        math.put("random", new StdLib(STD_RANDOM));
        math.put("floor", new StdLib(STD_FLOOR));

        objectPrototype.put("constructor", new StdLib(STD_OBJECT_CONSTRUCTOR));
        arrayPrototype.put("constructor", new StdLib(STD_ARRAY_CONSTRUCTOR));
        array.put("concat", new StdLib(STD_ARRAY_CONCAT));
        arrayPrototype.put("sort", new StdLib(STD_ARRAY_SORT));
        arrayPrototype.put("slice", new StdLib(STD_ARRAY_SLICE));
        stringPrototype.put("slice", new StdLib(STD_STRING_SLICE));
        stringPrototype.put("charCodeAt", new StdLib(STD_STRING_CHARCODEAT));
        string.put("fromCharCode", new StdLib(STD_STRING_FROMCHARCODE));
        string.put("concat", new StdLib(STD_STRING_CONCAT));

        ls.set("Object", object);
        ls.set("String", string);
        ls.set("Array", array);
        ls.set("Function", function);
        ls.set("Math", math);
    }
}
