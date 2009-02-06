import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
	InputStream is = new FileInputStream(new File(args[0]));
	int i;
	do {
		i = is.read();
		System.out.println(i);
	} while(i > 0);
    }
}
