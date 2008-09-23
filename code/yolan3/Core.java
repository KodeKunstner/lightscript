import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;
import java.io.IOException;

public class Core extends Function {
	private Hashtable globals;
	private int fn;
	private static String names[] = { "set-global", "get-global", "parse", "compile" };

	private Core (int fn, Hashtable globals) {
		this.fn = fn;
		this.globals = globals;
	}

	private Object parseNext(InputStream is) {
		try {
			int c;
	
			c = is.read();
	
			// skip white spaces
			while(c <= ' ') {
				c = is.read();
				// skip comments
				if(c == '#') {
					do {
						c = is.read();
					} while(c != '\n' && c != -1);
				}
				if(c == -1) {
					return null;
				}
			}
	
			// parse integer
			if('0' <= c && c <= '9') {
				int i = 0;
				do {
					i = i * 10 + c - '0';
					c = is.read();
				} while('0' <= c && c <= '9');
				return new Integer(i);
	
			// parse string
			} else if(c == '"') {
				StringBuffer sb = new StringBuffer();
				c = is.read();
				while(c != '"') {
					// support char escape 
					if(c == '\\') {
						c = is.read();
					}
					sb.append((char) c);
					c = is.read();
				}
				return sb.toString();
	
			// array literal
			} else if(c == '{') {
				Stack s = new Stack();
				Object o = parseNext(is);
				Object[] oa;
	
				while(o != null) {
					s.push(o);
					o = parseNext(is);
				}
	
				return s;
	
			// terminator
			} else if(c == '}' || c == -1) {
				return null;
	
			// word
			} else {
				StringBuffer sb = new StringBuffer();
				do {
					sb.append((char)c);
					c = is.read();
				} while(c > ' ');
				return globals.get(sb.toString());
			}
	
		} catch(IOException e) {
			return null;
		}
	}

	public void apply(Stack s) {
		switch(fn) {
/* set-global */ case 0: {
		globals.put(s.pop(), s.pop());
} break;
/* get-global */ case 1: {
		s.push(globals.get(s.pop()));
} break;
/* parse */ case 2: {
		s.push(parseNext((InputStream)s.pop()));
} break;
/* compile */ case 3: {
		s.push(new Code((Stack)s.pop()));
} break;
		}
	}

	/**
	 * Factory, create functions for accessing the globals, 
	 * and add them to the global-table/vm.
	 */
	public static void loadTo(Hashtable vm) {
		int i;
		for(i=0;i<names.length;i++) {
			vm.put(names[i], new Core(i, vm));
		}
	}

	public String toString() {
		return names[fn];
	}
}
