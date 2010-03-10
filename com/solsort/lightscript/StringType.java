package com.solsort.lightscript;

class StringType extends Type {

    public Object get(Object thisPtr, Object key) {
        java.lang.String str = (java.lang.String) thisPtr;
        int len = str.length();
        if (key instanceof Integer) {
            int pos = ((Integer) key).intValue();
            if (0 <= pos && pos < len) {
                return new Integer(str.charAt(pos));
            }
        }
        if (key.equals("length")) {
            return new Integer(len);
        }
        return super.get(thisPtr, key);
    }

    public boolean toBool(Object thisPtr) {
        return ((java.lang.String) thisPtr).length() != 0;
    }
}
