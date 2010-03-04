package com.solsort.mobile;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

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

    public static void qsort(Stack arr, int first, int last, LightScriptFunction cmp) throws LightScriptException {
        Object args[] = {arr, null, null};
        while (first < last) {
            int l = first;
            int r = last;
            Object pivot = arr.elementAt((l + r) / 2);
            arr.setElementAt(arr.elementAt(r), (l + r) / 2);
            arr.setElementAt(pivot, r);

            while (l < r) {
                --l;
                do {
                    ++l;
                    args[1] = arr.elementAt(l);
                    args[2] = pivot;
                } while (((Integer) cmp.apply(args, 0, 2)).intValue() <= 0 && l < r);
                if (l < r) {
                    arr.setElementAt(arr.elementAt(l), r);
                    r--;
                }
                ++r;
                do {
                    r--;
                    args[1] = pivot;
                    args[2] = arr.elementAt(r);
                } while (((Integer) cmp.apply(args, 0, 2)).intValue() <= 0 && l < r);
                if (l < r) {
                    arr.setElementAt(arr.elementAt(r), l);
                    l++;
                }
            }
            arr.setElementAt(pivot, r);
            qsort(arr, l + 1, last, cmp);
            last = l - 1;
        }
    }
    private static Hashtable cookies = new Hashtable();

    public static InputStream openUrl(String url) throws IOException {
        HttpConnection con = (HttpConnection) Connector.open(url);

        StringBuffer sb = new StringBuffer();
        String sep = "";
        for (Enumeration e = cookies.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            sb.append(sep);
            sb.append(key);
            sb.append("=");
            sb.append(cookies.get(key));
            sep = "&";
            con.setRequestProperty("Cookie", key + "=" + cookies.get(key));
        }
        con.setRequestProperty("User-Agent", "LightScript mobile application");

        String key;
        for (int i = 0; (key = con.getHeaderFieldKey(i)) != null; ++i) {
            System.out.println(key.toString());
            if (key.toLowerCase().equals("set-cookie")) {
                String cookie = con.getHeaderField(i);
                int eqPos = cookie.indexOf("=");
                int semiPos = cookie.indexOf(";");
                if (semiPos == -1) {
                    semiPos = cookie.length();
                }

                cookies.put(cookie.substring(0, eqPos),
                        cookie.substring(eqPos + 1, semiPos));
            }
        }
        return con.openInputStream();
    }
}

