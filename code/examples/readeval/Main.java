class Main {
    public static void main(String [] args) throws java.io.IOException {
        Yolan yl = Yolan.readExpression(System.in);
        while(yl != null) {
            try {
                System.out.println("Result: " + yl.value().toString());
            } catch(Throwable yolanError) {
                System.out.println("Error: " + yolanError.toString());
            }
            yl = Yolan.readExpression(System.in);
        }
    }
}   
