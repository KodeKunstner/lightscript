import java.util.Hashtable;
import java.util.Stack;

final class MobyVM {

	public final Hashtable globals;
	public final Stack stack;
	public int argc;
	public static final Boolean t = new Boolean(true);
	public static final Boolean f = new Boolean(false);

	public Object getArg(int n) {
		return n < argc?stack.elementAt(stack.size() - argc + n):null;
	}

	public int getArgI(int n) {
		return ((Integer)getArg(n)).intValue();
	}

	public String getArgS(int n) {
		return getArg(n).toString();
	}

	public MobyVM() {
		stack = new Stack();
		globals = new Hashtable();
		argc = 0;
		MobyBuiltin.doRegister(this);
	}

	private Object calln(MobyFunction f, int n) {
		Object result;
		int t = argc;
		argc = n;
		result = f.apply(this);
		argc = t;
		stack.setSize(stack.size() - n);
		return result;
	}
	public Object call(MobyFunction f) {
		return calln(f, 0);
	}
	public Object call(MobyFunction f, Object a0) {
		stack.push(a0);
		return calln(f, 1);
	}
	public Object call(MobyFunction f, Object a0, Object a1) {
		stack.push(a0);
		stack.push(a1);
		return calln(f, 2);
	}
	public Object call(MobyFunction f, Object a0, Object a1, Object a2) {
		stack.push(a0);
		stack.push(a1);
		stack.push(a2);
		return calln(f, 3);
	}
	public Object call(MobyFunction f, Object a0, Object a1, Object a2, Object a3) {
		stack.push(a0);
		stack.push(a1);
		stack.push(a2);
		stack.push(a3);
		return calln(f, 4);
	}
}
