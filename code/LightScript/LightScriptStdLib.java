import java.util.Stack;
import java.util.Hashtable; 

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

    private static final int[] argcs = {1, 1, 2, 1
        // not named
        , 0, 1, 0, 1, 2, 1
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
                 } else if(o instanceof Boolean) {
                     return "boolean";
                 } else if(o == null) {
                     return "undefined";
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
                return ((Hashtable)thisPtr).contains(args[argpos])?LightScript.TRUE:null;
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
                break;
            }
            case DEFAULT_GETTER: {
                Object key = args[argpos];
                Object result = null;
                // TODO: implement this more efficiently using a hashtable
                if("length".equals(key)) {
                    if(thisPtr instanceof Hashtable) {
                        return new Integer(((Hashtable)thisPtr).size());
                    }
                    if(thisPtr instanceof Stack) {
                        return new Integer(((Stack)thisPtr).size());
                    }
                    if(thisPtr instanceof String) {
                        return new Integer(((String)thisPtr).length());
                    }
                    if(thisPtr instanceof LightScriptFunction) {
                        return new Integer(((LightScriptFunction)thisPtr).getArgc());
                    }
                }
                if("prxsototype".equals(key)) {
                    if(thisPtr instanceof Hashtable || thisPtr instanceof LightScriptObject) {
                        return closure[PROTOTYPE_OBJECT];
                    }
                    if(thisPtr instanceof LightScriptFunction) {
                        return closure[PROTOTYPE_OBJECT];
                    }
                }
                break;
            }
        }
        return LightScript.UNDEFINED;
    }
    public static void register(LightScript ls) {
        for(int i = 0; i < names.length; i++) {
            ls.set(names[i], new LightScriptStdLib(i));
        }

        Hashtable objectPrototype = new Hashtable();
        objectPrototype.put("hasOwnProperty", new LightScriptStdLib(HAS_OWN_PROPERTY));
        objectPrototype.put("prototype", objectPrototype);
        Hashtable object = clone(objectPrototype);

        // Create members for array
        Hashtable arrayPrototype = new Hashtable();
        arrayPrototype.put("push", new LightScriptStdLib(ARRAY_PUSH));
        arrayPrototype.put("pop", new LightScriptStdLib(ARRAY_POP));
        arrayPrototype.put("join", new LightScriptStdLib(ARRAY_JOIN));
        arrayPrototype.put("prototype", arrayPrototype);
        Hashtable array = clone(arrayPrototype);

        Hashtable stringPrototype = clone(objectPrototype);
        stringPrototype.put("prototype", stringPrototype);
        Hashtable string = clone(stringPrototype);

        Hashtable functionPrototype = clone(objectPrototype);
        functionPrototype.put("prototype", functionPrototype);
        Hashtable function = clone(stringPrototype);

        // Special environment object, containing methods for array, ...
        Object[] env = new Object[4];
        env[0] = arrayPrototype;
        env[1] = objectPrototype;
        env[2] = stringPrototype;
        env[3] = functionPrototype;
        ls.set("(ENV)", env);
        ls.set("Object", object);
        ls.set("String", string);
        ls.set("Array", array);
        ls.set("Function", function);

        ls.defaultSetter = new LightScriptStdLib(DEFAULT_SETTER);
        ls.defaultGetter = new LightScriptStdLib(DEFAULT_GETTER);
    }
}
