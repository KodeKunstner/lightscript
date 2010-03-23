import javax.microedition.midlet.MIDlet;
import com.solsort.lightscript.*;

public class Midlet extends MIDlet {
    protected void startApp() {
        LightScript ls = new LightScript();
        try {
            Midp1.register(ls, this, "storage-name");
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
