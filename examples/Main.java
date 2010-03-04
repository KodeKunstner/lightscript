package examples;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import net.lightscript.i1.*;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        InputStream is;
        if(args.length == 1) {
	    is = new FileInputStream(new File(args[0]));
        } else {
            is = System.in;
        }
        LightScript ls = new LightScript();
        Object result = null;
        do {
            try {
                result = ls.evalNext(is);
            } catch(LightScriptException e) {
                System.out.println(e);
            }
            if(args.length != 1 && result != null) {
                System.out.println(" -> " + result);
            }
        } while(result != null);
    }
}
