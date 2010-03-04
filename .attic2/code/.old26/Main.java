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
	Ys.Closure cl = (Ys.Closure)Ys.readExpression(is);
	System.out.println(Ys.stringify(cl));
	System.out.println();
	Ys.stringify(Ys.execute(cl));
    }
}
