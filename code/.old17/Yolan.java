import java.io.*;
import java.util.*;

class Yolan {
	Object o;
	int fn;
	public Object apply(Object[] X) {
		switch(fn) {
			case 0: return X;
			case 1: return num(ival(e(X)[1]) + ival(X[2]));
			case 2: return num(ival(e(X)[1]) - ival(X[2]));
			case 3: return num(ival(e(X)[1]) * ival(X[2]));
			case 4: return num(ival(e(X)[1]) / ival(X[2]));
			case 5: print(e(X)[1]); return X[1];
			case 6: return Integer.valueOf(sval(X[1]));
			default: return null;
		}
	}


	public Yolan(int fn) {
		this.fn = fn;
	}

	static String sval(Object o) {
		return (String)o;
	}
	static Object eval(Object X[]) {
		return ((Yolan)globals.get(X[0])).apply(X);
	}

	static Object[] e(Object[]X) {
		for(int i = 1;i<X.length;i++) {
			Object o = X[i];
			if(o instanceof Object[]) {
				Object os[] = (Object[])o;
				X[i] = ((Yolan)globals.get(os[0])).apply(os);
			} else {
				X[i] = globals.get(o);
			}
		}
		return X;
	}
	static Hashtable globals = new Hashtable();
	static {
		String names = "' + - * / print num ";
		int i = 0, j = 0, k = 0;
		while(i < names.length()) {
			if(names.charAt(i) == ' ') {
				globals.put(names.substring(j, i), new Yolan(k));
				k++;
				i++;
				j = i;
			}
			i++;
		}
	}

	public static Object num(int i) {
		return new Integer(i);
	}
	public static int ival(Object o) {
		return ((Integer)o).intValue();
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
				while(c != '[' && c != ']' && c > ' ') {
					sb.append((char) c);
					c = is.read();
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
			System.out.print(o.toString());
		}
	}
	public static void evalblock(Object X[]) {
		for(int i = 0; i < X.length; i++) {
			eval((Object[])X[i]);
		}
	}

	public static void main(String X[]) throws Exception {
		InputStream is = new FileInputStream(new File(X[0]));
		evalblock(parse(is));
		System.out.println();
	}
}
