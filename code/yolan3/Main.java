import java.util.*;
import java.io.*;

class Main {
	static Hashtable globals;
	static Stack stack;
	static Function parse;
	static InputStream inputstream;


	static boolean readEval() {
		Object o;
		stack.push(inputstream);
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

	public static void main(String[] argc) throws Exception {

		globals = new Hashtable();
		stack = new Stack();
		Core.loadTo(globals);
		parse = (Function)globals.get("parse");
		inputstream = new FileInputStream(new File(argc[0]));

		while(readEval()) {
			System.out.println(stack);
		}
	}
}
