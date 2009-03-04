//This class provides basic abillity to run scripts in order to test
//fscriptME.  Typically used to run regtest.script and any other 
//test scripts duting development
import murlen.util.fscriptME.*;
import java.io.*;
import java.util.Vector;

//As fscriptME doesn't have a BasicIO class like fscript we have to make
//or owm (just lifted from FScript)
class BasicIO extends FScript {
    
    private Object files[];
    
    public BasicIO() {
        super();
        files=new Object[25];
    }
    
    
    public Object callFunction(String name,Vector param) throws FSException {
        
        
        if (name.equals("println")) {
            int n;
            String s="";
            for(n=0;n<param.size();n++) {
                s=s+ param.get(n);
            }
            System.out.println(s);
        }
        
        else if (name.equals("readln")) {
            try {
                return new BufferedReader(
                new InputStreamReader(System.in)).readLine();
                
            } catch (IOException e)  {
                throw new FSException(e.getMessage());
            }
        }
        
        else if (name.equals("open")) {
            int n;
            
            try {
                for(n=0;n<25;n++) {
                    if (files[n]==null) {
                        if (((String)param.get(1)).equals("r")) {
                            files[n]=new BufferedReader(
                            new FileReader((String)param.get(0)));
                            break;
                        } else if (((String)param.get(1)).equals("w"))  {
                            files[n]=new BufferedWriter(
                            new FileWriter((String)param.get(0)));
                            break;
                        } else {
                            throw new FSException(
                            "open expects 'r' or 'w' for modes");
                        }
                    }
                }
            } catch (IOException e)  {
                throw new FSException(e.getMessage());
            }
            if (n<25) return new Integer(n);
            else return new Integer(-1);
        }
        
        else if (name.equals("close")) {
            int n;
            n=((Integer)param.get(0)).intValue();
            if (files[n]==null) {
                throw new FSException("Invalid file number passed to close");
            }
            try {
                if (files[n] instanceof BufferedWriter) {
                    ((BufferedWriter)files[n]).close();
                } else {
                    ((BufferedReader)files[n]).close();
                }
                files[n]=null;
            } catch (IOException e) {
                throw new FSException(e.getMessage());
            }
        }
        
        else if (name.equals("write")) {
            int n;
            String s="";
            for(n=1;n<param.size();n++) {
                s=s+ param.get(n);
            }
            n=((Integer)param.get(0)).intValue();
            if (files[n]==null) {
                throw new FSException("Invalid file number passed to write");
            }
            if (!(files[n] instanceof BufferedWriter)) {
                throw new FSException("Invalid file mode for write");
            }
            try {
                ((BufferedWriter)files[n]).write(s,0,s.length());
                ((BufferedWriter)files[n]).newLine();
            } catch (IOException e) {
                throw new FSException(e.getMessage());
            }
        }
     
        else if (name.equals("read")) {
            int n;
            String s;
            n=((Integer)param.get(0)).intValue();
            if (files[n]==null) {
                throw new FSException("Invalid file number passed to read");
            }
            if (!(files[n] instanceof BufferedReader)) {
                throw new FSException("Invalid file mode for read");
            }
            try {
                s=((BufferedReader)files[n]).readLine();
                if (s==null) s="";
                return s;
            } catch (IOException e) {
                throw new FSException(e.getMessage());
            }
        }
        
        else if (name.equals("eof")) {
            int n;
            n=((Integer)param.get(0)).intValue();
            if (files[n]==null) {
                throw new FSException("Invalid file number passed to eof");
            }
            BufferedReader br=(BufferedReader)files[n];
            try {
                br.mark(1024);
                if (br.readLine()==null) {
                    return new Integer(1);
                } else {
                    br.reset();
                    return new Integer(0);
                }
            } catch (IOException e) {
                throw new FSException(e.getMessage());
            }
	} else if (name.equals("abort")) {
		exit(param.get(0));
        } else {
            super.callFunction(name,param);
        }
        return new Integer(0);
    }
}

public class FSTest{
    
    public static final void main(String args[]) throws FSException,IOException{
        
        BasicIO fs=new BasicIO();
        String s;
        
        //also FscriptME has no load method, so we have to load individual lines
        BufferedReader f=new BufferedReader(new FileReader(args[0]));
        s=f.readLine();
        
        while(s!=null) {
            fs.addLines(s);
            s=f.readLine();
        }
        
        f.close();
               
        Object o=fs.runCode();
	
	System.out.println("Code returned: "+o);
    }
}

