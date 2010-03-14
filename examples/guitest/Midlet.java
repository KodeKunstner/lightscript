import javax.microedition.midlet.MIDlet;
import com.solsort.mobile.LightScript;
import com.solsort.mobile.Midp1;

public class Midlet extends MIDlet {

    protected void startApp() {
        LightScript ls = new LightScript();
        try {
            Midp1.register(ls, this, "foo");
            ls.eval(this.getClass().getResourceAsStream("script.ls"));
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }
}
