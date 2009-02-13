class LightScriptStdLib {
    private int id;

    private static final int PRINT = 0;
    private static final String[] names = {"print"};
    private static final int[] argcs = {1};


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
        }
        return null;
    }
    public static void register(LightScript ls) {
        for(int i = 0; i < names.length; i++) {
            ls.set(names[i], new LightScriptStdLib(i));
        }
    }
}
