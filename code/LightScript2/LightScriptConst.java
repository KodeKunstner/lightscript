public class LightScriptConst implements LightScriptThing {
    // 0: true
    // 1: false
    // 2: undefined
    // 3: null
    private int kind;
    public LightScriptConst(int kind) {
        this.kind = kind;
    }
    public int getType() {
        return 8 << kind;
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
        return kind == 0;
    }
    public int toInt() {
        return kind == 0 ? 1 : 0;
    }
    public String toString() {
        return kind < 2
                ? kind  < 1
                    ? "true"
                    : "false"
                : kind < 3
                    ? "undefined"
                    : "null";
    }
}
