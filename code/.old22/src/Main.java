/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;

/**
 * @author voel
 */
public class Main extends MIDlet {
    static GUI gui;
    static TextDisplay txt;
    public static void print(String s) {
        System.err.print(s);
        txt.println(s);
        gui.repaint();
    }
    public void startApp() {
        Display disp = Display.getDisplay(this);
        gui = new GUI();
        Display.getDisplay(this).setCurrent(gui);
        txt = new TextDisplay(0, 0, gui.getWidth(), gui.getHeight());
        gui.drawables = new Object[1];
        gui.drawables[0] = txt;
        try {
            Yolan.eval(this.getClass().getResourceAsStream("main.yl"));
        } catch (IOException e) {
            throw new Error(e.toString());
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
