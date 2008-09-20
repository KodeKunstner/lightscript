
import java.util.Enumeration;
import java.util.Stack;
import java.util.Hashtable;
import java.io.InputStream;

final class Yolan extends Stack {
	public Hashtable globals;
	private Stack functions;
	private Stack literals;
	private Hashtable literalid; 
	private int prevpos;
	private static final String builtins[] = { "print", "+", "<", "if-else", "drop", "defun", "[", "]", "repeat-until", "dup", "dup2", "a.pop", "a.push", "a.is-empty", "dup3", "print-stack", "dup4", "swap", "rot", "a.copy", "a.reverse", "call2f", "a.iterator", "more-elements", "next-element", "not", "while", "dup4", "put", "put2", "put3", "string-join"};
	private static final int builtincount = builtins.length;
	private static final char firstliteral = 5000;
	public static final Boolean t = new Boolean(true);

	private char getid(Object val) {
		Object oid = literalid.get(val);
		char id;
		if(oid == null) {
			id = (char)(literals.size()+firstliteral);
			literals.push(val);
			literalid.put(val, new Character(id));
			return id;
		} else {
			return ((Character)oid).charValue();
		}

	}

	public char[] compile(InputStream is) throws Exception {
		int c;
		StringBuffer sb =  new StringBuffer();
		while((c = nextSymb(is)) != -1) {
			sb.append((char)c);
		}
		return sb.toString().toCharArray();
	}

	public int nextSymb(InputStream is) throws Exception {
		StringBuffer sb = new StringBuffer();
		int c = is.read();

		// skip whitespaces 
		while(c <= ' ') {
			c = is.read();
			if(c == -1) {
				return -1;
			}
		}

		// parse string
		if(c == '\"') {
			while((((c = is.read()) == '\"' && (c = is.read()) <= ' ' )?-1:c) != -1) {
				sb.append((char)c);
			}
			return getid(sb.toString());
		} 
		
		// compile lambda
		if(c == '{') {
			return getid(compile(is));
		}

		// end of lambda symbol
		if(c == '}') {
			return -1;
		} 
		
		// parse integer or word
		do {
			sb.append((char)c);
		} while((c = is.read()) > ' ');

		String s = sb.toString();

		// parse integer
		try {
			int i = Integer.parseInt(s);
			return getid(new Integer(i));

		// or parse word
		} catch(Exception e) {
			Object o = globals.get(s);
			if(o == null) {
				Util.print("Error, unknown word: " + s);
				return -1;
			} else {
				return ((Character)o).charValue();
			}
		}
	}

	public Yolan() {
		prevpos = 0;
		globals = new Hashtable();
		literalid = new Hashtable();
		literals = new Stack();
		functions = new Stack();
		for(char i = 0; i < builtincount; i++) {
			char fn[] = {i};
			globals.put(builtins[i], new Character(i));
		}
	}

	public void eval(InputStream is) throws Exception {
		char expr[] = new char[1];
		int i;
		while((i = nextSymb(is)) != -1) {
			expr[0] = (char) i;
			eval(expr);
		}
	}

