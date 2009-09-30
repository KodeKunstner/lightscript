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
token_prototype.readlist = function(list) {
    if(this.rparen === undefined) {
        throw "Error: no end-paren-type for '" + this.token_type + "' in line " + this.start.line;
    }
    while(token.val !== this.rparen) {

        // TODO: handle end of file
        list.push(parse());
    }
    token = next_token()
    return list;
}

function led(name, op, bp, gen_id) {
    if(bp !== undefined) {
        tok(name).bp = bp;
    }
    if(typeof(gen_id) === "function") {
        tok(name).led_op = op;
        tok(name).lupdate = gen_id;
        tok(name).led = function(left) {
            var new_this = this.led_op(left);
            return new_this.lupdate();
        }
    } else if(typeof(gen_id) === "string") {
        tok(name).led_op = op;
        tok(name).lid = gen_id;
        tok(name).led = function(left) {
            this.id = this.lid;
            return this.led_op(left);
        }
    } else {
        tok(name).led = op;
    }
}

infix = function(left) {
        this.children = [left, parse(this.bp)]; 
        return this;
}

infixr = function(left) {
        this.children = [left, parse(this.bp - 1)]; 
        return this;
}

infixparen = function(left) {
        this.children = this.readlist([left], this.rparen);
        return this;
}

nud = function(name, op, gen_id) {
    if(typeof(id) === "string") {
        tok(name).nud_op = op;
        tok(name).nid = gen_id;
        tok(name).nud = function() {
            this.id = this.nid;
            return this.nud_op();
        }
    } else {
        tok(name).nud = op;
    }
}

prefix = function() {
        this.children = [parse()];
        return this;
    }
prefix2 = function() {
        this.children = [parse(), parse()];
        return this;
    }

atom = function() {
        this.children = [];
        return this;
    }

paren = function() {
        this.children = this.readlist([], this.rparen);
        return this;
    }


var token_handler = {};
function tok(id) {
    if(token_handler[id] === undefined) {
        token_handler[id] = Object.create(token_prototype);
        token_handler[id].token_type = id;
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

// Define what opposing parentheses matches
tok("(").rparen = ")";
tok("[").rparen = "]";
tok("{").rparen = "}";

// Leds
led(".", infix, 700, "subscript");
led("(", infixparen, 600, "apply");
led("[", infixparen, 600, "subscript");
led("*", infix, 500, "mul");
led("/", infix, 500, "div");
led("%", infix, 500, "rem");
led("+", infix, 400, "add");
led("-", infix, 400, "sum");
led("===", infix, 300, "eq");
led("==", infix, 300, "eq");
led("!=", infix, 300, "neq");
led("!==", infix, 300, "neq");
led("<=", infix, 300, "leq");
led("<", infix, 300, "less");
led(">=", infix, 300, "geq");
led(">", infix, 300, "greater");
led("&&", infixr, 200, "and");
led("||", infixr, 200, "or");
led("else", infixr, 200);
led("=", infix, 100, "set");
led("+=", infix, 100);
led(",", infix, 50);
led(":", infix, 50);

// Nuds
nud("(", paren);
nud("{", paren, "dict");
nud("[", paren, "list");
nud("var", prefix);
nud("return", prefix, "return");
nud("-", prefix, "neg");
nud("!", prefix, "not");
nud("function", prefix2, "function");
nud("if", prefix2, "if");
nud("while", prefix2, "while");
nud(";", atom);
nud("undefined", atom, "nil");
nud("null", atom, "nil");
nud("None", atom, "nil");
nud("true", atom, "true");
nud("false", atom, "false");
nud("True", atom, "true");
nud("False", atom, "false");

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
