import java.util.Hashtable;
import java.util.Stack;

class MobyVM {

	public Hashtable globals;
	public Stack stack;
	public int argc;
	public static final Boolean t = new Boolean(true);
	public static final Boolean f = new Boolean(false);

	public Object getArg(int n) {
		return stack.elementAt(stack.size() - argc + n);
	}

	public int getArgI(int n) {
		return toInt(getArg(n));
	}

	public String getArgS(int n) {
		return getArg(n).toString();
	}

	public int toInt(Object o) {
		return ((Integer)o).intValue();
	}

	
	public MobyVM() {
		stack = new Stack();
		globals = new Hashtable();
		MobyBuiltin.doRegister(this);
	}

	public Object eval(Object o) {
		argc = 0;
		return (new MobyCode(o)).apply(this);
	}
}
