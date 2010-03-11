package com.solsort.mobile;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;
import javax.microedition.io.Connector;
import com.solsort.mobile.LightScriptFunction;
import com.solsort.mobile.LightScriptException;

/**
 * Utility functions
 */
public final class Util {

    public static final Object emptyObjectArray[] = {};

    public static int arraysearch(Object[] os, Object o) {
        for (int i = 0; i < os.length; ++i) {
            if (os[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public static Object[] stack2array(Stack s) {
        if (s.empty()) {
            return emptyObjectArray;
        }
        Object[] result = new Object[s.size()];
        s.copyInto(result);
        return result;
    }

    public static void convertToString(Object o, StringBuffer sb) {
        if (o instanceof Object[]) {
            String sep = "";
            Object[] os = (Object[]) o;

            sb.append("[");
            for (int i = 0; i < os.length; ++i) {
                sb.append(sep);
                convertToString(os[i], sb);
                sep = ", ";
            }
            sb.append("]");
        } else if (o instanceof Stack) {
            String sep = "";
            Stack s = (Stack) o;

            sb.append("[");
            for (int i = 0; i < s.size(); ++i) {
                sb.append(sep);
                convertToString(s.elementAt(i), sb);
                sep = ", ";
            }
            sb.append("]");
        } else if (o instanceof Hashtable) {
            String sep = "";
            Hashtable h = (Hashtable) o;
            sb.append("{");
            for (Enumeration e = h.keys(); e.hasMoreElements();) {
                Object key = e.nextElement();
                sb.append(sep);
                convertToString(key, sb);
                sb.append(": ");
                convertToString(h.get(key), sb);
                sep = ", ";
            }
            sb.append("}");
        } else if (o instanceof String) {
            sb.append("\"");
            sb.append(o);
            sb.append("\"");
        } else {
            sb.append(o);
        }
    }
}

