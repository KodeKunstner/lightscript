function Token(type, val, subtype, start, end, newline, comment) {
    var token, nud, led, bp;
    var token_type = tok("(" + type + ")");
    if(token_type === undefined) {
        throw "internal error, unsupported token type: " + type;
    }

    if((type === "symbol" || type === "identifier") && token_handler[val] !== undefined) {
        token_type = token_handler[val];
    }

    // create token object
    token = Object.create(token_type);
    token.type = type;
    token.val = val;
    token.start = start;
    token.end = end;
    if(comment != "") {
        token.comment = comment;
    }
    if(newline) {
        token.newline = newline;
    }
    if(subtype != undefined) {
        token.subtype = subtype;
    }
    return token;
};

var token_prototype = {};
token_prototype.bp = 0;
token_prototype.led_op = function() { 
    this.syntax_error(this.val + "' cannot be infixed");  
}
token_prototype.nud_op = function() { 
    return this; 
}
token_prototype.comment = "";
token_prototype.newline= false;
token_prototype.children = [];

// Utility functions
token_prototype.syntax_error = function(msg) {
    throw "Syntax error at line " + this.start.line + ": " + msg;
}

token_prototype.readlist = function(list) {
    if(this.rparen === undefined) {
        throw "Internal error: no end-paren-type for '" + this.token_type + "'";
    }
    try {
        while(token.val !== this.rparen) {
            // TODO: handle end of file
            list.push(parse());
        }
    token = next_token()
    } catch(e) {
        if(e === StopIteration) {
            this.syntax_error("Unterminated list, missing " + this.rparen);
        } else {
            throw e;
        }
    }
    return list;
}

token_prototype.led = function(left) {
    var obj = this;
    if(obj.lid !== undefined) {
        obj.id = obj.lid;
    }
    obj = obj.led_op(left);
    if(typeof(obj.lupdate) === "function") {
        return obj.lupdate();
    } else {
        return obj;
    }
}

token_prototype.nud = function(left) {
    var obj = this;
    if(obj.nid !== undefined) {
        obj.id = obj.nid;
    }
    obj = obj.nud_op();
    if(typeof(obj.nupdate) === "function") {
        return obj.nupdate();
    } else {
        return obj;
    }
}

var l = function(name, op, bp, gen_id) {
    tok(name).led_op = op;

    if(bp !== undefined) {
        tok(name).bp = bp;
    }

    if(typeof(gen_id) === "function") {
        tok(name).lupdate = gen_id;
    } 

    if(typeof(gen_id) === "string") {
        tok(name).lid = gen_id;
    }

}

var n = function(name, op, gen_id) {
    tok(name).nud_op = op;

    if(typeof(gen_id) === "function") {
        tok(name).nupdate = gen_id;
    } 

    if(typeof(gen_id) === "string") {
        tok(name).nid = gen_id;
    } 
}

var infix = function(left) {
        this.children = [left, parse(this.bp)]; 
        return this;
}

var infixr = function(left) {
        this.children = [left, parse(this.bp - 1)]; 
        return this;
}

var infixparen = function(left) {
        this.children = this.readlist([left]);
        return this;
}

var prefix = function() {
    this.children = [parse()];
    return this;
}

var prefix2 = function() {
    this.children = [parse(), parse()];
    return this;
}

var atom = function() {
    this.children = [];
    return this;
}

var paren = function() {
    this.children = this.readlist([]);
    return this;
}


var token_handler = {};
var tok = function(id) {
    if(token_handler[id] === undefined) {
        token_handler[id] = Object.create(token_prototype);
        token_handler[id].token_type = id;
    }
    return token_handler[id];
}

// Default nuds/leds
tok("(symbol)").nud_op = function() { this.syntax_error(this.val + " is not af prefix function"); }

// Define what opposing parentheses matches
tok("(").rparen = ")";
tok("[").rparen = "]";
tok("{").rparen = "}";

// Leds
l(".", infix, 700, "subscript", function() {
        var key = this.children[1];
        assert(key.type === "identifier");
        key.type = "string";
        key.subtype= "dot_identifier";
});
l("(", infixparen, 600, "apply");
l("[", infixparen, 600, "subscript");
l("*", infix, 500, "mul");
l("/", infix, 500, "div");
l("%", infix, 500, "rem");
l("+", infix, 400, "add");
l("-", infix, 400, "sum");
l("===", infix, 300, "eq");
l("==", infix, 300, "eq");
l("!=", infix, 300, "neq");
l("!==", infix, 300, "neq");
l("<=", infix, 300, "leq");
l("<", infix, 300, "less");
l(">=", infix, 300, "geq");
l(">", infix, 300, "greater");
l("&&", infixr, 200, "and");
l("||", infixr, 200, "or");
l("else", infixr, 200);
l("=", infix, 100, "set");
l("+=", infix, 100);
l(",", infix, 50);
l(":", infix, 50);

// Nuds
n("(", paren);
n("{", paren, "dict");
n("[", paren, "list");
n("var", prefix);
n("return", prefix, "return");
n("-", prefix, "neg");
n("!", prefix, "not");
n("function", prefix2, "function");
n("if", prefix2, "if");
n("while", prefix2, "while");
n(";", atom);
n("undefined", atom, "nil");
n("null", atom, "nil");
n("None", atom, "nil");
n("true", atom, "true");
n("false", atom, "false");
n("True", atom, "true");
n("False", atom, "false");

// Test code, just run on standard in while in development
var tokens = tokenise(stdin);

var next_token = function() {
        token = tokens.next();
        return token;
}

next_token();
var parse = function(rbp) {
    rbp = rbp || 0;
    var t = token;
    next_token();
    var left = t.nud();
    while(rbp < token.bp) {
        t = token;
        next_token();
        left = t.led(left);
    }
    return left
};

var parser = {
    "__iterator__": function() { return this; },
    "next": parse
};
