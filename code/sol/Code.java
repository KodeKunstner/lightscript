import java.util.Stack;

public class Code extends Function {
	private Function code[];
	private String name;

	public Code(Function code[], String name) {
		this.code = code;
		this.name = name;
	}

	public void apply(Stack s) {
		for(int i = 0; i < code.length; i++) {
			code[i].apply(s);
		}
	}

	public static void eval(Function code[], Stack s) {
		for(int i = 0; i < code.length; i++) {
			code[i].apply(s);
		}
	}

	public String toString() {
		return name;
	}
}
