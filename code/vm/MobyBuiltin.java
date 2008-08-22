import java.util.Hashtable;
import java.util.Stack;

final class MobyBuiltin implements MobyFunction {
	private final int id;

	private MobyBuiltin(int id) {
		this.id = id;
	}

	public static void doRegister(MobyVM vm) {
		Hashtable globals = vm.globals;
		globals.put("print", new MobyBuiltin(0));
		globals.put("+", new MobyBuiltin(1));
		globals.put("*", new MobyBuiltin(2));
		globals.put("-", new MobyBuiltin(3));
		globals.put("===", new MobyBuiltin(4));
		globals.put("<", new MobyBuiltin(5));
		globals.put("!", new MobyBuiltin(6));
		globals.put("~", new MobyBuiltin(7));
		globals.put("|", new MobyBuiltin(8));
		globals.put("^", new MobyBuiltin(9));
		globals.put("&", new MobyBuiltin(10));
		globals.put("<<", new MobyBuiltin(11));
		globals.put(">>", new MobyBuiltin(12));
		globals.put(">>>", new MobyBuiltin(13));
	}

	public Object apply(MobyVM vm) {
		Object o0, o1;
		Object result = null;
		int i;
		switch(id) {

////////////////////////
// Beginning of builtin functions
	// print
	case 0: 
		for(i=0;i<vm.argc;i++) {
			Print.print(vm.getArg(i));
		}
	break;
	// +
	case 1: 
		o0 = vm.getArg(0);
		o1 = vm.getArg(1);
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
		o0 = vm.getArg(0);
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
