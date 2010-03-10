package com.solsort.lightscript;

class StackType extends Type {

    public Object get(Object thisPtr, Object key) {
        java.util.Stack s = (java.util.Stack) thisPtr;
        int len = s.size();
        if (key instanceof Integer) {
            int pos = ((Integer) key).intValue();
            if (0 <= pos && pos < len) {
                return s.elementAt(pos);
            }
        }
        if (key.equals("length")) {
            return new Integer(len);
        }
        return super.get(thisPtr, key);
    }

    public void set(Object thisPtr, Object key, Object val) {
        java.util.Stack s = (java.util.Stack) thisPtr;
        int len = s.size();
        if (key instanceof Integer) {
            int pos = ((Integer) key).intValue();
            if (0 <= pos) {
                if (pos >= len) {
                    s.setSize(pos + 1);
                }
                s.setElementAt(val, pos);
            }
        }
    }

    public boolean toBool(Object thisPtr) {
        return !((java.util.Stack) thisPtr).empty();
    }
}
