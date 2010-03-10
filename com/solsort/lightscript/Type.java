package com.solsort.lightscript;

/**
 *
 * @author rje
 */
public class Type {

    public java.util.Hashtable methods;

    public Object get(Object thisPtr, Object key) {
        Object result = methods.get(key);
        return result;
    }

    public void set(Object thisPtr, Object key, Object val) {
    }
}




