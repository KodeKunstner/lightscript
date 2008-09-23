import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;

public class Yolan {
	public Hashtable globals;
	public Stack stack;
	private Function parse;

	public Yolan() {
		globals = new Hashtable();
		stack = new Stack();
		Core.loadTo(globals);
		parse = (Function)globals.get("parse");
	}

	public boolean readEval(InputStream inputstream) {

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
}
