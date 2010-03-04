import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

public final class Builtin extends Function {
	private static Boolean t = new Boolean(true);
	private Hashtable globals;
	private int fn;
	private static String names[] = { 
		"swap", 	// (a b -> b a)
		"print-stack", 	// ( -> )
		"print", 	// (string ->)
		"dup", 		// (a -> a a)
		"compile", 	// (code -> Function)
		"apply", 	// (... Function -> ...)
		"eval",		// (... code -> ... )
		"open-input-file", 	// (filename -> inputstream)
		"input-stream-read",  	// (inputstream -> single-char-string)
		"drop",		// (a -> )
		"do-while", 	// (... loop-code  -> ...)
		"while", 	// (cond-code loop-code -> ...)
		"drop2", 	// (a b -> )
		"{","[","[]",	// empty stack (-> stack)
		"a.new",
		",",":","]",	// stack push (stack elem -> stack)
		"a.push",
		"{}", "o.new",	// empty object (-> object)
		"}",		// push, and convert to object (stack elem -> object)
		"+",		// (int int -> int)
		"-",		// (int int -> int)
		"*",		// (int int -> int)
		"/",		// (int int -> int)
		"o.get",	// (object id -> value)
		"o.put",	// (object id value -> )
		"dup2",		// (a b ->  a b a)
		"str<=",	// (string1 string2 ->  bool)
		"and",		// (bool expr ->  bool)
		"or",		// (bool expr ->  bool)
		"if-else",	// (bool expr expr -> )
		"dup3",		// (a b c ->  a b c a)
		"noop"};

	private Builtin(int fn) {
		this.fn = fn;
	}

	public void apply(Stack s) {
		switch(fn) {
/* BEGINNING OF BUILTIN FUNCTIONS */
/* swap */ case 0: 
	{
		Object t1, t2;
		t1 = s.pop();
		t2 = s.pop();
		s.push(t1);
		s.push(t2);
	} break;
/* print-stack */ case 1:
	{
		System.out.println("### BEGIN STACKDUMP ###");
		for(int i = s.size() - 1; i >= 0; i--) {
			System.out.println(new Immediate(s.elementAt(i)));
		}
		System.out.println("### END STACKDUMP ###");
	} break;
/* print */ case 2:
	{
		System.out.println(s.pop());
	} break;
/* dup */ case 3:
	{
		s.push(s.peek());
	} break;
/* compile */ case 4:
	{
		s.push(new Code((Function [])s.pop()));
	} break;
/* apply */ case 5:
	{
		((Function)s.pop()).apply(s);
	} break;
/* eval */ case 6:
	{
		Code.eval((Function [])s.pop(), s);
	} break;
/* open input file */ case 7:
	{
		try {
			s.push(new FileInputStream(new File((String)s.pop())));
		} catch (Exception e) {
			s.push(null);
		}
	} break;
/* input-stream-read */ case 8:
	{
		int c = -1;
		try {

			c = ((InputStream)s.pop()).read();
		} catch (Exception e) {
		}
		if(c == -1) {
			s.push(null);
		} else {
			s.push(String.valueOf((char) c));
		}
	} break;
/* drop */ case 9:
	{
		s.pop();
	} break;
/* do-while */ case 10:
	{
		Function code[] = (Function[]) s.pop();
		do {
			Code.eval(code, s);
		} while(s.pop() != null);
	} break;
/* while */ case 11:
	{
		Function code[] = (Function[]) s.pop();
		Function cond[] = (Function[]) s.pop();
		Code.eval(cond, s);
		while(s.pop() != null) {
			Code.eval(code, s);
			Code.eval(cond, s);
		}
	} break;
/* drop2 */ case 12:
	{
		s.pop(); s.pop();
	} break;
/* { [ [] a.new */ case 13: case 14: case 15: case 16:
	{
		s.push(new Stack());
	} break;
/* , : ] a.push */ case 17: case 18: case 19: case 20:
	{
		Object o = s.pop();
		Stack s2 = (Stack)s.peek();
		s2.push(o);
	} break;
/* {} o.new */ case 21: case 22: 
	{
		s.push(new Hashtable());
	} break;
/* } */ case 23: 
	{
		Hashtable h = new Hashtable();
		Object o = s.pop();
		Stack s2 = (Stack)s.pop();
		s2.push(o);
		for(int i = 0; i < s2.size(); i+= 2) {
			h.put(s2.elementAt(i), s2.elementAt(i+1));
		}
		s.push(h);
	} break;
/* + */ case 24:
	{
		s.push(new Integer(((Integer)s.pop()).intValue() + ((Integer)s.pop()).intValue()));
	} break;
/* - */ case 25:
	{
		s.push(new Integer(-((Integer)s.pop()).intValue() + ((Integer)s.pop()).intValue()));
	} break;
/* * */ case 26:
	{
		s.push(new Integer(((Integer)s.pop()).intValue() * ((Integer)s.pop()).intValue()));
	} break;
/* / */ case 27:
	{
		s.push(new Integer(((Integer)s.pop()).intValue() / ((Integer)s.pop()).intValue()));
	} break;
/* o.get */ case 28:
	{
		s.push(((Hashtable)s.pop()).get(s.pop()));
	} break;
/* o.put */ case 29:
	{
		((Hashtable)s.pop()).put(s.pop(), s.pop());
	} break;
/* dup2 */ case 30:
	{
		s.push(s.elementAt(s.size() - 2));
	} break;
/* str<= */ case 31:
	{
		String s2 = (String)s.pop();
		String s1 = (String)s.pop();
		s.push((s1.compareTo(s2) <= 0)?t:null);
	} break;
/* and */ case 32:
	{
		Function code[] = (Function[])s.pop();
		if(s.peek() != null) {
			s.pop();
			Code.eval(code, s);
		}
	} break;
/* or */ case 33:
	{
		Function code[] = (Function[])s.pop();
		if(s.peek() == null) {
			s.pop();
			Code.eval(code, s);
		}
	} break;
/* if-else */ case 34:
	{
		Function then_branch[] = (Function[])s.pop();
		Function else_branch[] = (Function[])s.pop();
		if(s.pop() != null) {
			Code.eval(then_branch, s);
		} else {
			Code.eval(else_branch, s);
		}
	} break;
/* dup3 */ case 35:
	{
		s.push(s.elementAt(s.size() - 3));
	} break;
/* END OF BUILTIN FUNCTIONS */
		}
	}

	/**
	 * Factory, create functions for accessing the globals, 
	 * and add them to the global-table/vm.
	 */
	public static void loadTo(Hashtable vm) {
		int i;
		for(i=0;i<names.length;i++) {
			vm.put(names[i], new Builtin(i));
		}
	}

	public String toString() {
		return names[fn];
	}
}
