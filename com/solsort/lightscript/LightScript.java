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
import java.util.Stack;

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
    Hashtable types = new Hashtable();
    Type defaultType = new Type();

    Object subscript(Object obj, Object key) {
        Object o = types.get(obj.getClass());
        if (o != null) {
            o = ((Type) o).get(obj, key);
        }
        if (o != null) {
            return o;
        }
        o = defaultType.get(obj, key);
        if (o != null) {
            return o;
        }
        return UNDEFINED;
    }

    void subscriptAssign(Object obj, Object key, Object val) {
        Object o = types.get(obj.getClass());
        Type thisClass;
        if (o == null) {
            thisClass = defaultType;
        } else {
            thisClass = (Type) o;
        }
        thisClass.set(obj, key, val);
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
    public Object[] getBox(Object key) {
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
        types.put((new String()).getClass(), new StringType());
        types.put((new Hashtable()), new HashtableType());
        types.put((new Stack()), new StackType());
        types.put((new Object[1]).getClass(), new ObjectArrayType());
        StdLib.register(this);
    }

    // <editor-fold desc="eval(...)">
    /** Parse and execute LightScript code read from an input stream */
    public Object eval(InputStream is) throws ScriptException {
        Object result = UNDEFINED, t = UNDEFINED;
        Compiler c = new Compiler(is, this);
        Function stmt = c.compileNextStatement();
        while (stmt != null) {
            result = apply(this, stmt);
            stmt = c.compileNextStatement();
        }
        return result;
    }

    /** Shorthand for evaluating a string that contains LightScript code */
    public Object eval(String s) throws ScriptException {
        return eval(new ByteArrayInputStream(s.getBytes()));
    }
    // </editor-fold>
    //<editor-fold desc="apply(...)">

    /** Shorthands for executing a LightScript function */
    public Object apply(Object thisPtr, Function f) throws ScriptException {
        Object args[] = {thisPtr};
        return f.apply(args, 0, 0);
    }

    /** Shorthands for executing a LightScript function */
    public Object apply(Object thisPtr, Function f, Object arg1) throws ScriptException {
        Object args[] = {thisPtr, arg1};
        return f.apply(args, 0, 1);
    }

    /** Shorthands for executing a LightScript function */
    public Object apply(Object thisPtr, Function f, Object arg1, Object arg2) throws ScriptException {
        Object args[] = {thisPtr, arg1, arg2};
        return f.apply(args, 0, 2);
    }

    /** Shorthands for executing a LightScript function */
    public Object apply(Object thisPtr, Function f, Object arg1, Object arg2, Object arg3) throws ScriptException {
        Object args[] = {thisPtr, arg1, arg2, arg3};
        return f.apply(args, 0, 3);
    }
    // </editor-fold>
}
