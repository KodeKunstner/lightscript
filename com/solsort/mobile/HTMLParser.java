package com.solsort.mobile;

import com.solsort.lightscript.Util;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;


/**
 * Parse a HTML document from an inputstream.
 * It will be mapped to an SXML-like structure,
 * so some ugly site like:
 * &lt;!DOCTYPE html-foo&gt;&lt;html&gt;&lt;body bgcolor=#123123 jhjhj text="#ffffff" xf&gt;Hello world&lt;/body&gt;&lt;/html&gt;
 *
 * will be transformed into something like:
 * ["html" [] ["body" ["bgcolor" "#123123" "text" "#ffffff"] "Hello world"]]
 * where [...] has type Object[], and "..." has type string.
 */
public class HTMLParser {

    boolean doTrim;
    /**
     * Parse a HTML document from an input stream
     * @param is the inputstream to parse
     * @return the html document as an SXML-like structure
     */
    static Object[] parse(InputStream is) {
        return (new HTMLParser(is, true)).doParse();
    }
//
// Variables
//
    // Input Stream
    private InputStream is;
    //
    // Input Stream
    private boolean eof = false;
    //
    // Current token
    private int tokentype;
    private String content;
    private Object[] params;
    //
    // Tokentype values
    private static final int STARTTAG = 0;
    private static final int ENDTAG = 1;
    private static final int SINGLETAG = 2;
    private static final int TEXT = 3;
    //
    // Internal variables used by the tokeniser
    private String buffer = "";
    private char c;
    private StringBuffer readbuffer;
    //
    // Internal variables used by parser
    private Stack currentTag = new Stack();
    private Stack tagStack = new Stack();
    //
    // Table for automatic closing of html tags
    private static String autoclose[] = {"meta", "link", "br", "hr", "img"};
    private static String closingTags[] = {"li", "dd", "dt", "td", "th", "td"};
    private static String closedTags[][];

    static {
        String ul[] = {"p", "li"};
        String dl[] = {"p", "dd", "dt"};
        String td[] = {"p", "td", "th"};
        String tr[] = {"p", "tr", "th", "td"};
        closedTags = new String[closingTags.length][];
        closedTags[0] = ul;
        closedTags[1] = dl;
        closedTags[2] = dl;
        closedTags[3] = td;
        closedTags[4] = td;
        closedTags[5] = tr;
    }
//
// Initialiseation
//

    private HTMLParser(InputStream is, boolean doTrim) {
        this.is = is;
        this.doTrim = doTrim;
    }

//
// Tokeniser
//
    private void ensureBuffer(int len) {
        while (buffer.length() < len) {
            int ch;
            try {
                ch = is.read();
            } catch (IOException ex) {
                ch = -1;
            }
            buffer += (char) ch;
        }
    }

    private void skip(int n) {
        ensureBuffer(n + 1);
        buffer = buffer.substring(n, buffer.length());

        c = buffer.charAt(0);
        if (c == (char) -1) {
            eof = true;
        }
    }

    private boolean startsWith(String s) {
        ensureBuffer(s.length());
        return buffer.startsWith(s);
    }

    private void skipWhiteSpace() {
        while (c == ' ' || c == '\n' || c == '\t') {
            skip(1);
        }
    }

    private void appendc() {
        readbuffer.append(c);
        skip(1);
    }

    private String readId() {
        readbuffer = new StringBuffer();
        skipWhiteSpace();
        while ((('a' <= c && c <= 'z')
                || ('A' <= c && c <= 'Z')
                || ('0' <= c && c <= '9')
                || c == '"' || c == '#')) {
            if (c == '"') {
                skip(1);
                while (!eof && c != '"') {
                    appendc();
                }
                skip(1);
            } else {
                appendc();
            }
        }
        skipWhiteSpace();
        return readbuffer.toString();
    }

    private void nextToken() {
        content = "";
        while (!eof && content.equals("")) {
            if (startsWith("<")) {
                skip(1);
                if (startsWith("/")) {
                    tokentype = ENDTAG;
                    skip(1);
                } else {
                    tokentype = STARTTAG;
                }
                content = readId().toLowerCase();

                Stack paramsStack = new Stack();
                while (!eof && c != '>') {

                    String id = readId();
                    if (c == '=') {
                        skip(1);
                        paramsStack.push(id.toLowerCase());
                        paramsStack.push(readId());
                    } else if (startsWith("!--")) {
                        skip(3);
                        while (!eof && !startsWith("--")) {
                            skip(1);
                        }
                        skip(2);
                        skipWhiteSpace();
                    } else if (c == '/') {
                        tokentype = SINGLETAG;
                        skip(1);
                    } else {
                        if (readbuffer.length() == 0) {
                            skip(1);
                        }
                    }
                }
                skip(1);
                params = Util.stackToTuple(paramsStack);
                if (Util.tupleIndexOf(autoclose, content) != -1) {
                    tokentype = SINGLETAG;
                }
            } else {
                readbuffer = new StringBuffer();
                tokentype = TEXT;
                while (!eof && !(startsWith("<"))) {
                    appendc();
                }
                content = readbuffer.toString();
                if (doTrim) {
                    content = content.trim();
                }
            }
        }
    }

//
// Parser
//
    private void closeTag() {
        if (!tagStack.empty()) {
            Object[] tag = Util.stackToTuple(currentTag);
            currentTag = (Stack) tagStack.pop();
            currentTag.push(tag);
        }
    }

    private void forceCloseTag(String tag) {
        while (!tagStack.empty() && !currentTag.elementAt(0).equals(tag)) {
            closeTag();
        }
        closeTag();
    }

    private void tryCloseTags(String tags[]) {
        while (!tagStack.empty()
                && -1 != Util.tupleIndexOf(tags, ((Stack) tagStack.peek()).elementAt(0))) {
            closeTag();
        }
    }

    private Object[] doParse() {
        while (!eof) {
            nextToken();
            switch (tokentype) {
            case STARTTAG:
                int closetag = Util.tupleIndexOf(closingTags, content);
                if (closetag != -1) {
                    tryCloseTags(closedTags[closetag]);
                }

                tagStack.push(currentTag);
                currentTag = new Stack();
                currentTag.push(content);
                currentTag.push(params);
                break;
            case ENDTAG:
                forceCloseTag(content);
                break;
            case SINGLETAG:
                Object tag[] = {content, params};
                currentTag.push(tag);
                break;
            case TEXT:
                currentTag.push(content);
                break;
            }
        }
        while (!tagStack.empty()) {
            closeTag();
        }
        return (Object[]) currentTag.elementAt(0);
    }
}
