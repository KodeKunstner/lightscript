import java.util.Stack;

public final class Immediate extends Function {
	private Object val;

	public Immediate(Object val) {
		this.val = val;
	}

	public void apply(Stack s) {
		s.push(val);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(val == null) {
			return "false";
		} else if(val instanceof String) {
			String s = (String) val;
			sb.append("\"");
			for(int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if(c == '\\' || c == '"' || c < ' ') {
					sb.append('\\');
					if(c == '\n') {
						c = 'n';
					} else if(c == '\t') {
						c = 't';
					}
				}
				sb.append(c);
			}
			sb.append("\"");
		} else if(val instanceof Function[]) {
			Function code[] = ((Function[]) val);
			sb.append("( ");
			for(int i = 0; i< code.length; i++) {
				sb.append(code[i].toString());
				sb.append(" ");
			}
			sb.append(")");
		} else {
			return val.toString();
		}
		return sb.toString();
	}
}
