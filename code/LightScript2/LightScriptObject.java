import java.util.Hashtable; 

public class LightScriptObject extends Hashtable implements LightScriptThing {
    private Hashtable parent;

    public LightScriptObject() {
        this.parent = null;
    }

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

    public int getType() {
        return 8;
    }
    public LightScriptThing apply(LightScriptThing[] stack, int pos, int argc) {
        return LightScript.UNDEFINED;
    }
    public LightScriptThing get(LightScriptThing index) {
        return (LightScriptThing) this.get((Object) index);
    }
    public void put(LightScriptThing index, LightScriptThing value) {
        this.put((Object) index, (Object) value);
    }
    public boolean toBool() {
        return true;
    }
    public int toInt() {
        return super.size();
    }
}
