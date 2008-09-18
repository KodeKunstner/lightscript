import java.util.Hashtable;
import java.util.Stack;
import java.io.Reader;
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

	private Object call(short[] code) {
		int pc = -1;
		for(;;) {
			pc++;
			System.out.println("pc: " + pc + " code: " + code[pc] + " stack: " + toString());
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
		push(call((short [])pop()));
	/* (call method) */ break; } case 7: {
		short[] c = (short [])pop();
		Object prev = that;
		that = pop();
		push(call(c));
		that = prev;
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
		Object t2 = pop();
		Object t1 = pop();
		boolean b;
		if(t1 instanceof Integer && t2 instanceof Integer) {
			b = ((Integer)t1).intValue() < ((Integer)t2).intValue();
		} else {
			b = t1.toString().compareTo(t2.toString()) < 0;
		}
		push(b?t:f);
	/* <= */ break; } case 12: {
		Object t2 = pop();
		Object t1 = pop();
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
		Object obj = pop();
		pc++;
		setSize(size() - code[pc]);
		return obj;
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
		try { 
			push(String.valueOf((char)System.in.read()));
		} catch(java.io.IOException e) {
			System.out.println("VM error: " +e.toString());
		}
	/* is_a */ break; } case 28: {
		String type = (String)pop();
		Object o = pop();
		boolean b;
		if(type.equals("string")) {
			b = o instanceof String;
		} else if(type.equals("array")) {
			b = o instanceof Stack;
		} else if(type.equals("object")) {
			b = o instanceof Hashtable;
		} else if(type.equals("number")) {
			b = o instanceof Integer;
		} else {
			b = false;
		}
		push(b?t:f);
	/* println */ break; } case 29: {
		System.out.println(peek());
	/* str2int */ break; } case 30: {
		push(new Integer(Integer.parseInt((String)pop())));
	/* (put) */ break; } case 31: {
		Object val = pop();
		Object pos = pop();
		Object dst = peek();
		if(dst instanceof Hashtable) {
			((Hashtable)dst).put(pos, val);
		} else {
			int p = ((Integer)pos).intValue();
			Stack s = (Stack)dst;
			if(p>=s.size()) {
				s.setSize(p+1);
			}
			((Stack)dst).setElementAt(val, p);
		}
	/* (get) */ break; } case 32: {
		Object pos = pop();
		Object src = peek();
		if(src instanceof Hashtable) {
			((Hashtable)src).get(pos);
		} else {
			((Stack)src).elementAt(((Integer)pos).intValue());
		}
	/* (new array) */ break; } case 33: {
		push(new Stack());
	/* (new object) */ break; } case 34: {
		push(new Hashtable());
	/* (get literal) */ break; } case 35: {
		pc++;
		push(literals[code[pc]]);
	/* this */ break; } case 36: {
		push(that);
	/* popfront */ break; } case 37: {
		Stack s = (Stack)pop();
		push(s.elementAt(0));
		s.removeElementAt(0);
	/* map */ break; } case 38: {
		int i;
		Stack s = (Stack) pop();
		short[] c = (short [])pop();
		for(i=0;i<s.size();i++) {
			push(s.elementAt(i));
			s.setElementAt(call(c), i);
		}
	/* (number) */ break; } case 39: {
		pc++;
		push(new Integer(code[pc]));
	/* (function) */ break; } case 40: {
		pc++;
		push(functions[code[pc]]);
	/* * */ break; } case 41: {
		int i = ((Integer)pop()).intValue();
		int j = ((Integer)pop()).intValue();
		push(new Integer(j * i));
	/* % */ break; } case 42: {
		int i = ((Integer)pop()).intValue();
		int j = ((Integer)pop()).intValue();
		push(new Integer(j % i));
	}
			}
		}
	}

}
