import java.util.Stack;
import java.util.Hashtable;
import java.io.InputStream;

final class Yolan extends Stack {
	public Hashtable globals;
	private Stack functions;
	private Stack literals;
	private Hashtable literalid;
	private static final String builtins[] = { "print", "+", "<", "if-else", "pop", "defun"};
	private static final int builtincount = builtins.length;
	private static final char firstliteral = 5000;
	public static final Boolean t = new Boolean(true);

	char getid(Object val) {
		Object oid = literalid.get(val);
		char id;
		if(oid == null) {
			id = (char)(literals.size()+firstliteral);
			literals.push(val);
			literalid.put(new Character(id), literals);
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


		// skip whitespaces and comments
		while(c <= ' ' /*|| c == '('*/) {
			c = is.read();
			if(c == -1) {
				return -1;
			}
		}

		// parse string
		if(c == '\'') {
			while((((c = is.read()) == '\'' && (c = is.read()) <= ' ' )?-1:c) != -1) {
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
			return ((Character)globals.get(s)).charValue();
		}
	}

	public Yolan() {
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
	/* print */ case 0: 
	{
		Util.print(pop());
	}
	/* + */ break; case 1: 
	{
		push(new Integer(
			((Integer)pop()).intValue() +
			((Integer)pop()).intValue()));
	}
	/* < */ break; case 2: 
	{
		push( (((Integer)pop()).intValue() >
			((Integer)pop()).intValue())?t:null);
	}
	/* if-else */ break; case 3: 
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
	/* pop */ break; case 4: 
	{
		pop();
	}
	/* defun */ break; case 5: 
	{
		String name = (String) pop();
		Object codetext = pop();
		globals.put(name, new Character((char)(functions.size() + builtincount)));
		functions.push(codetext);
	}
	/* Literals or userdefined */ break; default:
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
