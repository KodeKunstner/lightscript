import java.util.Stack;

final class Eval extends Function {
	public void apply(Stack s) {
		Object code[] = (Object [])s.pop();
		for(int pc = 0; pc < code.length; pc++) {
			Object o = code[pc];
			if(o instanceof Function) {
				((Function) o).apply(s);
			} else {
				s.push(o);
			}
		}
	}
}
