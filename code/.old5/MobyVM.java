import java.util.Hashtable;
import java.util.Stack;

final class MobyVM extends Stack {

	public final Hashtable globals;
	public final Stack stack;
	public static final Boolean t = new Boolean(true);
	public static final Boolean f = new Boolean(false);
	public static final Integer zero = new Integer(0);
	public static final Integer one = new Integer(1);
	public static final Integer two = new Integer(2);

	public MobyVM() {
		stack = new Stack();
		globals = new Hashtable();
	}

	public Object[] json2code(Object o) {
		Stack v;
		Object [] result;
		short []code;
		int i;

		v = (Stack) ((Hashtable) o).get("code");
		i = v.size();
		code = new short[i];
		do {
			i--;
			code[i] = ((Integer)v.elementAt(i)).shortValue();
		} while(i>0);

		v = (Stack) ((Hashtable) o).get("literals");
		result = new Object[v.size() + 1];
		v.copyInto(result);
		result[result.length - 1] = code;
		return result;
		
	}

	public Object eval(Object[] literals) {
		short[] code = (short[])literals[literals.length - 1];
		int pc = 0;
		Object tmp;

		int clen = code.length;
		for(;;) {
			Print.print("" + pc + ": " + code[pc] + ", " + code[pc +1]);
			switch(code[pc]) {
case 0: // arrpush
	tmp = pop();
	((Stack)peek()).push(tmp);
	break;
case 1: // arrjoin
{
	StringBuffer str = new StringBuffer("");
	tmp = (String)pop();
	Stack s = (Stack)pop();
	int sz;
	sz = s.size();
	if(sz > 0) {
		str.append(s.elementAt(0));
		for(int i = 1;i < sz;i++) {
			str.append(tmp);
			str.append(s.elementAt(i));
		}

	}
	push(str.toString());
}
	break;
case 2: // arrpop
	push(((Stack)pop()).pop());
	break;
case 3: // jump
	pc++;
	pc += code[pc];
	break;
case 4: // eq
	push(pop().equals(pop())?t:f);
	break;
case 5: // getlocal
	pc++;
	push(elementAt(size() - code[pc]));
	break;
case 6: // global_get
	pc++;
	push(globals.get(literals[code[pc]]));
	break;
case 7: // global_set
	pc++;
	globals.put(literals[code[pc]], pop());
	break;
case 8: // iadd
	push(new Integer(((Integer)pop()).intValue() + ((Integer)pop()).intValue()));
	break;
case 9: // isub
	push(new Integer(((Integer)pop()).intValue() - ((Integer)pop()).intValue()));
	break;
case 10: // leq
{
	boolean b;
	tmp = pop();
	if(tmp instanceof Integer) {
		b = (((Integer)tmp).intValue() >= ((Integer)pop()).intValue());
	} else {
		b = (((String)tmp).compareTo((String)pop()) >= 0);
	}
	push(b?t:f);
}
	break;

case 11: // less than
{
	boolean b;
	tmp = pop();
	if(tmp instanceof Integer) {
		b = (((Integer)tmp).intValue() > ((Integer)pop()).intValue());
	} else {
		b = (((String)tmp).compareTo((String)pop()) > 0);
	}
	push(b?t:f);
}
	break;

case 12: // multiget
{
	tmp = pop();
	if(tmp instanceof Stack) {
		try {
			push(((Stack)tmp).elementAt(((Integer)pop()).intValue()));
		} catch(IndexOutOfBoundsException e) {
			push(null);
		}
	} else {
		push(((Hashtable)tmp).get(pop()));
	}
}
	break;
case 13: // multiput
{
	Object t2, t3;
	tmp = pop();
	t2 = pop();
	t3 = pop();
	if(tmp instanceof Stack) {
		try {
			((Stack)tmp).setElementAt(t3, ((Integer)t2).intValue());
		} catch(IndexOutOfBoundsException e) {
			((Stack)tmp).setSize(((Integer)t2).intValue() + 1);
			((Stack)tmp).setElementAt(t3, ((Integer)t2).intValue());
		}
	} else /* hash table */ {
		if(t3 == null) {
			((Hashtable)tmp).remove(t2);
		} else {
			((Hashtable)tmp).put(t2, t3);
		}
	}
}
	break;
case 14: // neg
	push(new Integer(- ((Integer)pop()).intValue()));
	break;
case 15: // neq
	push(pop().equals(pop())?f:t);
	break;
case 16: // not
	tmp = pop();
	push((tmp == null || tmp == f)?t:f);
	break;
case 17: // object_put
{
	Object val, key;
	val = pop();
	key = pop();
	((Hashtable)peek()).put(key, val);
}
	break;
case 18: // pop
	pop();
	break;
case 19: // push_emptyarray
	push(new Stack());
	break;
case 20: // push_emptyobject
	push(new Hashtable());
	break;
case 21: // pushfalse
	push(f);
	break;
case 22: // pushliteral
	pc++;
	push(literals[code[pc]]);
	break;
case 23: // pushnil
	push(null);
	break;
case 24: // condjumpfalse
	pc++;
	tmp = pop();
	if(tmp == null || tmp == f) {
		pc += code[pc];
	}
	break;
case 25: // pushtrue
	push(t);
	break;
case 26: // return n
	{
	pc++;
	Object o = pop();
	setSize(size() - code[pc]);
	return o;
	}
case 27: // setlocal
	pc++;
	setElementAt(pop(), size() - code[pc]);
	break;
default: 
			}
			pc++;
		}
	}
}
