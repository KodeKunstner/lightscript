function Token(type, val, subtype, start, end, newline, comment) {
    var token, nud, led, bp;

    // set nud/led/bp based on type
    if(type === "string" || type === "number") {
        nud = default_nud;
        led = syntax_error("infix string");
        bp = 0;
    } else  {
        nud = nuds[val];
        led = leds[val] || syntax_error("unsupported infix symbol");
        bp = bps[val] || 0;
        if(type === "symbol") {
            nud = nud || syntax_error("unsupported prefix symbol");
        } else if(type === "identifier") {
            nud = nud || default_nud;
        } else {
            throw "Unexpected token type";
        }
    }

    // create token object
    token = Object.create(token_prototype);
    token.type = type;
    token.val = val;
    token.start = start;
    token.end = end;
    token.newline = newline;
    token.comment = comment;
    token.nud = nud;
    token.led = led;
    token.bp = bp;
    return token;
};

token_prototype = {};

default_nud = function() { 
    this.children = [];
    return this; 
};

syntax_error = function(str) {
    return function() {
        throw "Syntax error -  " + str + " at line " + this.start.line + "\nContent: " + this.val;
    }
};

token_prototype.readlist = function(list, lparen) {
    while(token.val !== lparen) {

        // TODO: handle end of file
        list.push(parse());
    }
    this.end = token.end;
    token = next_token()
    return list;
}

infix = function(id, name, bp) {
    bp = bp || 0;
    bps[name] = bp;
    leds[name] = function(left) {
        this.id = id;
        this.subtype = "infix";
        var children = [left, parse(bp)];
        this.children = children;
        this.start = children[0].start;
        this.end = children[1].end;
        return this;
    };
};

infixr = function(id, name, bp) {
    bp = bp || 0;
    bps[name] = bp;
    leds[name] = function(left) {
        this.id = id;
        this.subtype = "infixr";
        var children = [left, parse(bp - 1)];
        this.children = children
        this.start = children[0].start;
        this.end = children[1].end;
        return this;
    };
};

infixparen = function(id, name, lparen, bp) {
    bp = bp || 0;
    bps[name] = bp;
    leds[name] = function(left) {
        this.id = id;
        this.subtype = "infixparen";
        this.children = this.readlist([left], lparen);
        this.start = this.children[0].start;
        return this;
    }
}

paren = function(id, name, lparen) {
    nuds[name] = function() {
        this.id = id;
        this.subtype = "paren";
        this.children = this.readlist([], lparen);
        return this;
    }
}

function prefix(id, name) { 
    nuds[name] = function() {
        this.id = id;
        this.subtype = "prefix";
        this.children = [parse()];
        this.end = this.children[0].end;
        return this;
    }
}
function prefix2(id, name) {
    nuds[name] = function() {
        this.id = id;
        this.subtype = "prefix";
        this.children = [parse(), parse()];
        this.end = this.children[1].end;
        return this;
    }
}

function atom(id, name) { 
    nuds[name] = function() {
        this.id = id;
        this.subtype = "prefix";
        this.children = [];
        return this;
    }
}
// \end TODO


var nuds = {};
var bps = {};
var leds = {};

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
