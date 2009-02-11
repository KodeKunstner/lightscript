import java.io.InputStream;
import java.io.IOException;

class Parser {
    public static Object parse(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        Tokeniser tokeniser = new Tokeniser(is);
        while(tokeniser.next()) {
            sb.append(tokeniser.getToken().toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    static class Tokeniser {

        InputStream is;
        int c;
        StringBuffer sb;
        Object token;

        public Object getToken() {
            return token;
        }
        
        Tokeniser(InputStream is) {
            this.is = is;
            sb = new StringBuffer();
            c = ' ';
            token = null;
        }

        private void nextc() throws IOException {
            c = is.read();
        }
        private void pushc() throws IOException {
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

        public boolean next() throws IOException {
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
                        token = "/";
                        return true;
                    }
                } 
                nextc();
            }

            // End of file
            if(c == -1) {
                token = null;
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
                token = new Literal(sb.toString());
                return true;

            // Number
            } else if(isNum()) {
                do {
                    pushc();
                } while(isNum());
                token = new Literal(Integer.valueOf(sb.toString()));
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
            token = sb.toString();
            return true;
        }
        
    }
    static class Literal {
        public Object val;
        public Literal(Object val) {
            this.val = val;
        }
    }
}
