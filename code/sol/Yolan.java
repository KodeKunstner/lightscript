import java.util.Stack;
import java.util.Hashtable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

class Yolan {
	public Hashtable globals;
	public Stack stack;
	private Function parse;

	boolean readEval(InputStream is) {
		Object o;
		stack.push(is);
		parse.apply(stack);
		o = stack.pop();
		if(o == null) {
			return false;
		} else if(o instanceof Function) {
			((Function)o).apply(stack);
		} else {
			stack.push(o);
		}
		return true;
	}

	public Yolan() {
		globals = new Hashtable();
		stack = new Stack();
		Core.loadTo(globals);
		Builtin.loadTo(globals);
		parse = (Function)globals.get("parse");
	}

	public static void main(String[] argc) throws Exception {

		InputStream is = new FileInputStream(new File(argc[0]));
		Yolan yl = new Yolan();

		{ while(yl.readEval(is)); }
	}
}
