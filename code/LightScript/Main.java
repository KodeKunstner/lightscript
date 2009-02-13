import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
	InputStream is = new FileInputStream(new File(args[0]));
        LightScript ls = new LightScript();
        ls.set("x", new Integer(17));
        ls.eval(is);
        ls.eval("print(x);");

    }
}
