tokenise = function(inputstream) {
    var buffer = "";
    var line = 0;
    var pos = 0;
    var start;
    var newline = false;
    var comment = "";
    var iterator = LightScriptIterator(inputstream);
    
    function len(list) {
        return list.length;
    }
    
    function one_of(str) {
        return LightScriptContains(peek(), str);
    }
    
    function starts_with(str) {
        return peek(len(str)) === str;
    }
    
    function ensure_buffer(size) {
        while(len(buffer) < size) {
            var c = inputstream.next();
            if(c !== undefined) {
                buffer = buffer + c;
            } else {
                buffer = buffer + "\0";
            }
        }
    }
    
    function peek(n, pos) {
        n = n || 1;
        pos = pos || 0;
        ensure_buffer(pos + n);
        return buffer.slice(pos, pos+n);
    }
    
    function pop(n) {
        n = n || 1;
        var result = peek(n);
        if(result[0] == "\0") {
            throw StopIteration;
        }
        buffer = buffer.slice(n);
    
        // keep track of position in stream
        for(c in LightScriptIterator(result)) {
            pos = pos + 1;
            if(c === "\n") {
                pos = 0;
                line = line + 1;
            }
        }
        return result;
    }
    
    function current_position() {
        return {"line": line, "pos": pos};
    }
    
    function begin_token() {
        start = current_position();
    }
    
    function new_token(type, val, subtype) {
        var result = Token(type, val, subtype, start, current_position, newline, comment);
        newline = false;
        comment = "";
        return result;
    }
    
    function next() {
        var whitespace = " \t\r";
        var single_symbol = "(){}[].:;,";
        var joined_symbol = "=+-*/<>%!|&";
        var ident = "_qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
        var digits = "0123456789";
        var hexdigits = digits + "abcdefABCDEF";
        var s, c, quote, subtype;
    
        // repeat until token parsed
        while(true) {
    
            // Keep track of postion
            begin_token();
    
            // Whitespace
            if(one_of(whitespace)) {
                pop();
    
            // Comment
            } else if(starts_with("//") || starts_with("#")) {
                if(pop() === "/") {
                    pop();
                }
                s = "";
                while(peek() !== "\n") {
                    s += pop();
                }
                comment = comment + s + "\n";
    
            // Unescaped string
            } else if(starts_with('"""')) {
                pop(3);
                s = "";
                while(!starts_with('"""')) {
                    s += pop();
                }
                pop(3);
                return new_token("string", s, '"""');
    
            // String
            } else if(one_of("'\"")) {
                s = "";
                quote = pop();
                while(!starts_with(quote)) {
                    c = pop();
                    if(c === "\\") {
                        c = pop();
                        c = {"n": "\n", "r": "\r", "t": "\t"}[c] || c;
                    }
                    s += c;
                }
    
                // remove end-quote
                pop();
                return new_token("string", s, quote);
    
            // Number
            } else if(one_of(digits)) {
                s = pop();
                subtype = "int";
    
                // normal or hexadecimal
                if(peek() !== 'x') {
                    while(one_of(".e" + digits)) {
                        if(peek() == ".") {
                            subtype = "float";
                        }
                        if(peek() == "e") {
                            subtype = "scientific";
                        }
                        s += pop();
                    }
                } else {
                    subtype = "hexadecimal";
                    s = pop(2);
                    while(one_of(hexdigit)) {
                        s += pop();
                    }
                }
                return new_token("number", s, subtype);
    
            // Symbol
            } else if(one_of(single_symbol)) {
                return new_token("symbol", pop());
            } else if(one_of(joined_symbol)) {
                s = "";
                while(one_of(joined_symbol)) {
                    s += pop();
                }
                return new_token("symbol", s);
    
            // Identifier
            } else if(one_of(ident)) {
                s = "";
                while(one_of(ident + digits)) {
                    s += pop();
                }
                return new_token("identifier", s);
    
            // Newline
            } else if(peek() === "\n") {
                pop();
                newline = true;
            } else if(peek() === "\0") {
                throw StopIteration;
            } else {
                throw "Tokenisation error: " + peek().charCodeAt(0) + " '" + buffer + "' at line " + line;
            }
        }
    }
    return { "__iterator__": function() { return this; }, "next": next };
}
