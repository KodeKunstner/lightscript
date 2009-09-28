function Token(type, val, subtype, start, end, newline, comment) {
    var token, nud, led, bp;

    // set nud/led/bp based on type
    if(type === "string" || type === "number") {
        nud = return_this;
        led = syntax_error("infix string");
        bp = 0;
    } else  {
        nud = nuds[val];
        led = leds[val] || syntax_error("unsupported infix symbol");
        bp = bps[val] || 0;
        if(type === "symbol") {
            nud = nud || syntax_error("unsupported prefix symbol");
        } else if(type === "identifier") {
            nud = nud || return_this;
        } else {
            throw "Unexpected token type";
        }
    }

    // create token object
    token = {};
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

return_this = function() { return this; };

syntax_error = function(str) {
    return function() {
        throw "Syntax error -  " + str + " at line " + this.start.line + "\nContent: " + this.val;
    }
};

infix = function(name, bp) {
    bp = bp || 0;
    bps[name] = bp;
    leds[name] = function(left) {
        this.subtype = "infix";
        this.children = [left, parse(this.bp)];
        return this;
    };
};

infixr = function(name, bp) {
    bp = bp || 0;
    bps[name] = bp;
    leds[name] = function(left) {
        this.subtype = "infixr";
        this.children = [left, parse(this.bp - 1)];
        return this;
    };
};

// \begin TODO
function infixlist() { }
function end() { }
function seperator() { }
function list() { }
function prefix() { }
function prefix2() { }
function atom() { }
// \end TODO


var nuds = {};
var bps = {};
var leds = {};

infix(".", 700);
infixlist("(", 600);
infixlist("[", 600);
infix("*", 500);
infix("%", 500);
infix("+", 400);
infix("-", 400);
infix("===", 300);
infix("!==", 300);
infix("<=", 300);
infix("<", 300);
infixr("&&", 200);
infixr("||", 200);
infixr("else", 200);
infix("=", 100);
end("]");
end(")");
end("}");
end("(EOF)");
seperator(":");
seperator(";");
seperator(",");
list("(");
list("{");
list("[");
prefix("var");
prefix("return");
prefix("-");
prefix("!");
prefix2("function");
prefix2("if");
prefix2("while");
atom("undefined");
atom("true");
atom("false");

tokens = tokenise(stdin);

function next_token() {
        return tokens.next();
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

