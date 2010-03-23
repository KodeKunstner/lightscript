package com.solsort.lightscript;

import java.util.Hashtable;

class Type implements Function {

    java.util.Hashtable methods;
    Function setter;
    Function getter;
    LightScript ls;

    Type(LightScript ls) {
        setter = this;
        getter = this;
        methods = new Hashtable();
        methods.put("__getter__", this);
        this.ls = ls;
    }

    public Object apply(Object[] args, int argpos, int argcount) throws LightScriptException {
        Object o = methods.get(args[argpos + 1]);
        if (o == null) {
            o = ls.defaultType.methods.get(args[argpos + 1]);
            if (o == null) {
                o = LightScript.UNDEFINED;
            }
        }
        return o;
    }
}
