import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

class Test {
	public static void main(String[] args) throws java.io.IOException {
		InputStream is = new FileInputStream(new File(args[0]));
		Object o = JSON.parse(is);
		JSON.print(o, System.out);
		System.out.println("");

		MobyVM vm = new MobyVM(o);
		JSON.print(vm, System.out);
		System.out.println("");
		System.out.println("");
	}
}
