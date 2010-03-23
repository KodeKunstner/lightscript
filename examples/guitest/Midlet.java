import javax.microedition.midlet.MIDlet;
import com.solsort.lightscript.*;
import javax.microedition.lcdui.*;

public class Midlet extends MIDlet implements LightScriptFunction {
    Display disp;
    MidpMenu menu;

    public Object apply(java.lang.Object[] args, int argpos, int argcount) {
        menu.addItem(String.valueOf(args[argpos+1]), null);
        return null;
    }

    protected void startApp() {
        disp = Display.getDisplay(this);

        Alert alert = new Alert("HERE!");
        disp.setCurrent(alert);
        menu = new MidpMenu();
        menu.addItem("Hello", null);
        menu.addItem("world", null);
        menu.show(disp);
        LightScript ls = new LightScript();
        try {
            Midp1.register(ls, this, "foo");
            ls.set("print", this);
            ls.eval(this.getClass().getResourceAsStream("script.ls"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }
}
