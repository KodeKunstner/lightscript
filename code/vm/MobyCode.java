import java.util.Hashtable;
import java.util.Stack;

final class MobyCode extends Hashtable {
	public byte[] code;
	public Object[] literals;

	public MobyCode(Object o) {
		Stack v;
		int i;

		v = (Stack) ((Hashtable) o).get("code");
		i = v.size();
		code = new byte[i];
		do {
			i--;
			code[i] = ((Integer)v.elementAt(i)).byteValue();
		} while(i>0);

		v = (Stack) ((Hashtable) o).get("literals");
		literals = new Object[v.size()];
		v.copyInto(literals);
	}
}
