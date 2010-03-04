public interface LightScriptObject {
    /**
     * Set a property on the object.
     */
    public void set(Object key, Object value);

    /**
     * Get a property from the object.
     */
    public Object get(Object key);
}
