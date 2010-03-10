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

    public boolean toBool(Object thisPtr) {
        return true;
    }
}

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
