import java.util.Stack;
import java.util.Hashtable;
import java.io.InputStream;

class Yolan {
	Hashtable globals; // globals
	int fp; // frame pointer
	Stack stack; // data stack
	static int c;
	static InputStream is;
	static Stack functions = new Stack();
	static Stack literals = new Stack();
	static Hashtable literals_ids = new Hashtable();
	static Hashtable opcodes;

	static {
		opcodes = new Hashtable();
		String str[] = {"get-arg", "arg-count"};
		for(int i=0;i<str.length;i++) {
			opcodes.put(str[i], new Integer(i));
		}
	}

	public Yolan(InputStream is) {
		this.is = is;
		c = ' ';
	}

	static int getLiteral(Object literal) {
		Object oid = literals_ids.get(literal);
		if(oid == null) {
			int id = literals.size();
			literals_ids.put(literal, new Integer(id));
			literals.push(literal);
			return id;
		} else {
			return ((Integer)oid).intValue();
		}
	}

	public Object parse() throws Exception {
		while(c <= ' ') {
			if(c == -1) {
				return null;
			}
			c = is.read();
		}

		if(c == '(') {
			Stack s = new Stack();
			c = is.read();
			Object o = parse();
			while(o != null) {
				s.push(o);
				o = parse();
			}
			return s;
		} else if(c == ')') {
			c = is.read();
			return null;
		} else {
			StringBuffer sb = new StringBuffer();
			do {
				if(c == '\\') {
					c = is.read();
				}
				sb.append((char) c);
				c = is.read();
			} while(c > ')');
			return sb.toString();
		}
	}

	public void yol2str(Object o, StringBuffer sb, int indent) {
		if(o instanceof Stack) {
			Stack s = (Stack) o;
			sb.append("[");
			indent = indent + 2;
			for(int i=0;i<s.size();i++) {
				if(i != 0) {
					sb.append(" ");
				}
				yol2str(s.elementAt(i), sb, indent);
			}
			sb.append("]");
		} else {
			sb.append(o.toString());
		}
	}
}
