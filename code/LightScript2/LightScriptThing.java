public interface LightScriptThing {
    public int getType();
    public LightScriptThing apply(LightScriptThing[] stack, int pos, int argc);
    public LightScriptThing get(LightScriptThing index);
    public void put(LightScriptThing index, LightScriptThing value);
    public boolean toBool();
    public int toInt();
}
