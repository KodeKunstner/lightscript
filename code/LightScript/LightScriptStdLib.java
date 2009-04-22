import java.util.Stack;
import java.util.Hashtable; 

class LightScriptStdLib implements LightScriptFunction {
    private int id;
    private Object closure;

    // globally named functions
    private static final int PRINT = 0;
    private static final int TYPEOF = 1;
    private static final int PARSEINT = 2;
    private static final int CLONE = 3;

    private static final String[] names = {"print", "gettype", "parseint", "clone"};
    // methods and other stuff added manually to lightscript
    private static final int NOT_NAMED = 4;
    private static final int HAS_OWN_PROPERTY = NOT_NAMED + 0;
    private static final int ARRAY_PUSH = NOT_NAMED + 1;
    private static final int ARRAY_POP = NOT_NAMED + 2;
    private static final int ARRAY_JOIN = NOT_NAMED + 3;

    private static final int[] argcs = {1, 1, 2, 1
        // not named
        , 0, 1, 0, 1
    };


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
                return new LightScriptObject((Hashtable)args[argpos]);
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
        }
        return null;
    }
    public static void register(LightScript ls) {
        for(int i = 0; i < names.length; i++) {
            ls.set(names[i], new LightScriptStdLib(i));
        }

        Hashtable objectPrototype = new Hashtable();
        objectPrototype.put("hasOwnProperty", new LightScriptStdLib(HAS_OWN_PROPERTY));
        objectPrototype.put("prototype", new objectPrototype);
        LightScriptObject object = new LightScriptObject(objectPrototype);

        // Create members for array
        Hashtable arrayPrototype = new Hashtable();
        arrayPrototype.put("push", new LightScriptStdLib(ARRAY_PUSH));
        arrayPrototype.put("pop", new LightScriptStdLib(ARRAY_POP));
        arrayPrototype.put("join", new LightScriptStdLib(ARRAY_JOIN));
        arrayPrototype.put("prototype", new arrayPrototype);
        LightScriptObject array = new LightScriptObject(arrayPrototype);

        Hashtable stringPrototype = new LightScriptObject(objectPrototype);
        stringPrototype.put("prototype", new stringPrototype);
        LightScriptObject string = new LightScriptObject(stringPrototype);

        Hashtable functionPrototype = new LightScriptObject(objectPrototype);
        functionPrototype.put("prototype", new functionPrototype);
        LightScriptObject function = new LightScriptObject(stringPrototype);

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
    }
}
