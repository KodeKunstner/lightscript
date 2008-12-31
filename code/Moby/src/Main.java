import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class Main extends MIDlet {
    static Form form;
    public void startApp() {
        form = new Form("Moby");
        Display.getDisplay(this).setCurrent(form);
        Yolan.addFunction("println", new Printer());
        try {
            Yolan.eval(this.getClass().getResourceAsStream("test.yl"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
