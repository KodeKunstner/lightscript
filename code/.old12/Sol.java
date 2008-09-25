import java.util.Stack;
import java.util.Hashtable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

public final class Sol {
	public Hashtable globals;
	public Stack stack;
	private Function parse;

	public Sol() {
		globals = new Hashtable();
		stack = new Stack();
		Globals.loadTo(globals);
		Builtin.loadTo(globals);
		runFile("stdlib.sol");
	}
	public Object callUnaryFunction(String name, Object arg) {
		stack.push(arg);
		((Function)globals.get(name)).apply(stack);
		return stack.pop();
	}

	public void runFile(String filename) {
		InputStream is;
		try {
			is = new FileInputStream(new File(filename));
		} catch(IOException e) {
			System.out.println(e);
			return;
		}
		Function f = (Function) callUnaryFunction("parse", is);
		while(f != null) {
			f.apply(stack);
			f = (Function) callUnaryFunction("parse", is);
		}
	}


	public static void main(String[] args) throws Exception {

		Sol sol = new Sol();
		sol.runFile(args[0]);
	}
}
