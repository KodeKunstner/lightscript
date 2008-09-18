import java.util.Stack;
import java.util.Hashtable;
import java.io.InputStream;

class Yolan extends Stack {
	Hashtable symbol; // globals
	static Stack literal = new Stack();
	static Hashtable literalid = new Hashtable();
	static char cs[];
	static int pos;
	static final char firstliteral = 5000;
	static String[] builtin = { "}", "print", "+", "<", "if-else"};

	char getid(Object val) {
		Object oid = literalid.get(val);
		char id;
		if(oid == null) {
			id = (char)(literal.size()+firstliteral);
			literal.push(val);
			literalid.put(new Character(id), literal);
			return id;
		} else {
			return ((Character)oid).charValue();
		}

	}

	public void compile(StringBuffer acc) {
		StringBuffer sb = new StringBuffer();
		char c;

		while(cs[pos] <= ' ') {
			pos++;
		}

		c = cs[pos];
		if(c == '\'') {
			pos++;
			while(cs[pos] != '\'') {
				sb.append(cs[pos]);
				pos++;
			}
			pos++;
			acc.append(getid(sb.toString()));
			
		} else if(c == '{') {
			pos++;
			do {
				compile(sb);
			} while(sb.charAt(sb.length() - 1) != 0);
			sb.setLength(sb.length() - 1);
			acc.append(getid(sb.toString().toCharArray()));
		} else {
			do {
				sb.append(c);
				pos++;
				c = cs[pos];
			} while(c > ' ');
			String s = sb.toString();
			try {
				int i = Integer.parseInt(s);
				acc.append(getid(new Integer(i)));
			} catch(Exception e) {
				acc.append(((Character)symbol.get(sb.toString())).charValue());
			}
		}
	}

	public Yolan() {
		symbol = new Hashtable();
		for(char i = 0; i < builtin.length; i++) {
			symbol.put(builtin[i], new Character(i));
		}
	}

	public void eval(String s) {
		cs = s.toCharArray();
		char [] cs2;
		pos = 0;
		for(;;) {
			StringBuffer acc = new StringBuffer();
			compile(acc);
			cs2 = acc.toString().toCharArray();
			for(int i=0;i<cs2.length;i++) {
		//		System.out.println((int)(short)cs2[i]);
			}
			eval(cs2);
			System.out.println(this.toString() + pos);
		}
	}
	private void eval(char code[]) {
		int pc;
		for(pc = 0; pc < code.length; pc++) {
			switch(code[pc]) {
/* BEGIN SWITCH*/
	/* print */ case 1: 
	{
		System.out.println(pop().toString());
	}
	/* + */ break; case 2: 
	{
		push(new Integer(
			((Integer)pop()).intValue() +
			((Integer)pop()).intValue()));
	}
	/* < */ break; case 3: 
	{
		push( (((Integer)pop()).intValue() >
			((Integer)pop()).intValue())?code:null);
	}
	/* if-else */ break; case 4: 
	{
		Object else_expr = pop();
		Object then_expr = pop();
		if(pop() != null) {
			eval((char[])then_expr);
		} else {
			eval((char[])else_expr);
		}
	}
	/* Literals or newly defined */ break; default:
		if(code[pc] >= firstliteral) {
			push(literal.get(code[pc] - firstliteral));
		} else {
			eval((char [])literal.get(code[pc]));
		}
/* END OF SWITCH */
			}
		}
	}
}
