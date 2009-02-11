import java.io.InputStream;
import java.io.IOException;
import java.util.Stack;

class Parser {
    ///////////////////////////////////////////////
    // Interface function for parsing
    ////
    public static Object parse(InputStream is) {
        Parser parser= new Parser(is);
        parser.next();
        return stringify(parser.parse(0));
    }

    public static String stringify(Object o) {
        if(o instanceof Object[]) {
            Object[] os = (Object[])o;
            StringBuffer sb = new StringBuffer();
            sb.append("[");

            for(int i = 0; i < os.length; i++) {
                sb.append(" ");
                sb.append(stringify(os[i]));
            }
            sb.append(" ]");
            return sb.toString();
        } else {
            return o.toString();
        }
    }

    /////////////////////////////////////////////
    // The core parse function
    ////////
    private Object[] parse(int rbp) {
        Token t = token;
        next();
        Object[] left = t.nud();
        while(rbp < token.bp && !t.sep) {
            t = token;
            next();
            left = t.led(left);
        }
        return left;
    }

    //////////////////////////////////////////
    // Utility functions
    ////
    private static Object [] v(Object o1) {
        Object[] result = {o1};
        return result;
    }

    private static Object [] v(Object o1, Object o2) {
        Object[] result = {o1, o2};
        return result;
    }

    private static Object [] v(Object o1, Object o2, Object o3) {
        Object[] result = {o1, o2, o3};
        return result;
    }

    //////////////////////////////////////////
    // Tokeniser
    ////
    
    InputStream is;
    int c;
    StringBuffer sb;
    Token token;

    private Object getToken() {
        return token;
    }
    
    private Parser(InputStream is) {
        this.is = is;
        sb = new StringBuffer();
        c = ' ';
        token = null;
    }

    private void nextc() {
        try {
            c = is.read();
        } catch(Exception e) {
            c = -1;
        }
    }
    private void pushc() {
        sb.append((char)c);
        nextc();
    }
    private boolean isNum() {
        return '0' <= c && c <= '9';
    }

