package com.solsort.mobile;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import com.solsort.lightscript.Function;
import com.solsort.lightscript.LightScriptException;

public final class MidpTextBox implements CommandListener, Runnable {

    Function callback;
    String text;

    public MidpTextBox(Display disp, String title, String text, Function callback) {
        TextBox textbox = new TextBox(title, text, 10000, TextField.ANY);
        textbox.addCommand(new Command("...", Command.OK, 0));
        textbox.setCommandListener(this);
        disp.setCurrent(textbox);
        this.callback = callback;
    }

    public void commandAction(Command c, Displayable d) {
        text = ((TextBox)d).getString();
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        Object args[] = {null, text};
        try {
            callback.apply(args, 0, 1);
        } catch (LightScriptException ex) {
            ex.printStackTrace();
        }
    }
}
