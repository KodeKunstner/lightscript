import java.util.Stack;
import java.util.Hashtable;
import java.io.InputStream;

import java.io.File;
import java.io.FileInputStream;

class Test{
	public static void main(String[] args) throws Exception {
		InputStream is = new FileInputStream(new File(args[0]));
		StringBuffer sb = new StringBuffer();
		Yolan yl = new Yolan(is);
		Object o = yl.parse();
		yl.yol2str(o, sb, 0);
		System.out.println(sb.toString());
	}
}
