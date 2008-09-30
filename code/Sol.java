import java.io.*;
import java.util.*;

class Sol extends Stack {
	static Boolean t = new Boolean(true);
	static Boolean f = new Boolean(false);
	InputStream is;
	Stack symbs;
	Hashtable symbmap;
	Object that;
	Hashtable globals;
	Object locals[];
	Stack localstack;
	static String builtin[] = { "<", "<=", "===", "||", "-", "!", "!==", "&&", "+", "(array-to-object)", "(call)", "(drop)", "false", "println", "(get)", "(get-global)", "(get-local)", "if", "if-else", "(initialise-function)", "(new-array)", "(put)", "return", "(set-global)", "(set-local)", "true", "while", "stackdump", "push", "length", "getch", "load" };
	static int opcount = builtin.length;

	public void eval(char code[]) {
		for(int pc = 0; pc < code.length; pc++) {
			switch(code[pc]) {
/* ### BEGINNING OF SWITCH ### */
	/* "<" */ case 0:
		{
			Object o1 = pop();
			if(o1 instanceof String) {
				push(((String)o1).compareTo((String)pop())>0?t:f);
			} else {
				push(((Integer)o1).intValue() > ((Integer)pop()).intValue()?t:f);
			}
		}
	/* "<=" */ break; case 1:
		{
			Object o1 = pop();
			if(o1 instanceof String) {
				push(((String)o1).compareTo((String)pop())>=0?t:f);
			} else {
				push(((Integer)o1).intValue() >= ((Integer)pop()).intValue()?t:f);
			}
		}
	/* "===" */ break; case 2:
		{
			push(pop().equals(pop())?t:f);
		}
	/* "||" */ break; case 3:
		{
			Object o1 = pop();
			Object o2 = pop();
			push((o1 != f)?o1:o2);
		}
	/* "-" */ break; case 4:
		{
			push(new Integer(-((Integer)pop()).intValue() + ((Integer)pop()).intValue()));
		}
	/* "!" */ break; case 5:
		{
			push(pop() == f?t:f);
		}
	/* "!==" */ break; case 6:
		{
			push(pop().equals(pop())?f:t);
		}
	/* "&&" */ break; case 7:
		{
			Object o1 = pop();
			Object o2 = pop();
			push((o1 != f)?o2:f);
		}
	/* "+" */ break; case 8:
		{
			push(new Integer(((Integer)pop()).intValue() + ((Integer)pop()).intValue()));
		}
	/* "(array-to-object)" */ break; case 9:
		{
			Hashtable h = new Hashtable();
			Stack s = (Stack)pop();
			for(int i = 0; i < s.size(); i+= 2) {
				h.put(s.elementAt(i), s.elementAt(i+1));
			}
		}
	/* "(call)" */ break; case 10:
		{
			int thatpos = size() - ((Integer)pop()).intValue() - 2;

			// save the this-pointer
			Object tmp = elementAt(thatpos);
			setElementAt(that, thatpos);
			that = tmp;

			// call the function
			eval((char[])elementAt(thatpos + 1));

			tmp = peek();

			// restore the this-pointer
			that = elementAt(thatpos);
			setSize(thatpos);
			push(tmp);
		}
	/* "(drop)" */ break; case 11:
		{
			pop();
		}
	/* "false" */ break; case 12:
		{
			push(f);
		}
	/* unused */ break; case 13:
		{
			System.out.println(pop());
		}
	/* "(get)" */ break; case 14:
		{
			Object key = pop();
			Object container = pop();
			if(container instanceof Hashtable) {
				push(((Hashtable)container).get(key));
			} else if(container instanceof Stack) {
				push(((Stack)container).elementAt(((Integer)key).intValue()));
			} else if(container instanceof String) {
				push(new Integer(((String)container).charAt(((Integer)key).intValue())));
			}
		}
	/* "(get-global)" */ break; case 15:
		{
			push(globals.get(pop()));
		}
	/* "(get-local)" */ break; case 16:
		{
			push(locals[((Integer)pop()).intValue()]);
		}
	/* "if" */ break; case 17:
		{
			char branch1[] = (char []) pop();
			if(pop() != f) {
				eval(branch1);
			}
		}
	/* "if-else" */ break; case 18:
		{
			char branch2[] = (char []) pop();
			char branch1[] = (char []) pop();
			if(pop() != f) {
				eval(branch1);
			} else {
				eval(branch1);
			}
		}
	/* "(initialise-function)" */ break; case 19:
		{
			int localc = ((Integer)pop()).intValue();
			int argc = ((Integer)pop()).intValue();
			localstack.push(locals);
			locals = new Object[localc];
			while(argc > 0) {
				argc--;
				locals[argc] = pop();
			}
		}
	/* "(new-array)" */ break; case 20:
		{
			push(new Stack());
		}
	/* "(put)" */ break; case 21:
		{
			Object val = pop();
			Object key = pop();
			Object container = peek();
			if(container instanceof Hashtable) {
				((Hashtable)container).put(key, val);
			} else { // if(container instanceof Stack) {
				((Stack)container).setElementAt(val, ((Integer)key).intValue());
			} 
		}
	/* "return" */ break; case 22:
		{
			locals = (Object[]) localstack.pop();
			return;
		}
	/* "(set-global)" */ case 23:
		{
			globals.put(pop(), pop());
		}
	/* "(set-local)" */ break; case 24:
		{
			int pos = ((Integer)pop()).intValue();
			locals[pos] = pop();
		}
	/* "true" */ break; case 25:
		{
			push(t);
		}
	/* "while" */ break; case 26:
		{
			char body[] = (char []) pop();
			char cond[] = (char []) pop();
			eval(cond);
			while(pop() != f) {
				eval(body);
				eval(cond);
			}
		}
	/* "stackdump" */ break; case 27:
		{
			System.out.println("### BEGIN STACKDUMP ###");
			for(int i = 0; i < size(); i++) {
				System.out.println("" + i + ":\t" + elementAt(i));
			}
			System.out.println("### END STACKDUMP ###");
		}
	/* "push" */ break; case 28:
		{
			Object val = pop();
			((Stack)peek()).push(val);
		}
	/* "length" */ break; case 29:
		{
			Object o = pop();
			if(o instanceof String) {
				push(new Integer(((String)o).length()));
			} else if(o instanceof Stack) {
				push(new Integer(((Stack)o).size()));
			}
		}
	/* "getch" */ break; case 30:
		{
			int c = -1;
			try {
				c = System.in.read();
			} catch(IOException e) {
			}
			if(c == -1) {
				push(f);
			} else {
				push(String.valueOf((char) c));
			}
		}
	/* "load" */ break; case 31:
		{
		}
	/* default case */ break; default:
		{
			push(symbs.elementAt(code[pc] - opcount));
		}
/* ### END OF SWITCH ### */
			}
		}
	}

