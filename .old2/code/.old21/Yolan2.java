import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.StringBuffer;
import java.util.Stack;
import java.util.Hashtable;

class Yolan2 implements Function {
	private int fn;
	private static String[] names = {"println", "to-string", "open-input-stream", "parse", "compile"};
	private static Function functions[] = null;

	public Object apply(Object [] args) {
		switch(fn) 
	{ /* println*/ case 0: {
		String s = args[1].toString();
		System.out.println(s);
		return s;
	} /* to-string*/ case 1: {
		Object o = args[1];
		if(o instanceof Object[]) {
			Object[] os = (Object[]) o;
			StringBuffer sb = new StringBuffer("(");
			for(int i = 0; i < os.length; i++) {
				sb.append(" ");
				args[1] = os[i];
				sb.append(this.apply(args));
			}
			sb.append(" )");
			return sb.toString();
		} else {
			return o.toString();
		}
	} /* open-input-stream */ case 2: {
		try {
			return new FileInputStream(new File(args[1].toString()));
		} catch(IOException e) {
			return null;
		}
	} /* parse */ case 3: {
		try {
			InputStream is = (InputStream)args[1];
			int ch = is.read();
	
			Stack result = new Stack();
	
			for(;;) {
				StringBuffer str = new StringBuffer();
	
				if(ch < 0 || ch == ')') {
					return stack2objs(result);
				} else if(ch == ';') {
					do {
						ch = is.read();
					} while(ch >= ' ');
				} else if(ch == '(') {
					result.push(this.apply(args));
					ch = is.read();
				} else if(ch == '"') {
					ch = is.read();
					while(ch != '"' && ch >= 0) {
						if(ch == '\\') {
							ch = is.read();
							if('0' <= ch && ch <= '7') {
								ch = ch * 64 + is.read() * 8 + is.read();
								ch -= '0' * (64 + 8 + 1);
							}
						}
						str.append((char)ch);
						ch = is.read();
					}
					ch = is.read();
					Object os[] = new Object[2];
					os[0] = "string";
					os[1] = str.toString();
					result.push(os);
				} else if('0' <= ch && ch <= '9') {
					do {
						str.append((char)ch);
						ch = is.read();
					} while('0' <= ch);
					Object os[] = new Object[2];
					os[0] = "number";
					os[1] = str.toString();
					result.push(os);
				} else if('*' <= ch) {
					do {
						str.append((char)ch);
						ch = is.read();
					} while('*' <= ch);
					result.push(str.toString());
				} else {
					ch = is.read();
				}
			}
		} catch(IOException e) {
			return null;
		}

	} /* compile*/ case 4: {
		// compiler args: [parent, ["compiler", macro-hashtable, code-accumulator,...], code]
		Object code = args[2];
		Hashtable macros = (Hashtable) ((Object [])args[1])[1];
		Function macro = null;

		if(code instanceof Object[]) {
			if(((Object[])code).length > 0) {
				macro = (Function) macros.get(((Object[])code)[0]);
			} else {
				macro = (Function) macros.get("()");
			}


		} else if(code instanceof String) {
			macro = (Function) macros.get("symbol macro");
		}

		if(macro == null) {
			macro = (Function) macros.get("default macro");
		}

		return macro.apply(args);

	} /* wrong function id */ default: {
		throw new Error("Undefined function");
	}}}

	private Yolan2(int fn) {
		this.fn = fn;
	}

	public static Function getFunction(String s) {
		if(functions == null) {
			int count = names.length;
			functions = new Function[count];
			for(int i = 0; i < count; i++) {
				functions[i] = new Yolan2(i);
			}
		}

		int i = 0;
		while(!s.equals(names[i])) {
			i++;
		}
		return functions[i];
	}

	public static Object call(String s, Object o1) {
		Object os[] = { null, o1 };
		return getFunction(s).apply(os);
	}

	public static Object call(String s, Object o1, Object o2) {
		Object os[] = { null, o1, o2 };
		return getFunction(s).apply(os);
	}

	public static Object call(String s, Object o1, Object o2, Object o3) {
		Object os[] = { null, o1, o2, o3 };
		return getFunction(s).apply(os);
	}



	//
	// Utility functions
	//
	public static Object[] stack2objs(Stack s) {
		Object os[] = new Object[s.size()];
		s.copyInto(os);
		return os;
	}

	public static void main(String[] args) throws java.io.IOException {
		call("println", call("to-string", call("parse", call("open-input-stream", args[0]))));

		//InputStream is = new FileInputStream(new File(args[0]));
		//System.out.println(print_r(parse(is)));
	}
}
