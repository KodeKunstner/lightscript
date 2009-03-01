
import java.io.File;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;

/**
 *
 * @author voel
 */
public class Main implements Function {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Yolan.addFunction("println", new Main(0));
        Yolan.addFunction("fn-test", new Main(1));
        Yolan.addFunction("string-test", new Main(2));
        Yolan.eval(new FileInputStream(new File(args[0])));
    }

    public Main(int i) {
        fn = i;
    }
    int fn;

    public Object apply(Yolan[] args) {
        switch (fn) {
            case 0: {
                Object o = args[0].value();
                System.out.println(o == null ? "nil" : o.toString());
                return args[0];
            }
            case 1: {
                Yolan function = ((Yolan) args[0].value());
                System.out.println(function.nargs());
                return function.apply(new Integer(32), new Integer(42));
            }
            case 2: {
                System.out.println(args[0].string());
                args[0].value();
                System.out.println(args[0].string());
                return null;
            }
        }
        return null;
    }
}
