import java.util.Hashtable;
import java.util.Stack;

final class MobyCode implements MobyFunction {
	//private final Object[] upvals;
	private final Object[] consts;
	private final short[] code;

	public MobyCode(Object o) {
		int i;
		Hashtable h = (Hashtable)o;

		Stack v = (Stack)h.get("consts");
		consts = new Object[v.size()];
		for(i=0;i<v.size();i++) {
			consts[i] = v.elementAt(i);
		}

		v = (Stack)h.get("code");
		code = new short[v.size()];
		for(i=0;i<v.size();i++) {
			code[i] = ((Integer)v.elementAt(i)).shortValue();
		}
	}

	public Object apply(MobyVM vm) {
		int pc, i;
		Object o, result;
		result = null;
		Stack stack = vm.stack;
		Hashtable globals = vm.globals;

		for(pc=0;pc < code.length;pc++) {
			switch(code[pc]) {
/////////////////////////////
// Beginning of dispatch
////
	// push from consts (consts id)
	// ... -> ..., val
	case 100: 
		pc++;
		stack.push(consts[code[pc]]);
		break;
	// call global (number of arguments) 
	// ..., arg0, arg1, ..., argN, fn -> ..., return value
	case 101:
		pc++; 
		vm.argc = code[pc];
		o = ((MobyFunction)stack.pop()).apply(vm);
		stack.setSize(stack.size() - vm.argc);
		stack.push(o);
		break;
	// lookup global (consts id)
	// ... -> ..., val
	case 102: 
		pc++;
		stack.push(globals.get(consts[code[pc]]));
		break;
	// jump
	// ... -> ...
	case 103: 
		pc++;
		pc += code[pc];
		break;
	// condjump, jump if stack top is false
	// ..., truth-value -> ...
	case 104: 
		pc++;
		o = stack.pop();
		if(o == null || o == vm.f 
				|| (o instanceof Integer && 0 == ((Integer)o).intValue())) {
			pc += code[pc];
		}
		break;
	// put-global, pop statcktop and store as global val (global const id)
	// ..., value -> ..., null
	case 105: 
		pc++;
		globals.put(consts[code[pc]], stack.pop());
		stack.push(null);
		break;
	// pop n values from the stack;
	// ..., value -> ...
	case 106: 
		pc++;
		stack.setSize(stack.size() - code[pc]);
		break;
////
// End of dispatch
/////////////////////////////
			}
		}
		return result;
	}
}

