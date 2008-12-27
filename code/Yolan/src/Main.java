
import java.io.File;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author voel
 */
public class Main implements Yoco {

    public static void print(String s) {
        System.out.print(s);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        Yolan.addFunction("println", new Main());
        Yolan.eval(new FileInputStream(new File("/home/voel/Desktop/speciale/code/Yolan/src/test.yl")));
    }

    public Object apply(Object[] args) {
        System.out.println(args[0].toString());
        return args[0];
    }

}
