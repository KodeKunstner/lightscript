class Main {
	public static void main(String [] args) throws Exception {
		Yolan yl;
		while((yl = Yolan.readExpression(System.in)) != null) {
			try {
				System.out.println(" -> " + yl.value().toString());
			} catch(Throwable yolanError) {
				System.out.println(" Error: " + yolanError.toString());
			}
		}
	}
}
