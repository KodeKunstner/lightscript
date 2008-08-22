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
		Object o0, o1;
		Object result = null;

		public Builtin(int id) {
			this.id = id;
		}

		public Object apply(MobyVM vm) {
			int i;
			switch(id) {

////////////////////////
// Beginning of builtin functions
		// print
		case 0: 
			for(i=0;i<vm.argc;i++) {
				System.out.println(vm.getArg(i));
			}
		break;
		// +
		case 1: 
			o0 = getArg(0);
			o1 = getArg(1);
			if(o0 instanceof String || o1 instanceof String) {
				result = vm.getArgS(0) + vm.getArgS(1);
			} else {
				result = new Integer(vm.getArgI(0) + vm.getArgI(1));
			}
		break;
		// *
		case 2: 
			result = new Integer(vm.getArgI(0) * vm.getArgI(1));
		break;
		// -
		case 3: 
			if(vm.argc == 1) {
				result = new Integer(- vm.getArgI(0));
			} else {
				result = new Integer(vm.getArgI(0) - vm.getArgI(1));
			}
		break;
		// ===
		case 4: 
			result = vm.getArg(0).equals(vm.getArg(1))?vm.t:vm.f;
		break;
		// <
		case 5: 
			o0 = getArg(0);
			if(o0 instanceof Integer) {
				result = vm.getArgI(0) < vm.getArgI(1)?vm.t:vm.f;
			} else if(o0 instanceof String) {
				result = (vm.getArgS(0).compareTo(vm.getArgS(1)) < 0)?(vm.t):(vm.f);
			}
		break;
		// !
		case 6: 
			result = (vm.getArg(0) == null || vm.getArg(0).equals(vm.f))?(vm.t):(vm.f);
		break;
		// ~
		case 7: 
			result = new Integer(~vm.getArgI(0));
		break;
		// |
		case 8: 
			result = new Integer(vm.getArgI(0) | vm.getArgI(1));
		break;
		// ^
		case 9: 
			result = new Integer(vm.getArgI(0) ^ vm.getArgI(1));
		break;
		// &
		case 10: 
			result = new Integer(vm.getArgI(0) & vm.getArgI(1));
		break;
		// <<
		case 11: 
			result = new Integer(vm.getArgI(0) << vm.getArgI(1));
		break;
		// >>
		case 12: 
			result = new Integer(vm.getArgI(0) >> vm.getArgI(1));
		break;
		// >>>
		case 13: 
			result = new Integer(vm.getArgI(0) >>> vm.getArgI(1));
		break;
// End of builtin functions
////////////////////////////
			}
			return result;
		}
	}

	Hashtable globals;
	Stack stack;
	int argc;
	static final Boolean t = new Boolean(true);
	static final Boolean f = new Boolean(false);

	Object getArg(int n) {
		return stack.elementAt(stack.size() - argc + n);
	}

	int getArgI(int n) {
		return toInt(getArg(n));
	}

	String getArgS(int n) {
		return getArg(n).toString();
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
		globals.put("+", new Builtin(1));
		globals.put("*", new Builtin(2));
		globals.put("-", new Builtin(3));
		globals.put("===", new Builtin(4));
		globals.put("<", new Builtin(5));
		globals.put("!", new Builtin(6));
		globals.put("~", new Builtin(7));
		globals.put("|", new Builtin(8));
		globals.put("^", new Builtin(9));
		globals.put("&", new Builtin(10));
		globals.put("<<", new Builtin(11));
		globals.put(">>", new Builtin(12));
		globals.put(">>>", new Builtin(13));
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
/////////////////////////////
// Beginning of dispatch
////
	// push from consts (consts id)
	// ... -> ..., val
	case 0: 
		pc++;
		stack.push(consts[code[pc]]);
		break;
	// call global (number of arguments) 
	// ..., arg0, arg1, ..., argN, fn -> ..., return value
	case 1:
		pc++; 
		argc = code[pc];
		o = ((MobyFunction)stack.pop()).apply(this);
		stack.setSize(stack.size() - argc);
		stack.push(o);
		break;
	// lookup global (consts id)
	// ... -> ..., val
	case 2: 
		pc++;
		stack.push(globals.get(consts[code[pc]]));
		break;
////
// End of dispatch
/////////////////////////////
			}
		}
		return null;
	}
}
