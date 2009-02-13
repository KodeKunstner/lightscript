import java.util.Hashtable; 

class LightScriptObject extends Hashtable {
    public Hashtable parent;

    public LightScriptObject(Hashtable parent) {
        this.parent = parent;
    }

    public void set(Object key, Object value) {
        if(value == null) {
            super.remove(key);
        } else {
            super.put(key, value);
        }
    }

    public Object get(Object key) {
        Object result = super.get(key);
        if(result == null && parent != null) {
            result = parent instanceof LightScriptObject 
                ? ((LightScriptObject)parent).get(key)
                : parent.get(key);
        }
        return result;
    }
}
