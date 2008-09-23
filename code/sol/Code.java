import java.util.Stack;

public class Code extends Function {
	private Object code[];
	private String name;

	public Code(Object code[], String name) {
		this.code = code;
		this.name = name;
	}

	public void apply(Stack s) {
		for(int i = 0; i < code.length; i++) {
			Object obj = code[i];
			if(obj instanceof Function) {
				((Function)obj).apply(s);
			} else {
				s.push(obj);
			}
		}
	}

	public static void eval(Object code[], Stack s) {
		for(int i = 0; i < code.length; i++) {
			Object obj = code[i];
			if(obj instanceof Function) {
				((Function)obj).apply(s);
			} else {
				s.push(obj);
			}
		}
	}

	public String toString() {
		return name;
	}
}
