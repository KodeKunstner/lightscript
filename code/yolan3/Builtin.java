import java.util.Stack;
import java.util.Hashtable;

public class Builtin extends Function {
	private int fn;
	private static String[] names = {"compile"};

	private Builtin(int fn) {
		this.fn = fn;
	}
	
	public static void loadTo(Hashtable h) {
		for(int i = 0; i < names.length; i++) {
			h.put(names[i], new Builtin(i));
		}
	}

	public void apply(Stack s) {
		switch(fn) {
/* compile */ case 0: {
		s.push(new Code((Stack)s.pop()));
} break;
		}
	}

	public String toString() {
		return names[fn];
	}
}
