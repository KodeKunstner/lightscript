import java.util.Stack;
import java.util.Hashtable; 

class LightScriptStdLib implements LightScriptFunction {
    private int id;

    // globally named functions
    private static final int PRINT = 0;
    private static final int TYPEOF = 1;
    private static final int PARSEINT = 2;

    private static final String[] names = {"print", "gettype", "parseint"};
    // methods and other stuff added manually to lightscript
    private static final int NOT_NAMED = 3;
    private static final int HAS_OWN_PROPERTY = NOT_NAMED + 0;
    private static final int ARRAY_PUSH = NOT_NAMED + 1;
    private static final int ARRAY_POP = NOT_NAMED + 2;
    private static final int ARRAY_JOIN = NOT_NAMED + 3;

    private static final int[] argcs = {1, 1, 2
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

        // Create members for stack
        Hashtable stackMembers = new Hashtable();
        stackMembers.put("push", new LightScriptStdLib(ARRAY_PUSH));
        stackMembers.put("pop", new LightScriptStdLib(ARRAY_POP));
        stackMembers.put("join", new LightScriptStdLib(ARRAY_JOIN));

        Hashtable objectMembers = new Hashtable();
        objectMembers.put("hasOwnProperty", new LightScriptStdLib(HAS_OWN_PROPERTY));

        Hashtable stringMembers = new Hashtable();

        // Special environment object, containing methods for stack, ...
        Object[] env = new Object[3];
        env[0] = stackMembers;
        env[1] = objectMembers;
        env[2] = stringMembers;
        ls.set("(ENV)", env);
    }
}
