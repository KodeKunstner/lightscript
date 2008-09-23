import java.util.*;
import java.io.*;

class Main {
	public static void main(String[] argc) throws Exception {
		InputStream is = new FileInputStream(new File(argc[0]));
		Yolan yl = new Yolan();

		while(yl.readEval(is)) {
			System.out.println(yl.stack);
		}
	}
}
