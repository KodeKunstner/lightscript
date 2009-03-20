public final class LightScriptException extends Exception {
    public Object value;
    public LightScriptException(Object value) {
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
