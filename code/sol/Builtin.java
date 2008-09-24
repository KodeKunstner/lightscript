import java.util.Hashtable;
import java.util.Stack;

public class Builtin extends Function {
	private Hashtable globals;
	private int fn;
	private static String names[] = { "swap", "print-stack", "print", "dup"};

	private Builtin(int fn) {
		this.fn = fn;
	}

	public void apply(Stack s) {
		switch(fn) {
/* BEGINNING OF BUILTIN FUNCTIONS */
/* swap */ case 0: 
	{
		Object t1, t2;
		t1 = s.pop();
		t2 = s.pop();
		s.push(t1);
		s.push(t2);
	} break;
/* print-stack */ case 1:
	{
		System.out.println("### BEGIN STACKDUMP ###");
		for(int i = s.size() - 1; i >= 0; i--) {
			System.out.println(new Immediate(s.elementAt(i)));
		}
		System.out.println("### END STACKDUMP ###");
	} break;
/* print */ case 2:
	{
		System.out.println(s.pop());
	} break;
/* dup */ case 3:
	{
		s.push(s.peek());
	} break;
/* END OF BUILTIN FUNCTIONS */
		}
	}

	/**
	 * Factory, create functions for accessing the globals, 
	 * and add them to the global-table/vm.
	 */
	public static void loadTo(Hashtable vm) {
		int i;
		for(i=0;i<names.length;i++) {
			vm.put(names[i], new Builtin(i));
		}
	}

	public String toString() {
		return names[fn];
	}
}
