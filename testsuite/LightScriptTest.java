package testsuite;
import com.solsort.lightscript.*;
import java.io.*;

class LightScriptTest implements Function {
    public Object apply(Object[] args, int argpos, int argc) throws ScriptException {
        String str = (String) args[argpos+1];
        Object result = ls.eval(str);
        ++testno;
        if(result.equals(args[argpos+2])) {
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
    static int errorcount = 0;
    static int totalcount = 0;
    public static void main(String args[]) throws FileNotFoundException{
        ls = new LightScript();
        ls.set("test", new LightScriptTest());
        for(int i = 0; i < args.length; ++i) {
            System.out.print("\nTesting " + args[i] + ": ");
            try {
                testno = 0;
                ls.eval(new FileInputStream(args[i]));
            } catch(ScriptException e) {
                ++errorcount;
                System.out.println("Error: got ScriptException during evaluation: " + e.value.toString());
                e.printStackTrace();
            }
            totalcount += testno;
        }
        System.out.println("\n" + (totalcount-errorcount) + "/" + totalcount + " tests ok.");
    }
}
