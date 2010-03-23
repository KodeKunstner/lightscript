package testsuite;
import com.solsort.lightscript.*;
import java.io.*;

class LightScriptTest implements Function {
    public Object apply(Object[] args, int argpos, int argc) throws LightScriptException {
        str = (String) args[argpos+1];
        Object result = ls.eval(str);
        ++testno;
        if (result.equals(args[argpos+2])) {
            System.out.print(testno % 10);
        } else {
            ++errorcount;
            System.out.println(  "\nError evaluating: " +  str);
            System.out.println("Expected: "+args[argpos+2].toString());
            System.out.println("Got: "+result.toString());
        }
        return null;
    }
    static LightScript ls;
    static int testno;
    static String str;
    static int errorcount = 0;
    static int totalcount = 0;
    public static void main(String args[]) throws FileNotFoundException {
        ls = new LightScript();
        ls.set("test", new LightScriptTest());
        for (int i = 0; i < args.length; ++i) {
            System.out.print("\nTesting " + args[i] + ": ");
            try {
                testno = 0;
                ls.eval(new FileInputStream(args[i]));
            } catch (Throwable e) {
                ++errorcount;
                if (e instanceof LightScriptException) {
                    System.out.println("Error: got LightScriptException during evaluation: " + ((LightScriptException)e).value.toString());
                }
                System.out.println("Exception with code: " + str);
                e.printStackTrace();
            }
            totalcount += testno;
        }
        System.out.println("\n" + (totalcount-errorcount) + "/" + totalcount + " tests ok.");
    }
}
