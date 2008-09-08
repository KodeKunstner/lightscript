import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


final public class Main extends MIDlet {
	Form form = new Form("MobyScript");
	String code = "{\"consts\":[1,\"a\",\"b\",0,\"t\",\"Fibonaccital under 1000:\",\"print\",1000,\"<\",\"+\"],\"code\":[0,0,5,1,0,0,5,2,0,3,5,4,0,5,2,6,1,1,2,1,0,7,2,8,1,2,4,28,2,1,2,6,1,1,2,2,5,4,2,2,2,1,2,9,1,2,5,2,2,4,5,1,6,4,3,-38]}";
	protected void destroyApp(boolean uncond) {
	};
	protected void startApp() {
		(Display.getDisplay(this)).setCurrent(form);
		Print.main = this;
		Print.print("Hello 2");
		Object o = null;
		try {
		o = JSON.parse(new ByteArrayInputStream(code.getBytes()));
		} catch(Exception e) {
		}

		MobyVM vm = new MobyVM();
		vm.eval(vm.json2code(o));
		Print.print(o);

	};
	public void print(Object o) {
		form.append(new StringItem("", o.toString()));
	}
	protected void pauseApp() {

	};
}
