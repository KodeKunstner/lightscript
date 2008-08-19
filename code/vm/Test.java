import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

class Test {
	public static void main(String[] args) throws java.io.IOException {
		InputStream is = new FileInputStream(new File(args[0]));
		JSON.print(JSON.parse(is), System.out);
		System.out.println("");
	}
}
