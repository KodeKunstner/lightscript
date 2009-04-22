import java.util.Hashtable; 

public final class LightScriptObject extends Hashtable {
    private Hashtable parent;

    public LightScriptObject(Hashtable parent) {
        this.parent = parent;
    }

    public LightScriptObject() {
    }

    public void set(Object key, Object value) {
        if(value == null) {
            super.remove(key);
        } else {
            super.put(key, value);
        }
    }

    public Object get(Object key) {
        if("prototype".equals(key)) {
            return parent;
        }
        Object result = super.get(key);
        if(result == null && parent != null) {
            result = parent instanceof LightScriptObject 
                ? ((LightScriptObject)parent).get(key)
                : parent.get(key);
        }
        return result;
    }
}
