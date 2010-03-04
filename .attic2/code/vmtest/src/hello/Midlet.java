/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hello;

import java.util.Stack;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Timer;


/**
 * @author voel
 */
public class Midlet extends MIDlet {
    private static final int iterations = 1000;
    private static long push1() {
        Stack s = new Stack();
        s.ensureCapacity(1000);
        long l = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++) {
            s.setSize(0);
            Object o;
            for(int j = 0; j < 1000; j++) {
                s.push(null);
            }
            for(int j = 0; j < 1000; j++) {
                o = s.pop();
            }
        } 
        return System.currentTimeMillis() - l;
    }
    private static long push2() {
        Object[] s = new Object[1000];
        int pos = 0;
        long l = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++) {
            pos = 0;
            Object o;
            for(int j = 0; j < 1000; j++) {
                s[pos] = null;
                pos++;
            }
            for(int j = 0; j < 1000; j++) {
                pos--;
                o = s[pos];
            }
        }    
        return System.currentTimeMillis() - l;
        
        
    }
    public void startApp() {
        for(int i = 0; i < 100; i++) {
            System.out.println("push2:" + push2());
            System.out.println("push1:" + push1());
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
