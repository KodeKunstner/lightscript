/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * @author voel
 */
public class ZuiMidlet extends MIDlet {
    public void startApp() {
        Zui zui = new Zui();
        Display.getDisplay(this).setCurrent(zui);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
