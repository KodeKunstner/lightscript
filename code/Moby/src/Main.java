import java.io.IOException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class Main extends MIDlet implements Function {
    static Form form;
    public void startApp() {
        form = new Form("Moby");
        Display.getDisplay(this).setCurrent(form);
        Yolan.addFunction("println", this);
        try {
            Yolan.eval(this.getClass().getResourceAsStream("test.yl"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Object apply(Yolan[] args) {
        Object result = args[0].value();
        Main.form.append(result==null?"nil":result.toString());
        return result;
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
