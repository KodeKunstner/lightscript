package com.solsort.lightscript;

public interface Function {

    /**
     * Method callable from LightScript.
     *
     * @param args      An array that contains the parameters,
     *                  this is READ ONLY.
     *                  This is args[argpos].
     *                  The first parameter is at args[argpos + 1], and
     *                  the last parameter is at args[argpos + argcount].
     *
     * @param argpos    The position of the first parameter.
     *
     * @param argcount  The number of parameters.
     *
     * @return          An object that is returned to LightScript
     */
    public Object apply(Object[] args, int argpos, int argcount)
    throws LightScriptException;
}
