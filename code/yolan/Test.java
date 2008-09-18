import java.util.Stack;
import java.util.Hashtable;
import java.io.InputStream;

import java.io.File;
import java.io.FileInputStream;

class Test{
	public static void main(String[] args) throws Exception {
		Yolan yl = new Yolan();
		yl.eval("'Hello World!' print 3 5 < { 'xxx' print } { 5 4 3 2 1 + + + + } if-else 'foo' 'bar' ");
		System.out.println(yl.toString());
	}
}