	private int getId(Object o) {
		Object tmp = symbmap.get(o);
		if(tmp == null) {
			int result = symbs.size() + opcount;
			symbmap.put(o, new Integer(result));
			symbs.push(o);
			return result;
		} else {
			return ((Integer)tmp).intValue();

		}
	}

	private int nextc() {
		try {
			return is.read();
		} catch(Exception e) {
			return -1;
		}

	}

	public void printsymb(int i) {
		if(i < opcount) {
			System.out.println(builtin[i]);
		} else {
			Object o = symbs.elementAt(i-opcount);
			if(o instanceof String) {
				System.out.println("\"" + o + "\"");
			} else if(o instanceof char[]) {
				char cs[] = (char[]) o;
				System.out.println("{");
				for(int j = 0; j < cs.length; j++) {
					printsymb(cs[j]);
				}
				System.out.println("}");
			} else {
				System.out.println(symbs.elementAt(i-opcount));
			}
		}
	}

	public int parseNext() {
		int c = nextc();
		while(c <= ' ') {
			c = nextc();
			if(c == -1 ) {
				return -1;
			}
		}

		if('0' <= c  && c <= '9') {
			int result = 0;
			do {
				result = result * 10 + c - '0';
				c = nextc();
			} while ('0' <= c && c <= '9');
			return getId(new Integer(result));

		} else if(c == '{') {
			StringBuffer sb = new StringBuffer();
			int i = parseNext();
			while(i != -1) {
				sb.append((char) i);
				i = parseNext();
			}
			return getId(sb.toString().toCharArray());
		} else if(c == '}') {
			return -1;
		} else if(c == '"') {
			StringBuffer sb = new StringBuffer();
			c = nextc();
			while(c != '"' && c != -1) {
				if(c == '\\') {
					c = nextc();
					if(c == 'n') {
						c ='\n';
					} else if(c == 't') {
						c ='\t';
					}
				}
				sb.append((char) c);
				c = nextc();
			}
			return getId(sb.toString());

		// read symbol
		} else {
			StringBuffer sb = new StringBuffer();
			do {
				sb.append((char)c);
				c = nextc();
			} while(c > ' ');

			String s = sb.toString();
			for(int i = 0; i < opcount; i++) {
				if(builtin[i].equals(s)) {
					return i;
				}
			}
			System.out.println("Unknown symbol: " + s);
		}

		return -1;
	}

	public Sol(InputStream is) {
		this.is = is;
		symbs = new Stack();
		symbmap = new Hashtable();
		globals = new Hashtable();
		locals = null;
		localstack = new Stack();
	}

	public static void main(String args[]) throws Exception {
		Sol sol = new Sol(new FileInputStream(new File(args[0])));
		char code[] = new char[1];

		int i = sol.parseNext();
		while(i != -1) {
			sol.printsymb((char)i);
			code[0] = (char)i;
			sol.eval(code);
			i = sol.parseNext();
		}
	}
}
