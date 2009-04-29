import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        InputStream is;
        if("-".equals(args[0])) {
            is = System.in;
        } else {
	    is = new FileInputStream(new File(args[0]));
        }
        LightScript ls = new LightScript();
        ls.evalFrom(is);
        Object result = ls.evalNext();
        while(result != null) {
            System.out.println("> " + result);
            result = ls.evalNext();
        }
    }
}
