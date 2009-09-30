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
    token.newline = newline;
    token.comment = comment;
    return token;
};


default_nud = function() { 
    this.children = [];
    return this; 
};

syntax_error = function(str) {
    return function() {
        throw "Syntax error -  " + str + " at line " + this.start.line + "\nContent: " + this.val;
    }
};

token_prototype = {};
token_prototype.bp = 0;
token_prototype.readlist = function(list, lparen) {
    while(token.val !== lparen) {

        // TODO: handle end of file
        list.push(parse());
    }
    token = next_token()
    return list;
}


infix = function(id, name, bp) {
    bp = bp || 0;
    tok(name).bp = bp;
    tok(name).lid = id;
    tok(name).led = function(left) {
        var children = [left, parse(bp)];
        this.children = children;
        return this;
    };
};

infixr = function(id, name, bp) {
    bp = bp || 0;
    tok(name).bp = bp;
    tok(name).lid = id;
    tok(name).led = function(left) {
        var children = [left, parse(bp - 1)];
        this.children = children
        return this;
    };
};

infixparen = function(id, name, lparen, bp) {
    bp = bp || 0;
    tok(name).bp = bp;
    tok(name).lid = id;
    tok(name).led = function(left) {
        this.children = this.readlist([left], lparen);
        return this;
    }
}

paren = function(id, name, lparen) {
    tok(name).nid = id;
    tok(name).nud = function() {
        this.children = this.readlist([], lparen);
        return this;
    }
}

function prefix(id, name) { 
    tok(name).nid = id;
    tok(name).nud = function() {
        this.children = [parse()];
        return this;
    }
}
function prefix2(id, name) {
    tok(name).nid = id;
    tok(name).nud = function() {
        this.children = [parse(), parse()];
        return this;
    }
}

function atom(id, name) { 
    tok(name).nid = id;
    tok(name).ntype = "atom";
    tok(name).nud = function() {
        this.children = [];
        return this;
    }
}
// \end TODO


var token_handler = {};
function tok(id) {
    if(token_handler[id] === undefined) {
        token_handler[id] = Object.create(token_prototype);
    }
    return token_handler[id];
}

// Default nuds/leds
tok("(string)").nud = default_nud;
tok("(string)").led = syntax_error("infix string");
tok("(number)").nud = default_nud;
tok("(number)").led = syntax_error("infix number");
tok("(identifier)").nud = default_nud;
tok("(identifier)").led = syntax_error("unsupported infix identifier");
tok("(symbol)").nud = syntax_error("unsupported prefix symbol");
tok("(symbol)").led = syntax_error("unsupported infix symbol");

infix("subscript", ".", 700);
infixparen("apply", "(", ")", 600);
infixparen("subscript", "[", "]", 600);
infix("mul", "*", 500);
infix("div", "/", 500);
infix("rem", "%", 500);
infix("add", "+", 400);
infix("sum", "-", 400);
infix("eq", "===", 300);
infix("eq", "==", 300);
infix("neq", "!=", 300);
infix("neq", "!==", 300);
infix("leq", "<=", 300);
infix("less", "<", 300);
infixr("and", "&&", 200);
infixr("or", "||", 200);
infixr(undefined, "else", 200);
infix("set", "=", 100);
infix("+=", 100);
infix(undefined, ",", 50);
infix(undefined, ":", 50);
paren(undefined, "(", ")", 0);
paren("dict", "{", "}", 0);
paren("list", "[", "]", 0);
atom(undefined, ";");
prefix(undefined, "var");
prefix("return", "return");
prefix("neg", "-");
prefix("not", "!");
prefix2("function", "function");
prefix2("if", "if");
prefix2("while", "while");
atom("nil", "undefined");
atom("nil", "null");
atom("nil", "None");
atom("true", "true");
atom("false", "false");
atom("true", "True");
atom("false", "False");

tokens = tokenise(stdin);

function next_token() {
        token = tokens.next();
        return token;
}

token = tokens.next();
parse = function(rbp) {
    rbp = rbp || 0;
    var t = token;
    token = next_token();
    var left = t.nud();
    while(rbp < token.bp) {
        t = token;
        token = next_token();
        left = t.led(left);
    }
    return left
};

parser = {
    "__iterator__": function() { return this; },
    "next": parse
}
