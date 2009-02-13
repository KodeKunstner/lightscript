import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;

public class Main implements LightScriptFunction {
    public Object apply(Object thisPtr, Object[] args, int argpos, int argcount) throws LightScriptException {
        System.out.println("T" + thisPtr);
        for(int i = 0; i < argcount; i++) {
            System.out.println("M" + args[argpos + i]);
        }
        throw new LightScriptException("Blah");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
	InputStream is = new FileInputStream(new File(args[0]));
        LightScript ls = new LightScript();
        ls.set("Main", new Main());
        ls.eval(is);
    }
}
