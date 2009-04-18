class LightScriptNum implements LightScriptThing {
    public int val;
    public LightScriptNum(int val) {
        this.val = val;
    }
    public LightScriptNum(String val) {
        this.val = Integer.parseInt(val);
    }
    public int getType() {
        return 1;
    }
    public LightScriptThing apply(LightScriptThing[] stack, int pos, int argc) {
        return LightScript.UNDEFINED;
    }
    public LightScriptThing get(LightScriptThing index) {
        return LightScript.UNDEFINED;
    }
    public void put(LightScriptThing index, LightScriptThing value) {
    }
    public boolean toBool() {
        return val != 0;
    }
    public int toInt() {
        return val;
    }
    public String toString() {
        return Integer.toString(val);
    }
}
