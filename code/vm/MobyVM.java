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

	public Object eval(MobyCode m) {
		int pc = 0;
		byte[] code = m.code;
		Object tmp;

		int clen = m.code.length;
		for(;;) {
			switch(code[pc]) {
case 0: // array_put
case 1: // arrjoin
case 2: // arrpop
case 3: // arrput
case 4: // eq
	push(pop().equals(pop())?t:f);
	break;
case 5: // getlocal
case 6: // global_get
case 7: // global_set
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
case 13: // multiput
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
	push(m.literals[code[pc]]);
	break;
case 23: // pushnil
	push(null);
	break;
case 24: // ???
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
case 28: // strcat
	
default: 
			}
			pc++;
		}
	}
}
