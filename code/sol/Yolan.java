import java.util.Stack;
import java.util.Hashtable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

class Yolan {
	public Hashtable globals;
	public Stack stack;
	private Function parse;

	public Yolan() {
		globals = new Hashtable();
		stack = new Stack();
		Core.loadTo(globals);
		Builtin.loadTo(globals);
	}
	public Object callUnaryFunction(String name, Object arg) {
		stack.push(arg);
		((Function)globals.get(name)).apply(stack);
		return stack.pop();
	}

	public static void main(String[] argc) throws Exception {
		StringBuffer text = new StringBuffer();
		InputStream is = new FileInputStream(new File(argc[0]));
		Yolan yl = new Yolan();
		Function f = (Function) yl.callUnaryFunction("parse", is);
		while(f != null) {
			text.append(f);
			text.append(" ");
			f.apply(yl.stack);
			f = (Function) yl.callUnaryFunction("parse", is);
		}
		System.out.println(text);
	}
}
