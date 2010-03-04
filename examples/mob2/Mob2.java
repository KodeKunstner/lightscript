package examples.mob2;
import java.io.InputStream;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import com.solsort.mobile.*;

public class Mob2 extends MIDlet implements LightScriptFunction {
    static Form form;

    public void startApp() {
        form = new Form("Mob2");
        Display.getDisplay(this).setCurrent(form);
     
//	InputStream is = this.getClass().getResourceAsStream("test.js");
        LightScript ls = new LightScript();
        ls.set("print", this);
        try {
            ls.eval("print('Hello world')");
        } catch(LightScriptException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public Object apply(Object[] args, int argpos, int argcount) throws LightScriptException {
        Object obj = args[argpos + 1];
        Mob2.form.append(obj==null?"nil":obj.toString());
        return obj;
    }
}
