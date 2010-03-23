package examples.moby;
import java.io.InputStream;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import com.solsort.lightscript.*;

public class Moby extends MIDlet implements Function {
    static Form form;

    public void startApp() {
        form = new Form("Moby");
        Display.getDisplay(this).setCurrent(form);

//	InputStream is = this.getClass().getResourceAsStream("test.js");
        LightScript ls = new LightScript();
        ls.set("print", this);
        try {
            ls.eval("print('Hello world')");
        } catch (LightScriptException e) {
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
        Moby.form.append(obj==null?"nil":obj.toString());
        return obj;
    }
}
