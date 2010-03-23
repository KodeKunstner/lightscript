package com.solsort.mobile;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import com.solsort.lightscript.Function;
import com.solsort.lightscript.LightScriptException;

public final class HTTPClient {
    private Hashtable cookies;

    public HTTPClient() {
        cookies = new Hashtable();
    }

    public void openUrl(String url, Function callback) {
        (new Thread(new RequestThread(url, callback))).start();
    }

    private class RequestThread implements Runnable {
        private Function callback;
        private String url;
        RequestThread(String url, Function callback) {
            this.url = url;
            this.callback = callback;
        }
        public void run() {
            try {

                HttpConnection con = (HttpConnection) javax.microedition.io.Connector.open(url);

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


                InputStream is = con.openInputStream();
                /*
                int c = is.read();
                sb = new StringBuffer();
                while(c >= 0) {
                    sb.append((char) c);
                    c = is.read();
                }
                Object args[] = {null, sb.toString()};
                */
                Object args[] = {null, is};
                callback.apply(args,0,1);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}

