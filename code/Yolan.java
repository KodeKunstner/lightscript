import java.io.*;
import java.util.*;

class Yolan {
	Hashtable globals;

	static Object t = new Boolean(true);
	static Object f = null;

	interface Yoco {
		public Object apply(Object X[]);
	}

	static String eager[] = {"+", "-", "*", "/", "print", "<"};
	class Eager implements Yoco {
		int fn;
		public Eager(int fn) {
			this.fn = fn;
		}
		public Object apply(Object X[]) {
			Object a0 = null, a1 = null, a2 = null;
			switch(X.length) {
				case 4:
					a2 = eval(X[3]);
				case 3:
					a1 = eval(X[2]);
				case 2:
					a0 = eval(X[1]);
			}

			switch(fn) {
				case 0: return num(ival(a0) + ival(a1));
				case 1: return num(ival(a0) - ival(a1));
				case 2: return num(ival(a0) * ival(a1));
				case 3: return num(ival(a0) / ival(a1));
				case 4: print(a0); return a0;
				case 5: return ival(a0) < ival(a1)?a0:null;
			}
			return null;
		}
	}

	static String lazy[] = {"num", "if"};
	class Lazy implements Yoco {
		int fn;
		public Lazy(int fn) {
			this.fn = fn;
		}
		public Object apply(Object X[]) {
			switch(fn) {
				case 0: return Integer.valueOf((String)X[1]);
				case 1: return (eval(X[1]) != null)?eval(X[2]):eval(X[3]);
			}
			return null;
		}
	}


	public static int ival(Object o) {
		return ((Integer)o).intValue();
	}

	public static Object num(int i) {
		return new Integer(i);
	}

	public Object eval(Object o) {
		if(o instanceof Object[]) {
			Object X[] = ((Object [])o);
			o = X[0];
			if(!(o instanceof Yoco)) {
				X[0] = o = globals.get(o);
			}
			return ((Yoco)o).apply(X);
		} else {
			Object tmp;
			tmp = globals.get(o);
			return (tmp==null)?o:tmp;
		}
	}

	public Yolan() {
		globals = new Hashtable();
		for(int i = 0; i<eager.length;i++) {
			globals.put(eager[i], new Eager(i));
		}
		for(int i = 0; i<lazy.length;i++) {
			globals.put(lazy[i], new Lazy(i));
		}
	}

	public static Object[] parse(InputStream is) throws Exception {
		int c;
		Stack s = new Stack();
		c = is.read();
		while(c != -1 && c != ']') {
			if(c <= ' ') {
				c = is.read();
			} else if(c == '[') {
				s.push(parse(is));
				c = is.read();
			} else {
				StringBuffer sb = new StringBuffer();
				boolean quoted = false;
				while((quoted && c != 0) || (c > ' ' && c != '[' && c!= ']')) {
					if(c == '"') {
						quoted = !quoted;
						c = is.read();
					} else {
						if(c == '\\') {
							c = is.read();
						}
						sb.append((char) c);
						c = is.read();
					}
				}
				s.push(sb.toString());
			}
		}
		Object result[] = new Object[s.size()];
		s.copyInto(result);
		return result;
	}

	public static void print(Object o[]) {
		System.out.print('[');
		for(int i = 0; i < o.length; i++) {
			if(i!=0) {
				System.out.print(' ');
			}
			print(o[i]);
		}
		System.out.print(']');
	}

	public static void print(Object o) {
		if(o instanceof Object[]) {
			print((Object[]) o);
		} else {
			System.out.print("\"" + o.toString() + "\"");
		}
	}

	public static void main(String X[]) throws Exception {
		InputStream is = new FileInputStream(new File(X[0]));
		(new Yolan()).eval(parse(is)[0]);
		System.out.println();
	}
}