    private boolean isAlphaNum() {
        return isNum() || c == '_' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    private boolean isSymb() {
        return c == '=' || c == '!' || c == '<' || c == '&' || c == '|';
    }

    private boolean next() {
        sb.setLength(0);

        // skip whitespaces
        while(c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '/') {
            // comments
            if(c == '/') {
                nextc();
                if(c == '/') {
                    while(c != '\n' && c != -1) {
                        nextc();
                    }
                } else {
                    token = new Token(false, "/");
                    return true;
                }
            } 
            nextc();
        }

        // End of file
        if(c == -1) {
            token = new Token(false, null);
            return false;

        // String
        } else if(c == '"') {
            nextc();
            while(c != -1 && c != '"') {
                if(c == '\\') {
                    nextc();
                    if(c == 'n') {
                        c = '\n';
                    }
                }
                pushc();
            }
            nextc();
            token = new Token(true, sb.toString());
            return true;

        // Number
        } else if(isNum()) {
            do {
                pushc();
            } while(isNum());
            token = new Token(true, Integer.valueOf(sb.toString()));
            return true;

        // Identifier
        } else if(isAlphaNum()) {
            do {
                pushc();
            } while(isAlphaNum());

        // Long symbol !== , ===, <= , &&, ...
        } else if(isSymb()) {
            do {
                pushc();
            } while(isSymb());

        // Single symbol
        } else {
            pushc();
        }
        token = new Token(false, sb.toString());
        return true;
    }
    
    ////////////////////////////////////////////////////////
    // Token class
    // nuds/leds
    ////
    
    private static final Object[] END_TOKEN = { "(end)" };
    private static final Object[] SEP_TOKEN = { "(sep)" };
    private static final String ID_APPLY= "apply";
    private static final Boolean TRUE = new Boolean(true);


    class Token {
        Object val;
        private byte nudId;
        private byte ledId;
        public boolean sep;
        public int bp;

        private static final int
            NUD_ID = 0,
            NUD_LITERAL= 1,
            NUD_END = 2,
            NUD_SEP = 3,
            NUD_LIST = 4,
            NUD_PREFIX = 5,
            NUD_PREFIX2 = 6,
            LED_INFIX = 7,
            LED_INFIXR = 8,
            LED_INFIX_LIST = 9;

        private Object[] readList(Stack s) {
            Object[] p = parse(0);
            while(p != END_TOKEN) {
                if(p != SEP_TOKEN) {
                    s.push(p);
                }
                p = parse(0);
            }

            Object[] result = new Object[s.size()];
            s.copyInto(result);
            return result;
        }
        public Object[] nud() {
            switch(nudId) {
                case NUD_ID:
                    return v("identifier", val);
                case NUD_LITERAL:
                    return v("literal", val);
                case NUD_END:
                    return END_TOKEN;
                case NUD_SEP:
                    return SEP_TOKEN;
                case NUD_LIST:
                    {
                      Stack s = new Stack();
                        s.push(val);
                        return readList(s);
                    }
                case NUD_PREFIX:
                    return v(val, parse(0));
                case NUD_PREFIX2:
                    return v(val, parse(0), parse(0));
                default:
                    throw new Error("Unknown nud: " + nudId);
            }
        }
        public Object[] led(Object left) {
            switch(ledId) {
                case LED_INFIX: 
                    return v(val, left, parse(bp));
                case LED_INFIXR: 
                    return v(val, left, parse(bp-1));
                case LED_INFIX_LIST:
                    {
                        Stack s = new Stack();
                        s.push(ID_APPLY);
                        s.push(val);
                        s.push(left);
                        return readList(s);
                    }
                default:
                    throw new Error("Unknown led: " + ledId);
            }
        }

        public Token(boolean isLiteral, Object val) {
            this.val = val;
            sep = false;
            bp = 0;
            nudId = 0;
            ledId = 0;
            if(isLiteral) {
                nudId = NUD_LITERAL;

                return;
            } 
            
            if( ".".equals(val)) {
                bp = 700;
                ledId = LED_INFIX;

            } 
            
            if( "(".equals(val)
             || "[".equals(val)) {
                bp = 600;
                ledId = LED_INFIX_LIST;
            } 
            
            if( "*".equals(val)
             || "%".equals(val)) {
                bp = 500;
                ledId = LED_INFIX;
            } 
            
            if( "+".equals(val)
             || "-".equals(val)) {
                bp = 400;
                ledId = LED_INFIX;

            } 
            
            if( "===".equals(val)
             || "!==".equals(val)
             || "<=".equals(val)
             || "<".equals(val)) {
                bp = 300;
                ledId = LED_INFIX;
            } 
            
            if( "&&".equals(val)
             || "||".equals(val)
             || "else".equals(val)) {
                bp = 200;
                ledId = LED_INFIXR;
            } 
            
            if( "=".equals(val)) {
                bp = 100;
                ledId = LED_INFIX;
            } 
            
            if( val == null 
             || "]".equals(val)
             || ")".equals(val)
             || "}".equals(val) ) {
                nudId = NUD_END;
                sep = true;
            } 
            
            if( ":".equals(val)
             || ";".equals(val)
             || ",".equals(val) ) {
                nudId = NUD_SEP;
                sep = true;
            } 
            
            if( "(".equals(val)
             || "{".equals(val)
             || "[".equals(val) ) {
                nudId = NUD_LIST;
            } 
            
            if( "var".equals(val)
             || "return".equals(val)
             || "-".equals(val)
             || "!".equals(val) ) {
                nudId = NUD_PREFIX;
            } 
            
            if( "function".equals(val)
             || "if".equals(val)
             || "while".equals(val) ) {
                nudId = NUD_PREFIX2;
            }

            if( "undefined".equals(val) 
             || "null".equals(val)
             || "false".equals(val)) {
                val = null;
                nudId = NUD_LITERAL;
            }
            if( "true".equals(val) ) {
                val = TRUE;
                nudId = NUD_LITERAL;
            }

        }
        public String toString() {
            return val.toString();
        }

    }

}
