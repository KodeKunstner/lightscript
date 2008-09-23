import java.util.Stack;

public class Code extends Function {
	private Object code[];

	public Code(Stack s) {
		code = new Object[s.size()];
		s.copyInto(code);
	}

	public void apply(Stack s) {
		for(int i = 0; i < code.length; i++) {
			if(code[i] instanceof Function) {
				((Function)code[i]).apply(s);
			} else {
				s.push(code[i]);
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{ ");
		for(int i = 0; i < code.length; i++) {
			sb.append(code[i].toString());
			sb.append(" ");
		}
		sb.append("}");
		return sb.toString();
	}
}
