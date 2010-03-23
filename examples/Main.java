package examples;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import com.solsort.lightscript.*;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        InputStream is;
        if (args.length == 1) {
            is = new FileInputStream(new File(args[0]));
        } else {
            is = System.in;
        }
        LightScript ls = new LightScript();
        ls.eval(is);
    }
}
