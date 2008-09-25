import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;
import java.io.IOException;

public final class Globals extends Function {
	private Hashtable globals;
	private int fn;
	private static String names[] = { 
		"set-global", 	// value string -> (none)
		"get-global", 	// string -> value
		"parse"};	// inputstream -> function

	private Globals (int fn, Hashtable globals) {
		this.fn = fn;
		this.globals = globals;
	}

	private Function parseNext(InputStream is) {
		try {
			int c;
	
			c = is.read();
	
			// skip white spaces
			while(c <= ' ' || c == '#') {
				// skip comments
				if(c == '#') {
					do {
						c = is.read();
					} while(c != '\n' && c != -1);
				}
				if(c == -1) {
					return null;
				}
				c = is.read();
			}
	
			// parse integer
			if('0' <= c && c <= '9') {
				int i = 0;
				do {
					i = i * 10 + c - '0';
					c = is.read();
				} while('0' <= c && c <= '9');
				return new Immediate(new Integer(i));
	
			// parse string
			} else if(c == '"') {
				StringBuffer sb = new StringBuffer();
				c = is.read();
				while(c != '"') {
					// support char escape 
					if(c == '\\') {
						c = is.read();
						if(c == 'n') {
							c = '\n';
						} else if(c == 't') {
							c = '\t';
						}
					}
					sb.append((char) c);
					c = is.read();
				}
				return new Immediate(sb.toString());
	
			// array literal
			} else if(c == '(') {
				Stack s = new Stack();
				Object o = parseNext(is);
	
				while(o != null) {
					s.push(o);
					o = parseNext(is);
				}
	 
				Function code[] = new Function[s.size()];
				for(int i = 0; i < s.size(); i++) {
					code[i] = (Function)s.elementAt(i);
				}
				return new Immediate(code);
	
			// terminator
			} else if(c == ')' || c == -1) {
				return null;
	
			// word
			} else {
				StringBuffer sb = new StringBuffer();
				do {
					sb.append((char)c);
					c = is.read();
				} while(c > ' ');
				Object o = globals.get(sb.toString());
				if(o == null) {
					System.out.println("Unknown symbol: " + sb);
				}
				return (Function)o;
			}
	
		} catch(IOException e) {
			return null;
		}
	}

	public void apply(Stack s) {
		switch(fn) {
/* set-global */ case 0: {
		Object key = s.pop();
		Object val = s.pop();
		if(val == null) {
			globals.remove(key);
		} else {
			globals.put(key, val);
		}
} break;
/* get-global */ case 1: {
		s.push(globals.get(s.pop()));
} break;
/* parse */ case 2: {
		s.push(parseNext((InputStream)s.pop()));
} break;
		}
	}

	public static void loadTo(Hashtable vm) {
		int i;
		for(i=0;i<names.length;i++) {
			vm.put(names[i], new Globals(i, vm));
		}
	}

	public String toString() {
		return names[fn];
	}
}
