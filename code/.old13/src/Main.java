import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


final public class Main extends MIDlet {
	TextCanvas tc = new TextCanvas();
	protected void destroyApp(boolean uncond) {
	};
	protected void startApp() {
		(Display.getDisplay(this)).setCurrent(tc);

	};
	public void print(Object o) {
		tc.print(o.toString());
	}
	protected void pauseApp() {

	};
}
