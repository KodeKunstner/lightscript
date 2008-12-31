
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
public class Main implements Yoco {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        Yolan.addFunction("println", new Main());
        Yolan.eval(new FileInputStream(new File("/home/voel/Desktop/speciale/code/Yolan/src/test.yl")));
    }

    public Object apply(Yolan[] args) {
        System.out.println(args[0].value().toString());
        return args[0];
    }

}
