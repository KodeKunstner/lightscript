import java.util.Hashtable;
import java.util.Stack;

final class MobyVM {

	public final Hashtable globals;
	public final Stack stack;
	public static final Boolean t = new Boolean(true);
	public static final Boolean f = new Boolean(false);
	public static final Integer zero = new Integer(0);
	public static final Integer one = new Integer(1);
	public static final Integer two = new Integer(2);

	public MobyVM() {
		stack = new Stack();
		globals = new Hashtable();
	}

	public Object eval(Object o) {
		MobyCode m = new MobyCode(o);
		return o;
	}
}
