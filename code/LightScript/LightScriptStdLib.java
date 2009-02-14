import java.util.Stack;
import java.util.Hashtable; 

class LightScriptStdLib implements LightScriptFunction {
    private int id;

    // globally named functions
    private static final int PRINT = 0;
    private static final int TYPEOF = 1;

    // methods and other stuff added manually to lightscript
    private static final int NOT_NAMED = 2;
    private static final int STACK_PUSH = NOT_NAMED + 0;

    private static final String[] names = {"print", "gettype"};
    private static final int[] argcs = {1, 1,
        // not named
        1
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
            case STACK_PUSH: {
                 ((Stack)thisPtr).push(args[argpos]);
                 break;
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
        stackMembers.put("push", new LightScriptStdLib(STACK_PUSH));

        // Special environment object, containing methods for stack, ...
        Object[] env = new Object[1];
        env[0] = stackMembers;
        ls.set("(ENV)", env);
    }
}
