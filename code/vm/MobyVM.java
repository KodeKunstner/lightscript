import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

class MobyVM {
	class Code implements MobyFunction {
		public Object[] consts;
		public byte[] code;

		public Code(Object o) {
			int i;
			Hashtable h = (Hashtable)o;

			Vector v = (Vector)h.get("consts");
			consts = new Object[v.size()];
			for(i=0;i<v.size();i++) {
				consts[i] = v.elementAt(i);
			}

			v = (Vector)h.get("code");
			code = new byte[v.size()];
			for(i=0;i<v.size();i++) {
				code[i] = ((Integer)v.elementAt(i)).byteValue();
			}
		}

		public Object apply(MobyVM vm) {
			return vm.eval((Code) this);
		}
	}

	class Builtin implements MobyFunction {
		private int id;

		public Builtin(int id) {
			this.id = id;
		}

		public Object apply(MobyVM vm) {
			int i;
			switch(id) {
		// print
		case 0: 
			for(i=0;i<vm.argc;i++) {
				System.out.println(vm.getArg(i));
			}
		break;
			}

			return null;
		}
	}

	Hashtable globals;
	Stack stack;
	int argc;

	Object getArg(int n) {
		return stack.elementAt(stack.size() - argc + n);
	}

	int getArgI(int n) {
		return toInt(getArg(n));
	}

	String getArgS(int n) {
		return (String)getArg(n);
	}

	public void log(String s) {
		System.out.println(s);
	}
	public int toInt(Object o) {
		return ((Integer)o).intValue();
	}

	
	public MobyVM() {
		stack = new Stack();
		globals = new Hashtable();

		globals.put("print", new Builtin(0));
	}

	public Object eval(String s) {
		return this;
	}

	public Object eval(Object o) {
		return eval(new Code(o));
	}

	public Object eval(Code c) {
		byte[] code = c.code;
		Object[] consts = c.consts;
		int pc, codesize, i;
		Object o;
		codesize = code.length;

		for(pc=0;pc < codesize;pc++) {
			switch(code[pc]) {
	// push from consts (consts id)
	case 0: 
		pc++;
		stack.push(consts[code[pc]]);
		break;
	// call global (number of arguments, consts id of name)
	case 1:
		pc++; 
		argc = code[pc];
		pc++;
		((MobyFunction)globals.get(consts[code[pc]])).apply(this);
		break;
			}
		}
		return null;
	}
}
