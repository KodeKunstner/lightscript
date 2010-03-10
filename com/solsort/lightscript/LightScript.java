//<editor-fold desc="license and imports">
package com.solsort.lightscript;

/**
This software is released under the 
AFFERO GENERAL PUBLIC LICENSE version 3

(the actual license text can be retrieved
from the Free Software Foundation:
http://www.gnu.org/licenses/agpl-3.0.html)

Copyright, 2009-2010, Rasmus Jensen, rasmus@solsort.com

Contact for other licensing options.
 */
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Hashtable;

/*`\subsection{Variables}'*/
/** Instances of the LightScript object, is an execution context,
 * where code can be parsed, compiled, and executed.
 *
 * @author Rasmus Jensen, rasmus@lightscript.net
 * @version 1.2
 */
// </editor-fold>
public final class LightScript {
    // <editor-fold desc="TRUE NULL FALSE UNDEFINED">

    /** The true truth value of results
     * of tests/comparisons within LightScript */
    public static final Object TRUE = new StringBuffer("true");
    /** The null value within LightScript */
    public static final Object NULL = new StringBuffer("null");
    /** The undefined value within LightScript */
    public static final Object UNDEFINED = new StringBuffer("undefined");
    /** The false truth value of results
     * of tests/comparisons within LightScript */
    public static final Object FALSE = new StringBuffer("false");
    // </editor-fold>
    // <editor-fold desc="types">

    private class Type implements LightScriptFunction {

        java.util.Hashtable methods;
        LightScriptFunction setter;
        LightScriptFunction getter;

        Type() {
            setter = this;
            getter = this;
            methods = new Hashtable();
            methods.put("__getter__", this);
        }

        public Object apply(Object[] args, int argpos, int argcount) throws LightScriptException {
            Object o = methods.get(args[argpos + 1]);
            return o != null ? o : LightScript.UNDEFINED;
        }
    }
    private Hashtable types = new Hashtable();
    private Type defaultType = new Type();

    LightScriptFunction getGetter(Class c) {
        Object o = types.get(c);
        if (o != null) {
            o = ((Type) o).getter;
        }
        if (o == null) {
            o = defaultType.getter;
        }
        return (LightScriptFunction) o;
    }

    LightScriptFunction getSetter(Class c) {
        Object o = types.get(c);
        if (o != null) {
            o = ((Type) o).setter;
        }
        if (o == null) {
            o = defaultType.setter;
        }
        return (LightScriptFunction) o;
    }

    public LightScriptFunction getTypeMethod(Class c, String method) {
        Object o = null;
        if (c != null) {
            o = types.get(c);
        }
        if (o == null) {
            o = new Type();
            types.put(c, o);
        }
        o = ((Type) o).methods.get(method);

        if (o == null) {
            o = defaultType.methods.get(method);
        }
        return o == null ? null : (LightScriptFunction) o;
    }

    /**
     * Set a named method on a given class. Special methodnames are
     * "__setter__", "__getter__", "__iter__", "toBool", "-", "+", "*", "/", "!", "&lt;", "&lt;="
     * @param c the class on which to set the method. If c is null, the default class is used instead.
     * @param method the name of the method to set
     * @param function 
     */
    public void setTypeMethod(Class c, String method, LightScriptFunction function) {
        Type t;
        if (c == null) {
            t = defaultType;
        } else {
            Object o = types.get(c);
            if (o == null) {
                t = new Type();
                types.put(c, t);
            } else {
                t = (Type) o;
            }
        }
        if ("__setter__".equals(method)) {
            t.setter = function;
        } else if ("__getter__".equals(method)) {
            t.getter = function;
        }
        t.methods.put(method, function);
    }
    // </editor-fold>
    //<editor-fold desc="globals">
    /** Hashtable containing boxed global values */
    private Hashtable globals = new Hashtable();

    /** 
     * Retrieve a boxed global.
     * @param key the name of the global
     * @return an array of length one, containing the global value.
     */
    Object[] getBox(Object key) {
        Object box[] = (Object[]) globals.get(key);
        if (box == null) {
            box = new Object[1];
            box[0] = UNDEFINED;
            globals.put(key, box);
        }
        return box;
    }

    /** Set a global value */
    public void set(Object key, Object value) {
        getBox(key)[0] = value;
    }

    /** Retrieve a global value */
    public Object get(Object key) {
        return getBox(key)[0];
    }
    // </editor-fold>

    /** Constructor, loading standard library */
    public LightScript() {
        StdLib.register(this);
    }

    // <editor-fold desc="eval(...)">
    /** Parse and execute LightScript code read from an input stream */
    public Object eval(InputStream is) throws LightScriptException {
        Object result = UNDEFINED, t = UNDEFINED;
        Compiler c = new Compiler(is, this);
        LightScriptFunction stmt = c.compileNextStatement();
        while (stmt != null) {
            result = apply(this, stmt);
            stmt = c.compileNextStatement();
        }
        return result;
    }

    /** Shorthand for evaluating a string that contains LightScript code */
    public Object eval(String s) throws LightScriptException {
        return eval(new ByteArrayInputStream(s.getBytes()));
    }
    // </editor-fold>
    //<editor-fold desc="apply(...)">

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, LightScriptFunction f) throws LightScriptException {
        Object args[] = {thisPtr};
        return f.apply(args, 0, 0);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, LightScriptFunction f, Object arg1) throws LightScriptException {
        Object args[] = {thisPtr, arg1};
        return f.apply(args, 0, 1);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, LightScriptFunction f, Object arg1, Object arg2) throws LightScriptException {
        Object args[] = {thisPtr, arg1, arg2};
        return f.apply(args, 0, 2);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, LightScriptFunction f, Object arg1, Object arg2, Object arg3) throws LightScriptException {
        Object args[] = {thisPtr, arg1, arg2, arg3};
        return f.apply(args, 0, 3);
    }
    // </editor-fold>
}
