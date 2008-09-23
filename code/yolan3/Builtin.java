import java.util.Hashtable;
import java.util.Stack;

public class Builtin extends Function {
	private Hashtable globals;
	private int fn;
	private static String names[] = { "swap", "print-stack"};

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
		System.out.println(s);
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
