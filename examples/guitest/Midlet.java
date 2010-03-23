import javax.microedition.midlet.MIDlet;
import com.solsort.lightscript.*;
import com.solsort.mobile.*;

public class Midlet extends MIDlet {
    protected void startApp() {
        LightScript ls = new LightScript();
        try {
            Midp1.register(ls, this, "storage-name");
            ls.eval(this.getClass().getResourceAsStream("script.ls"));
            HTTPClient http = new HTTPClient();
            http.openUrl("http://www.lightscript.net/", (Function)ls.get("f"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }
}
