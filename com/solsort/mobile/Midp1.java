package com.solsort.mobile;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class Midp1 implements LightScriptFunction {

    int fn;
    static Display disp;

    public Object apply(Object[] args, int argpos, int argcount) throws LightScriptException {
        switch (fn) {
            case 0: { // Menu
                String name;
                if (argcount == 0) {
                    name = "menu";
                } else {
                    name = (String) args[argpos + 1];
                }
                return new MidpMenu(name);
            }
            case 1: { // addItem
                MidpMenu midpmenu = (MidpMenu) args[argpos];
                midpmenu.addItem((String) args[argpos + 1],
                        argcount < 2
                        ? null
                        : (LightScriptFunction) args[argpos + 2]);
                return midpmenu;
            }
            case 2: { // show
                MidpMenu midpmenu = (MidpMenu) args[argpos];
                midpmenu.show(disp);
                return midpmenu;
            }
            case 3: { // textinput(title, [text,] callback)
                String s = "";
                if(argcount > 2) {
                    s = (String)args[argpos+2];
                }
                return new MidpTextBox(disp, (String)args[argpos+1], s, (LightScriptFunction)args[argpos+argcount]);
            }

        }
        return null;
    }

    private Midp1(int fn) {
        this.fn = fn;
    }

    public static void register(MIDlet mid, LightScript ls) {
        disp = Display.getDisplay(mid);
        
        Class cls = new MidpMenu("").getClass();
        ls.set("Menu", new Midp1(0));
        ls.setMethod(cls, "addItem", new Midp1(1));
        ls.setMethod(cls, "show", new Midp1(2));
        ls.set("TextInput", new Midp1(3));

    }
}
