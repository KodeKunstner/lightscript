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
    // TODO: globals object

    public static Object oldGlobalObject = null;
    private static Hashtable globals = new Hashtable();


    /*`\subsection{Public functions}'*/
    // <editor-fold desc="public api">
    /** Get the default-setter function */
    public Function defaultSetter() {
        return (Function) executionContext[EC_SETTER];
    }

    /** Set the default-setter function.
     * The default setter function is called as a method on the object,
     * with the key and the value as arguments. */
    public void defaultSetter(Function f) {
        executionContext[EC_SETTER] = f;
    }

    /** Get the default-getter function */
    public Function defaultGetter() {
        return (Function) executionContext[EC_GETTER];
    }

    /** Set the default-getter function.
     * The default getter function is called as a method on the object,
     * with a single argument, which is the key */
    public void defaultGetter(Function f) {
        executionContext[EC_GETTER] = f;
    }
    /**
     * context for execution
     */
    public Object[] executionContext;

    /** Constructor, loading standard library */
    public LightScript() {
        executionContext = new Object[9];
        executionContext[EC_GLOBALS] = new Hashtable();
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
    /*`\section{Definitions, API, and utility functions}'*/

    /* If debugging is enabled, more tests are run during run-time,
     * and errors may be caught in a more readable way.
     * It also adds support for more readable printing of
     * id, etc.
     */
    private static final boolean DEBUG_ENABLED = true;
    private static final boolean __PRINT_EXECUTED_INSTRUCTIONS__ = false;
    private static final boolean __DO_YIELD__ = true;

    /* If enabled, wipe the stack on function exit,
     * to kill dangling pointers on execution stack,
     * for better GC at performance price
     */
    private static final boolean __CLEAR_STACK__ = true;
    // </editor-fold>
    // <editor-fold desc="defines">

    /** Sizes of different kinds of stack frames */
    public static final int RET_FRAME_SIZE = 4;
    public static final int TRY_FRAME_SIZE = 5;
    /** Token used for separators (;,:), which are just discarded */
    private static final Object[] SEP_TOKEN = {new Integer(OpCodes.SEP)};
    /** The globals variables in this execution context.
     * they are boxed, in such that they can be passed
     * to the closure of af function, which will then
     * be able to modify it without looking it up here */
    private static final int EC_GLOBALS = 0;
    private static final int EC_OBJECT_PROTOTYPE = 1;
    private static final int EC_ARRAY_PROTOTYPE = 2;
    private static final int EC_FUNCTION_PROTOTYPE = 3;
    private static final int EC_STRING_PROTOTYPE = 4;
    /* Index in executionContext for the default setter function,
     * which is called when a property is set on
     * an object which is neither a Stack, Hashtable nor LightScriptObject
     *
     * The apply method of the setter gets the container as thisPtr,
     * and takes the key and value as arguments
     */
    private static final int EC_SETTER = 5;
    /* Index in executionContext for the default getter function,
     * called when subscripting an object
     * which is not a Stack, Hashtable, String nor LightScriptObject
     * or when the subscripting of any of those objects returns null.
     * (non-integer on stacks/strings, keys not found in Hashtable or
     * its prototypes, when LightScriptObject.get returns null)
     *
     * The apply method of the getter gets the container as thisPtr,
     * and takes the key as argument
     */
    private static final int EC_GETTER = 6;
    private static final int EC_NEW_ITER = 8;
    //</editor-fold>
    /*`\subsection{Debugging}'*/
    //<editor-fold>
    /** Mapping from ID to name of ID */

}
