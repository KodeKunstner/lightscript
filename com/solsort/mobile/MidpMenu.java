package com.solsort.mobile;

import java.util.Stack;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

class MidpMenu implements CommandListener, Runnable {

    private List menu;
    private Stack menuHandlers;
    private static Image linkLogo;

    static {
        linkLogo = Image.createImage(5, 5);
        linkLogo.getGraphics().drawRect(1, 1, 3, 3);
    }

    public void show(Display disp) {
        disp.setCurrent(menu);
    }

    public MidpMenu(String name) {
        menu = new List(name, List.IMPLICIT);
        menuHandlers = new Stack();
        menu.setCommandListener(this);
        menu.addCommand(new Command("...", Command.OK, 0));
    }

    public void addItem(String text, LightScriptFunction callback) {
        menu.append(text, callback == null ? null : linkLogo);
        menuHandlers.push(callback);
    }

    public void commandAction(Command c, Displayable d) {
        Thread t = new Thread(this);
        t.start();
    }


    public void run() {
        int i = menu.getSelectedIndex();
        Object o = menuHandlers.elementAt(i);
        if (o != null) {
            try {
                ((LightScriptFunction) o).apply(new Object[1], 0, 0);
            } catch (LightScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
