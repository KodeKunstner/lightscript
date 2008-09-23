import java.util.Stack;
import java.util.Hashtable;

final class BaseLib extends Function {
	private int function;

	private BaseLib(int i) {
		function = i;
	}

	public static void addTo(Hashtable globals) {
		for(int i = 0; i < functions.length; i++) {
			globals.put(functions[i], new BaseLib(i));
		}
	}

	private static String functions[] = {
		"print-stack",
		"char-to-string",
		"dup",
		"dup2",
		"dup3",
		// 5 ops so far
		"dup4",
		"dup5",
		"dup6",
		"dupn",
		"drop",
		// 5 ops so far
		"drop2",
		"drop3",
		"drop4",
		"drop5",
		"drop6",
		// 10 ops so far
		"dropn",
		"put2",
		"put3",
		"put4",
		"put5",
		// 15 ops so far
		"put6",
		"putn",
		// 20 ops so far
		// 25 ops so far
		""};

	public void apply(Stack s) {
		switch(function) {
/* ### BEGINNING OF SWITCH ### */
/* print-stack */ case 0:
	{
		System.out.println(s);
	}
/* char-to-string */ break; case 1:
	{
		s.push(String.valueOf((char)((Integer)s.pop()).intValue()));
	}
/* dup dup2, ... */ break; case 2: 
	{
		int n = function - 1; // 1 is opcode-1
		s.push(s.elementAt(s.size() - n));
	}
/* dupn */ break; case 7: 
	{
		int n = ((Integer) pop()).intValue();
		s.push(s.elementAt(s.size() - n));
	}
/* drop drop2, ... */ break; case 8: 
	{
		int n = function - 7; // 7 is opcode-1
		s.setSize((s.size() - n));
	}
/* dropn */ break; case 13: 
	{
		int n = ((Integer) pop()).intValue();
		s.setSize((s.size() - n));
	}
/* put2, put3, ... */ break; case 14: 
	{
		int n = function - 12; // 12 is opcode-2
		s.setElementAt(s.size() - n));
	}
/* putn */ break; case 14: 
	{
		int n = ((Integer) pop()).intValue();
		s.setElementAt(s.size() - n));
	}
/* ### END OF SWITCH ### */
		}
	}
}
