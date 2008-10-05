import java.io.*;
import java.util.*;

class Yolan implements Yoco {
	int fn;
	public Object apply(Object[] args) {
		switch(fn) {
			default:
				return null;
		}
	}

	public Yolan(int fn) {
		this.fn = fn;
	}

	static String macronames[] = {"default"};
	static String varnames[] = {"print"};
	static Hashtable macros;
	static Hashtable vars;
	static Stack saves;
	static {
		int i;
		macros = new Hashtable();
		vars = new Hashtable();
		setvar("macros", macros);
		saves = new Stack();
		i = macronames.length;
		while(i>0) {
			i--;
			macros.put(macronames[i], new Yolan(i));
		};
		i = varnames.length;
		while(i>0) {
			i--;
			vars.put(varnames[i], new Yolan(i+macronames.length));
		};
	}

	public static Object call(Object name, Object args[]) {
		return ((Yoco)vars.get(name)).apply(args);
	}

	public static void setvar(Object key, Object val) {
		if(val == null) {
			vars.remove(key);
		} else {
			vars.put(key, val);
		}
	}

	public static Object[] parse(InputStream is) throws Exception {
		int c;
		Stack s = new Stack();
		c = is.read();
		while(c != -1 && c != ')') {
			if(c <= ' ') {
				c = is.read();
			} else if(c == '(') {
				s.push(parse(is));
				c = is.read();
			} else {
				StringBuffer sb = new StringBuffer();
				while(c != '(' && c != ')' && c > ' ') {
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
		System.out.print('(');
		for(int i = 0; i < o.length; i++) {
			if(i!=0) {
				System.out.print(' ');
			}
			if(o[i] instanceof Object[]) {
				print((Object[]) o[i]);
			} else {
				print(o[i]);
			}
		}
		System.out.print(')');
	}

	public static void print(Object o) {
		System.out.print(o.toString());
	}

	public static void main(String args[]) throws Exception {
		InputStream is = new FileInputStream(new File(args[0]));
		print(parse(is));
		System.out.println();
	}
}
