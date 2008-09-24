import java.util.Stack;

public class Code extends Function {
	public Function code[];

	public Code(Function code[]) {
		this.code = code;
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
}
