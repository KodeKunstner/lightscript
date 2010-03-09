package com.solsort.mobile;

public final class ScriptException extends Exception {

    public Object value;

    public ScriptException(Object value) {
        super(value.toString());
        this.value = value;
    }
    /*
    public void printStackTrace() {
    if(value instanceof Throwable) {
    ((Throwable)value).printStackTrace();
    }
    super.printStackTrace();
    }
     */
}
