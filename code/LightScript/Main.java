import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;

public class Main implements LightScriptFunction {
    public int getArgc() { return 0; }
    public Object apply(Object thisPtr, Object[] args, int argpos, int argcount) throws LightScriptException {
        LightScriptFunction fn = (LightScriptFunction)args[argpos];
        args[argpos] = "X";
        args[argpos+1] = "Y";
        args[argpos+2] = "Z";
        System.out.println("MainA");
        Object o = fn.apply(thisPtr, args, argpos, 3);
        System.out.println("MainB");
        return o;
        /*
        System.out.println("T" + thisPtr);
        for(int i = 0; i < argcount; i++) {
            System.out.println("M" + args[argpos + i]);
        }
        throw new LightScriptException("Blah");
        */
    }
static class FunctionLibrary implements LightScriptFunction { 
    int id; // This tells which function the object represents
    public int getArgc() {
        return 0;
    }
    public Object apply(Object thisPtr, Object[] args, int argpos, 
                        int argcount) throws LightScriptException {
        switch(id) {   
            case 0: // integer division
                return new Integer(((Integer)args[argpos]).intValue()
                                  /((Integer)args[argpos+1]).intValue());
            case 1: // increment property i, not of superclass
                int i = ((Integer)((Hashtable)thisPtr).get("i")).intValue();
                ((Hashtable)thisPtr).put("i", new Integer(i + 1));
        }
        return null;
    }
    private FunctionLibrary(int id) { this.id = id; }
    public static void register(LightScript lsContext) {
        lsContext.set("div", new FunctionLibrary(0));
        lsContext.set("propinc", new FunctionLibrary(1));
    }
}   

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
	InputStream is = new FileInputStream(new File(args[0]));
        LightScript ls = new LightScript();
        /*
    FunctionLibrary.register(ls);
    ls.eval("obj = {}; obj.i = 1; obj.inc = propinc;"
                  +"while(obj.i < 10) { "
                  +"    print(div(42, obj.i)); "
                  +"    obj.inc();"
                  +"}");

        System.out.println(ls.get("bar"));
    */


        ls.eval(is);
    }
}
