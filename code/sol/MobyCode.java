public final class MobyCode extends Code {
	public int argc; 
	public boolean is_void;

	MobyCode(Function[] code, int argc, boolean is_void) {
		super(code);
		this.argc = argc;
		this.is_void = is_void;
	}
}
