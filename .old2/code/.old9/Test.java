import java.util.Stack;
class Test{
	public static void main(String[] args) throws Exception {
		Yolan yl = new Yolan();
		yl.crudeEval("1 2 33 char-to-string 5 6 print-stack ", new Stack());
	}
}
