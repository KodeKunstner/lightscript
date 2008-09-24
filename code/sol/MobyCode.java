import java.util.Stack;
public class MobyCode extends Code {
	private int argc; 
	private boolean is_void;
	private Function[] code;

	public int getArgc() {
		return argc;
	}

	public boolean isVoid() {
		return is_void;
	}

	MobyCode(Function[] code, String name, int argc, boolean is_void) {
		super(code, name);
		this.argc = argc;
		this.is_void = is_void;
	}

	public void apply(Stack s) {
		for(int i = 0; i < code.length; i++) {
			code[i].apply(s);
		}
	}
}
