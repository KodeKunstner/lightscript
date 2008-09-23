import java.util.Hashtable;
import java.util.Stack;

final class Yolan extends Hashtable {

	public Yolan() {
		put("set-global", new Globals(this, true));
		put("get-global", new Globals(this, false));
		put("eval", new Eval());
		BaseLib.addTo(this);
	}

	public void call(String function, Stack s) {
		s.push(get(function));
		((Function)get("eval")).apply(s);
	}

	public void crudeEval(String text, Stack s) {
		Object code[] = new Object[1];
		Function eval = (Function) get("eval");

		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			// skip whitespaces
			if(c <= ' ') {
				continue;
			}

			// parse integer
			{
				boolean readint = false;
				int ival = 0;
				while('0' <= c && c <= '9') {
					ival = ival * 10 + c - '0';
					i++;
					c = text.charAt(i);
					readint = true;
				}
				if(readint) {
					s.push(new Integer(ival));
					continue;
				}
			}

			// parse symbol
			{ 
				int startpos = i;
				while(c > ' ') {
					i++;
					c = text.charAt(i);
				}
				code[0] = get(text.substring(startpos, i));
				s.push(code);
				eval.apply(s);
			}
		}
	}
}
