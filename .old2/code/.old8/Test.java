import java.util.Stack;
import java.util.Hashtable;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;

class Test{
	public static void main(String[] args) throws Exception {
		Yolan yl = new Yolan();
		yl.eval(new FileInputStream(new File(args[0])));
	}
}
