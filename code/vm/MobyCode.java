import java.util.Hashtable;
import java.util.Stack;

class MobyCode implements MobyFunction {
	public Object[] consts;
	public short[] code;

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
		int pc, codesize, i;
		Object o;
		Stack stack = vm.stack;
		Hashtable globals = vm.globals;

		for(pc=0;pc < code.length;pc++) {
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
		vm.argc = code[pc];
		o = ((MobyFunction)stack.pop()).apply(vm);
		stack.setSize(stack.size() - vm.argc);
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
		return stack.pop();
	}
}

