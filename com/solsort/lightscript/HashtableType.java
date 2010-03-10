package com.solsort.lightscript;

class HashtableType extends Type {

    public Object get(Object thisPtr, Object key) {
        Object result = ((java.util.Hashtable) thisPtr).get(key);
        if (result != null) {
            return result;
        }
        return super.get(thisPtr, key);
    }

    public void set(Object thisPtr, Object key, Object val) {
        java.util.Hashtable h = (java.util.Hashtable) thisPtr;
        if (val == LightScript.UNDEFINED || val == null) {
            h.remove(key);
        } else {
            h.put(key, val);
        }
    }
}
