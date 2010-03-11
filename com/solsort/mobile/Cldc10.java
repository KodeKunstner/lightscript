package com.solsort.mobile;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import com.solsort.mobile.LightScriptFunction;
import com.solsort.mobile.LightScriptException;

/**
 * StdLibity functions
 */
public final class Cldc10{
    private static Hashtable cookies = new Hashtable();

    /**
     * @todo extract to other class, get LightScriptLightScriptFunction as callback
     * @param url
     * @return
     * @throws IOException
     */
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

