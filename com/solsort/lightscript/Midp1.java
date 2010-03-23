package com.solsort.lightscript;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import com.solsort.mobile.*;

public class Midp1 implements Function {

    Object closure;
    int fn;
    static Display disp;

    public Object apply(Object[] args, int argpos, int argcount) throws LightScriptException {
        switch (fn) {
        case 0: { // Menu
            return new MidpMenu();
        }
        case 1: { // addItem
            LightScript ls = (LightScript) closure;
            MidpMenu midpmenu = (MidpMenu) args[argpos];
            midpmenu.addItem(ls.toString(args[argpos + 1]),
                             argcount < 2
                             ? null
                             : (Function) args[argpos + 2]);
            return midpmenu;
        }
        case 2: { // show
            MidpMenu midpmenu = (MidpMenu) args[argpos];
            midpmenu.show(disp);
            return midpmenu;
        }
        case 3: { // textinput(title, [text,] callback)
            LightScript ls = (LightScript) closure;
            String s = "";
            if (argcount > 2) {
                s = ls.toString(args[argpos + 2]);
            }
            return new MidpTextBox(disp, ls.toString(args[argpos + 1]), s, (Function) args[argpos + argcount]);
        }
        case 4: {
            LightScript ls = (LightScript) closure;
            return ((MidpStorage) args[argpos]).get(ls.toString(args[argpos + 1]));
        }
        case 5: {
            LightScript ls = (LightScript) closure;
            ((MidpStorage) args[argpos]).set(ls.toString(args[argpos + 1]), ls.toString(args[argpos + 2]));
            return args[argpos];
        }
        case 6: { // storage __iter__
            return new Util(Util.ENUMERATION_ITERATOR, ((MidpStorage) args[argpos]).keys());
        }

        }
        return null;
    }

    private Midp1(int fn, Object o) {
        this.fn = fn;
        this.closure = o;
    }

    private Midp1(int fn) {
        this.fn = fn;
    }

    /**
     * Registers the following functions within the runtime:
     * <ul>
     * <li>Menu() - creates a new menu</li>
     * <li>menu.addItem(text [, callback])</li>
     * <li>menu.show()</li>
     * <li>TextInput(prompt[, intialValue], callback)</li>
     * <li>Storage.set(key, value)</li>
     * <li>Storage.get(key)</li>
     * </ul>
     * @param ls
     * @param mid
     * @param recordStoreName
     * @throws LightScriptException
     */
    public static void register(LightScript ls, MIDlet mid, String recordStoreName) throws LightScriptException {
        disp = Display.getDisplay(mid);

        Class menuClass = new MidpMenu().getClass();
        ls.set("Menu", new Midp1(0));
        ls.setMethod(menuClass, "addItem", new Midp1(1, ls));
        ls.setMethod(menuClass, "show", new Midp1(2));
        ls.set("TextInput", new Midp1(3, ls));

        MidpStorage storage = MidpStorage.openStorage(recordStoreName);
        ls.set("Storage", storage);
        Class storageClass = storage.getClass();
        Util.setGetter(ls, storageClass, new Midp1(4, ls));
        ls.setMethod(storageClass, "get", new Midp1(4, ls));
        ls.setMethod(storageClass, "__setter__", new Midp1(5, ls));
        ls.setMethod(storageClass, "set", new Midp1(5, ls));
        ls.setMethod(storageClass, "__iter__", new Midp1(6));
    }
}