	private void eval(char code[]) throws Exception {
		int pc;
		for(pc = 0; pc < code.length; pc++) {
			switch(code[pc]) {
/* BEGIN SWITCH*/

    
/* print*/ case  0: 
    { 
		Util.print(pop());
	
    } 
    break; 
    
/* +*/ case  1: 
    { 
		push(new Integer(
			((Integer)pop()).intValue() +
			((Integer)pop()).intValue()));
	
    } 
    break; 
    
/* <*/ case  2: 
    { 
		push( (((Integer)pop()).intValue() >
			((Integer)pop()).intValue())?t:null);
	
    } 
    break; 
    
/* if-else*/ case  3: 
    { 
		if(pop() == null) {
			pop();
			eval((char[])pop());
		} else {
			char [] expr = (char [])pop();
			pop();
			eval(expr);
		}
	
    } 
    break; 
    
/* drop*/ case  4: 
    { 
		pop();
	
    } 
    break; 
    
/* defun*/ case  5: 
    { 
		String name = (String) pop();
		Object codetext = pop();
		globals.put(name, new Character((char)(functions.size() + builtincount)));
		functions.push(codetext);
	
    } 
    break; 
    
/* [*/ case  6: 
    { 
		push(new Integer(prevpos));
		prevpos = size();
	
    } 
    break; 
    
/* ]*/ case  7: 
    { 
		Stack s = new Stack();
		int count = size() - prevpos;
		s.setSize(size() - prevpos);
		for(int i = count - 1; i >= 0; i--) {
			s.setElementAt(elementAt(prevpos + i), i);
		}
		setSize(prevpos);
		prevpos = ((Integer)pop()).intValue();
		push(s);
	
    } 
    break; 
    
/* repeat-until*/ case  8: 
    { 
		char [] expr = (char []) pop();
		do {
			eval(expr);
		} while(pop() == null);
	
    } 
    break; 
    
/* dup*/ case  9: 
    { 
		push(peek());
	
    } 
    break; 
    
/* dup2*/ case  10: 
    { 
		push(elementAt(size() - 2));
	
    } 
    break; 
    
/* a.pop*/ case  11: 
    { 
		push(((Stack)pop()).pop());
	
    } 
    break; 
    
/* a.push*/ case  12: 
    { 
		Object o = pop();
		((Stack)peek()).push(o);
	
    } 
    break; 
    
/* a.is-empty*/ case  13: 
    { 
		push((((Stack)pop()).size() == 0)?t:null);
	
    } 
    break; 
    
/* dup3*/ case  14: 
    { 
		push(elementAt(size() - 3));
	
    } 
    break; 
    
/* print-stack*/ case  15: 
    { 
		Util.print(this.toString());
	
    } 
    break; 
    
/* dup4*/ case  16: 
    { 
		push(elementAt(size() - 4));
	
    } 
    break; 
    
/* swap*/ case  17: 
    { 
		Object a, b;
		a = pop();
		b = pop();
		push(a);
		push(b);
	
    } 
    break; 
    
/* rot*/ case  18: 
    { 
		Object a, b, c;
		a = pop();
		b = pop();
		c = pop();
		push(b);
		push(a);
		push(c);
	
    } 
    break; 
    
/* a.copy*/ case  19: 
    { 
		Stack src, dst;
		src = (Stack)pop();
		dst = new Stack();
		dst.setSize(src.size());
		for(int i = src.size() - 1; i != 0; i--) {
			dst.setElementAt(src.elementAt(i), i);
		}
		push(dst);
	
    } 
    break; 
    
/* a.reverse*/ case  20: 
    { 
		Object t;
		Stack s = (Stack)peek();
		int a = 0, b = s.size() - 1;
		while(a < b) {
			t = s.elementAt(a);
			s.setElementAt(s.elementAt(b), a);
			s.setElementAt(t, b);
			a++; b--;
		}
	
    } 
    break; 
    
/* call2f*/ case  21: 
    { 
		eval((char []) pop());
	
    } 
    break; 
    
/* a.iterator*/ case  22: 
    { 
		push(((Stack)pop()).elements());
	
    } 
    break; 
    
/* more-elements*/ case  23: 
    { 
		push(((Enumeration)pop()).hasMoreElements()?t:null);
	
    } 
    break; 
    
/* next-element*/ case  24: 
    { 
		push(((Enumeration)pop()).nextElement());
	
    } 
    break; 
    
/* not*/ case  25: 
    { 
		push((pop() == null)?t:null);
	
    } 
    break; 
    
/* while*/ case  26: 
    { 
		char [] cond, expr;
		expr = (char [])pop();
		cond = (char [])pop();
		eval(cond);
		while(pop() != null) {
			eval(expr);
			eval(cond);
		}
	
    } 
    break; 
    
/* dup4*/ case  27: 
    { 
		push(elementAt(size() - 4));
	
    } 
    break; 
    
/* put*/ case  28: 
    { 
		setElementAt(pop(), size() - 1);
	
    } 
    break; 
    
/* put2*/ case  29: 
    { 
		setElementAt(pop(), size() - 2);
	
    } 
    break; 
    
/* put3*/ case  30: 
    { 
		setElementAt(pop(), size() - 3);
	
    } 
    break; 
    
/* string-join*/ case  31: 
    { 
		StringBuffer sb = new StringBuffer();
		Stack s = (Stack)pop();
		for(int i = 0; i<s.size();i++) {
			sb.append(s.elementAt(i).toString());
		}
		push(sb.toString());
	
    } 
    break; 
	/* Literals or userdefined */ default:
		if(code[pc] >= firstliteral) {
			push(literals.get(code[pc] - firstliteral));
		} else {
			eval((char [])functions.get(code[pc] - builtincount));
		}
/* END OF SWITCH */
			}
		}
	}
}

