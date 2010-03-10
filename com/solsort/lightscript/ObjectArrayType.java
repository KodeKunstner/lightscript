package com.solsort.lightscript;

class ObjectArrayType extends Type {

    public Object get(Object thisPtr, Object key) {
        Object[] os = (Object[]) thisPtr;
        int len = os.length;
        if (key instanceof Integer) {
            int pos = ((Integer) key).intValue();
            if (0 <= pos && pos < len) {
                return os[pos];
            }
        }
        if (key.equals("length")) {
            return new Integer(len);
        }
        return super.get(thisPtr, key);
    }

    public void set(Object thisPtr, Object key, Object val) {
        Object[] os = (Object[]) thisPtr;
        int len = os.length;
        if (key instanceof Integer) {
            int pos = ((Integer) key).intValue();
            if (0 <= pos && pos < len) {
                os[pos] = val;
            }
        }
    }

    public boolean toBool(Object thisPtr) {
        return ((Object[]) thisPtr).length != 0;
    }
}
