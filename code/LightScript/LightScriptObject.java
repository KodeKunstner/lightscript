public interface LightScriptObject {
    /** 
     * Method callable from LightScript.
     *
     * @param thisPtr   The this LightScript object, 
     *                  if apply was called as a method.
     *
     * @param args      An array that contains the parameters,
     *                  this is READ ONLY.
     *                  The first parameter is at args[argpos], and 
     *                  the last parameter is at args[argpos + argcount - 1].
     *
     * @param argpos    The position of the first parameter.
     *
     * @param argcount  The number of parameters.
     *
     * @return          An object that is returned to LightScript
     */
    public Object apply(Object thisPtr, Object[] args, int argpos, 
                        int argcount) throws LightScriptException;
    /**
     * Set a property on the object.
     */
    public void set(Object key, Object value);

    /**
     * Get a property from the object.
     */
    public Object get(Object key);
}
