package com.solsort.mobile;

import java.util.Stack;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import com.solsort.lightscript.Function;
import com.solsort.lightscript.LightScriptException;

public final class MidpMenu implements CommandListener, Runnable {

    private List menu;
    private Stack menuHandlers;

    public void show(Display disp) {
        disp.setCurrent(menu);
    }

    public MidpMenu() {
        menu = new List("", List.IMPLICIT);
        menuHandlers = new Stack();
        menu.setCommandListener(this);
        menu.addCommand(new Command("-", Command.OK, 0));
    }

    public void addItem(String text, Function callback) {
        if (callback != null) {
            text = "- " + text;
        }
        menu.append(text, null);
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
                ((Function) o).apply(new Object[1], 0, 0);
            } catch (LightScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
