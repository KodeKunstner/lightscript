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
    // TODO: globals object

    public static Object oldGlobalObject = null;
    private static Hashtable globals;

    /** Constructor, loading standard library */
    public LightScript() {
        globals = new Hashtable();
        StdLib.register(this);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f) throws ScriptException {
        Object args[] = {thisPtr};
        return f.apply(args, 0, 0);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f, Object arg1) throws ScriptException {
        Object args[] = {thisPtr, arg1};
        return f.apply(args, 0, 1);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f, Object arg1, Object arg2) throws ScriptException {
        Object args[] = {thisPtr, arg1, arg2};
        return f.apply(args, 0, 2);
    }

    /** Shorthands for executing a LightScript function */
    public static Object apply(Object thisPtr, Function f, Object arg1, Object arg2, Object arg3) throws ScriptException {
        Object args[] = {thisPtr, arg1, arg2, arg3};
        return f.apply(args, 0, 3);
    }

    /** Shorthand for evaluating a string that contains LightScript code */
    public Object eval(String s) throws ScriptException {
        return eval(new ByteArrayInputStream(s.getBytes()));
    }

    /** Parse and execute LightScript code read from an input stream */
    public Object eval(InputStream is) throws ScriptException {
        Object result = UNDEFINED, t = UNDEFINED;
        Compiler c = new Compiler(is, this);
        Function stmt = c.compileNextStatement();
        while(stmt != null) {
            result = apply(oldGlobalObject, stmt);
            stmt = c.compileNextStatement();
        }
        return result;
    }

    public Object[] getBox(Object key) {
        Object box[] = (Object[]) globals.get(key);
        if(box == null) {
            box = new Object[1];
            box[0] = UNDEFINED;
            globals.put(key, box);
        }
        return box;
    }
    /** Set a global value for this execution context */
    public void set(Object key, Object value) {
        getBox(key)[0] = value;
    }

    /** Retrieve a global value from this execution context */
    public Object get(Object key) {
        return getBox(key)[0];
    }
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
    // <editor-fold desc="options">
}
