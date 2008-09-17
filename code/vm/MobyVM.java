import java.util.Hashtable;
import java.util.Stack;
import java.util.Enumeration;

final class MobyVM extends Stack {

	public final Hashtable globals;
	public static final Boolean t = new Boolean(true);
	public static final Boolean f = new Boolean(false);
	public String[] literals;
	public short[][] functions;
	Object that;
	Object undefined;

	public MobyVM(Object o) {
		globals = new Hashtable();
		// just something unique where "equals"-function and toString works something
		undefined = new Hashtable();
		int i, j;
		Stack v, w;


		v = (Stack) ((Hashtable) o).get("literals");
		i = v.size();
		literals = new String[i];
		while(i>0) {
			i--;
			literals[i] = (String)v.elementAt(i);
		} ;

		v = (Stack) ((Hashtable) o).get("functions");
		i = v.size();
		functions = new short[i][];
		while(i>0) {
			i--;
			w = (Stack)v.elementAt(i);
			j = w.size();
			functions[i] = new short[j];
			while(j>0) {
				j--;
				functions[i][j] = ((Integer)w.elementAt(j)).shortValue();
			}
		}
		call(functions[functions.length - 1]);
	}

	private void call(short[] code) {
		int pc = -1;
		for(;;) {
			pc++;
			System.out.println("pc: " + pc + "   code: " + code[pc]);
			switch(code[pc]) {
	/* (jump) */ case 0: {
		pc++;
		pc += code[pc];
	/* (jump if false) */ break; } case 1: {
		Object tmp = pop();
		pc++;
		if(tmp == undefined || tmp == f) {
			pc += code[pc];
		}
	/* (get local) */ break; } case 2: {
		pc++;
		push(elementAt(size() - code[pc]));
	/* (set local) */ break; } case 3: {
		pc++;
		setElementAt(pop(), size() - code[pc]);
	/* (set global) */ break; } case 4: {
		pc++;
		globals.put(literals[code[pc]], pop());
	/* (get global) */ break; } case 5: {
		pc++;
		push(globals.get(literals[code[pc]]));
	/* (call function) */ break; } case 6: {
	/* (call method) */ break; } case 7: {
	/* (pop) */ break; } case 8: {
		pop();
	/* === */ break; } case 9: {
		Object t1 = pop();
		Object t2 = pop();
		push(t1.equals(t2)?t:f);
	/* !== */ break; } case 10: {
		Object t1 = pop();
		Object t2 = pop();
		push(t1.equals(t2)?f:t);
	/* < */ break; } case 11: {
		Object t1 = pop();
		Object t2 = pop();
		boolean b;
		if(t1 instanceof Integer && t2 instanceof Integer) {
			b = ((Integer)t1).intValue() < ((Integer)t2).intValue();
		} else {
			b = t1.toString().compareTo(t2.toString()) < 0;
		}
		push(b?t:f);
	/* <= */ break; } case 12: {
		Object t1 = pop();
		Object t2 = pop();
		boolean b;
		if(t1 instanceof Integer && t2 instanceof Integer) {
			b = ((Integer)t1).intValue() <= ((Integer)t2).intValue();
		} else {
			b = t1.toString().compareTo(t2.toString()) <= 0;
		}
		push(b?t:f);
	/* ! */ break; } case 13: {
		Object tmp = pop();
		push((tmp == undefined || tmp == f)?t:f);
	/* + */ break; } case 14: {
		push(new Integer(((Integer)pop()).intValue() + ((Integer)pop()).intValue()));
	/* (neg) */ break; } case 15: {
		push(new Integer(-((Integer)pop()).intValue()));
	/* (minus) */ break; } case 16: {
		push(new Integer(-((Integer)pop()).intValue() + ((Integer)pop()).intValue()));
	/* return */ break; } case 17: {
		return;
	/* (pushnil) */ } case 18: {
		push(undefined);
	/* true */ break; } case 19: {
		push(t);
	/* false */ break; } case 20: {
		push(f);
	/* next */ break; } case 21: {
		Enumeration e = (Enumeration)pop();
		if(e.hasMoreElements()) {
			push(e.nextElement());
		} else {
			push(undefined);
		}
	/* iterator */ break; } case 22: {
		Object tmp = pop();
		if(tmp instanceof Stack) {
			push(((Stack)tmp).elements());
		} else if(tmp instanceof Hashtable) {
			push(((Hashtable)tmp).keys());
		} else {
			push(undefined);
		}
	/* length */ break; } case 23: {
		Object tmp = pop();
		if(tmp instanceof Stack) {
			push(new Integer(((Stack)tmp).size()));
		} else if(tmp instanceof Hashtable) {
			push(new Integer(((Hashtable)tmp).size()));
		} else {
			push(undefined);
		}
	/* join */ break; } case 24: {
		StringBuffer sb = new StringBuffer();
		Stack s = (Stack)pop();
		int i = 0;
		for(i=0; i < s.size();i++) {
			sb.append(s.elementAt(i).toString());
		}
		push(sb.toString());
	/* pop */ break; } case 25: {
		push(((Stack)pop()).pop());
	/* push */ break; } case 26: {
		Object tmp = pop();
		((Stack)peek()).push(tmp);
	/* getch */ break; } case 27: {
	/* is_a */ break; } case 28: {
	/* println */ break; } case 29: {
		System.out.println(peek());
	/* str2int */ break; } case 30: {
	/* (put) */ break; } case 31: {
	/* (get) */ break; } case 32: {
	/* (new array) */ break; } case 33: {
	/* (new object) */ break; } case 34: {
	/* (get literal) */ break; } case 35: {
		pc++;
		push(literals[code[pc]]);
	/* this */ break; } case 36: {
	/* (set this) */ break; } case 37: {
	/* map */ break; } case 38: {
	/* (number) */ break; } case 39: {
		pc++;
		push(new Integer(code[pc]));
	/* str2int */ break; } case 40: {
	/* popfront */ break; } case 41: {
	/* (function) */ break; } case 42: {
	}
			}
		}
	}

}
