import java.util.Hashtable;
import java.util.Stack;

final class Globals extends Function {
	private Hashtable globals;
	// boolean indicating whether this is the set-global-function or get-global-function
	private boolean set;

	public Globals(Hashtable globals, boolean set) {
		this.globals = globals;
		this.set = set;
	}

	public void apply(Stack s) {
		if(set) {
			s.push(globals.put(s.pop(), s.pop()));
		} else {
			s.push(globals.get(s.pop()));
		}
	}

	public String toString() {
		return set ? "set-global" : "get-global";
	}
}
