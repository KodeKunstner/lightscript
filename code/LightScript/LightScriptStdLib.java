import java.util.Stack;
import java.util.Hashtable; 
import java.util.Enumeration; 

class LightScriptStdLib implements LightScriptFunction {
    private int id;
    private Object closure[];

    // globally named functions
    private static final int PRINT = 0;
    private static final int TYPEOF = 1;
    private static final int PARSEINT = 2;
    private static final int CLONE = 3;

    private static final String[] names = {"print", "gettype", "parseint", "clone"};
    // methods and other stuff added manually to lightscript
    private static final int GLOBALLY_NAMED = 4;
    private static final int HAS_OWN_PROPERTY = GLOBALLY_NAMED + 0;
    private static final int ARRAY_PUSH = GLOBALLY_NAMED + 1;
    private static final int ARRAY_POP = GLOBALLY_NAMED + 2;
    private static final int ARRAY_JOIN = GLOBALLY_NAMED + 3;
    private static final int DEFAULT_SETTER = GLOBALLY_NAMED + 4;
    private static final int DEFAULT_GETTER = GLOBALLY_NAMED + 5;
    private static final int NEW_ITERATOR = GLOBALLY_NAMED + 6;
    private static final int INTEGER_ITERATOR = GLOBALLY_NAMED + 7;
    private static final int ENUMERATION_ITERATOR = GLOBALLY_NAMED + 8;

    private static final int[] argcs = {1, 1, 2, 1
        // not named
        // hasown, push, pop, join
        , 0, 1, 0, 1 
        // default- setter getter
        , 2, 1
        // new iter int-iter enum-iter
        , 0, 0, 0
    };

    private static final int PROTOTYPE_OBJECT = 0;
    private static final int PROTOTYPE_ARRAY = 1;
    private static final int PROTOTYPE_STRING = 2;
    private static final int PROTOTYPE_FUNCTION = 3;

    private static Hashtable clone(Object o) {
        Hashtable result = new Hashtable();
        result.put("prototype", o);
        return result;
    }

    public int getArgc() {
            return argcs[id];
    }

    private LightScriptStdLib(int id) {
        this.id = id;
    }
    public Object apply(Object thisPtr, Object[] args, int argpos, int argcount) throws LightScriptException {
        if(argcs[id] >= 0 && argcount != argcs[id]) {
            throw new LightScriptException("Error: Wrong number of arguments");
        }
        switch(id) {
            case PRINT: {
                 System.out.println(args[argpos]);
                 break;
            }
            case TYPEOF: {
                Object o = args[argpos];
                if(o instanceof Hashtable) {
                    return "object";
                } else if(o instanceof Stack) {
                    return "array";
                } else if(o instanceof Integer) {
                    return "number";
                } else if(o == LightScript.UNDEFINED) {
                    return "undefined";
                } else if(o == LightScript.NULL) {
                    return "null";
                } else if(o == LightScript.TRUE || o == LightScript.FALSE) {
                    return "boolean";
                } else {
                    return "builtin";
                }
            }
            case PARSEINT: {
                return Integer.valueOf((String)args[argpos], ((Integer)args[argpos +1]).intValue());
            }
            case CLONE: {
                return clone((Hashtable)args[argpos]);
            }
            case HAS_OWN_PROPERTY: {
                if(thisPtr instanceof Hashtable) {
                    return ((Hashtable)thisPtr).contains(args[argpos])
                            ? LightScript.TRUE
                            : LightScript.FALSE;
                }
                break;
            }
            case ARRAY_PUSH: {
                ((Stack)thisPtr).push(args[argpos]);
                 break;
            }
            case ARRAY_POP: {
                ((Stack)thisPtr).pop();
                break;
            }
            case ARRAY_JOIN: {
                Stack s = (Stack) thisPtr;
                if(s.size() == 0) {
                    return "";
                }
                StringBuffer sb = new StringBuffer();
                sb.append(s.elementAt(0).toString());
                String sep = args[argpos].toString();
                for(int i = 1; i < s.size(); i++) {
                    sb.append(sep);
                    sb.append(s.elementAt(i));
                }
                return sb.toString();
            }
            case DEFAULT_SETTER: {
                Object key = args[argpos];
                Object val = args[argpos + 1];
                // implementation like "thisPtr[key] = val"
                break;
            }
            case DEFAULT_GETTER: {
                Object key = args[argpos];
                // implementation like "return thisPtr[key]"
                break;
            }
            case NEW_ITERATOR: {
                if(thisPtr instanceof Hashtable) {
                    LightScriptStdLib result;
                    result = new LightScriptStdLib(ENUMERATION_ITERATOR);
                    result.closure = new Object[1];
                    result.closure[0] = ((Hashtable)thisPtr).keys();
                    return result;
                }
                if(thisPtr instanceof Stack) {
                    LightScriptStdLib result;
                    result = new LightScriptStdLib(INTEGER_ITERATOR);
                    result.closure = new Object[2];
                    result.closure[0] = new Integer(-1);
                    result.closure[1] = new Integer(((Stack)thisPtr).size() - 1);
                    return result;
                }
                break;
            }
            case INTEGER_ITERATOR: {
                if(closure[0].equals(closure[1])) {
                    return LightScript.UNDEFINED;
                }
                int current = ((Integer)closure[0]).intValue();
                current = current + 1;
                Object result = new Integer(current);
                closure[0] = result;
                return result;
            }
            case ENUMERATION_ITERATOR: {
                Enumeration e = (Enumeration) closure[0];
                if(!e.hasMoreElements()) {
                    return LightScript.UNDEFINED;
                }
                return e.nextElement();
            }
        }
        return LightScript.UNDEFINED;
    }

    public static void register(LightScript ls) {

        Hashtable objectPrototype = new Hashtable();
        ls.executionContext[ls.OBJECT_PROTOTYPE] = objectPrototype;

        Hashtable arrayPrototype = new Hashtable();
        ls.executionContext[ls.ARRAY_PROTOTYPE] = arrayPrototype;

        Hashtable stringPrototype = clone(objectPrototype);
        ls.executionContext[ls.STRING_PROTOTYPE] = stringPrototype;

        Hashtable functionPrototype = clone(objectPrototype);
        ls.executionContext[ls.FUNCTION_PROTOTYPE] = functionPrototype;

        ls.executionContext[ls.SETTER] = new LightScriptStdLib(DEFAULT_SETTER);

        ls.executionContext[ls.GETTER] = new LightScriptStdLib(DEFAULT_GETTER);

        for(int i = 0; i < names.length; i++) {
            ls.set(names[i], new LightScriptStdLib(i));
        }

        objectPrototype.put("hasOwnProperty", new LightScriptStdLib(HAS_OWN_PROPERTY));
        objectPrototype.put("__create_iterator", new LightScriptStdLib(NEW_ITERATOR));
        Hashtable object = clone(objectPrototype);

        // Create members for array
        arrayPrototype.put("push", new LightScriptStdLib(ARRAY_PUSH));
        arrayPrototype.put("pop", new LightScriptStdLib(ARRAY_POP));
        arrayPrototype.put("join", new LightScriptStdLib(ARRAY_JOIN));
        Hashtable array = clone(arrayPrototype);

        Hashtable string = clone(stringPrototype);

        Hashtable function = clone(stringPrototype);


        ls.set("Object", object);
        ls.set("String", string);
        ls.set("Array", array);
        ls.set("Function", function);


    }
}
