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
    token.id = token.type;
    token.val = val;
    token.start = start;
    token.end = end;
    if(typeof(comment) === "string" && comment !== "") {
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

function Node(type, arg) {
    var result = Object.create(token_prototype);
    if(typeof(arg) ==="string") {
        result.val = arg;
    } else if(arg instanceof Array) {
        result.children = arg;
    } else {
        throw "Internal Error in Node";
    }
    result.id = type;
    return result;
}

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
token_prototype.to_string = function() {
    if(this.id === "string") {
        return '"' + this.val + '"';
    } 
    if(this.id === "identifier" || this.id === "number") {
        return this.val
    } 
    var result = "(" + this.id;
    for(child in LightScriptIterator(this.children)) {
        result += " " + child.to_string();
    }
    return result + ")";
};

token_prototype.syntax_error = function(msg) {
    throw "Syntax error at line " + this.start.line + ": " + msg;
};

token_prototype.readlist = function(list) {
    if(this.rparen === undefined) {
        throw "Internal error: no end-paren-type for '" + this.token_type + "'";
    }
    try {
        while(token.val !== this.rparen) {
            // TODO: handle end of file
            var t = parse();
            if(t.id !== "separator") {
                list.push(t);
            }
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
};

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
};

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
};

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

};

var n = function(name, op, gen_id) {
    tok(name).nud_op = op;

    if(typeof(gen_id) === "function") {
        tok(name).nupdate = gen_id;
    } 

    if(typeof(gen_id) === "string") {
        tok(name).nid = gen_id;
    } 
};

var infix = function(left) {
        this.children = [left, parse(this.bp)]; 
        return this;
};

var infixr = function(left) {
        this.children = [left, parse(this.bp - 1)]; 
        return this;
};

var infixparen = function(left) {
        this.children = this.readlist([left]);
        return this;
};

var prefix = function() {
    this.children = [parse()];
    return this;
};

var prefix2 = function() {
    this.children = [parse(), parse()];
    return this;
};

var atom = function() {
    this.children = [];
    return this;
};

var paren = function() {
    this.children = this.readlist([]);
    return this;
};


var token_handler = {};
var tok = function(id) {
    if(token_handler[id] === undefined) {
        token_handler[id] = Object.create(token_prototype);
        token_handler[id].token_type = id;
    }
    return token_handler[id];
};

var block = function(node) {
    if(node.id !== "dictionary") {
        var result = Token("identifier", "begin").nud();
        result.id = "begin";
        result.children = [node];
        return result;
    } else {
        node.id = "begin";
        return node;
    }
};

// Default nuds/leds
tok("(symbol)").nud_op = function() { this.syntax_error(this.val + " is not af prefix function"); };

// Define what opposing parentheses matches
tok("(").rparen = ")";
tok("[").rparen = "]";
tok("{").rparen = "}";
tok("var").rparen = ";";

// Leds
l(".", infix, 700, function() {
        this.id = "subscript";
        var key = this.children[1];
        if(key.type !== "identifier") {
            this.syntax_error("unexpected entity within dot-notation");
        }
        key.id = "string";
        key.subtype= "dot_identifier";
        return this;
});
l("(", infixparen, 600, function() {
        this.id = "apply";
        return this;
});
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
l("else", infixr, 100, "else");
l("=", infix, 100, function() {
        if(this.children[0].id == "subscript") {
            this.id = "put"
            this.children.unshift(this.children[0].children[0]);
            this.children[1] = this.children[1].children[1];
        } else {
            this.id = "set";
        }
        return this;
});
l("+=", infix, 100, "TODO:op=");
// TODO: Below, should these be represented and worked with as linked lists,
// or just thrown away while making lists. Currently just thrown away to get
// implementation up and running, but later use as linked-list for tuples 
// etc.
//l(":", infixr, 60, "colon");
//l(",", infixr, 40, "tuple");

// Nuds
// TODO: "for" "try" "catch" 
n("(", paren, "paren");
n("{", paren, "dictionary");
n("[", paren, "array");
n("var", paren, "local");
n("return", prefix, "return");
n("-", prefix, "neg");
n("!", prefix, "not");
n("throw", prefix, "throw");
n("typeof", prefix, "typeof");
n("function", prefix2, function() {
        this.id = "function";
        this.children[1] = block(this.children[1]);
        var result = this;
        if(this.children[0].id === "apply") {
            var fn_name = this.children[0].children[0];
            var result = Token("set");
            result.children = [fn_name, this];
            this.children[0].children.shift(1);
            this.children[0].id = "paren";
        }

        // Resolve scope
        var params = [];
        var locals = [];
        var globals = [];
        this.params = params;
        this.locals = locals;
        this.globals = globals;
        for(param in LightScriptIterator(this.children[0].children)) {
            assert(param.id === "identifier");
            params.push(param.val);
        }
        var resolve_scope = function(node) {
            if(node.id === "local") {
                var varname;
                for(varname in LightScriptIterator(node.children)) {
                    if(varname.id === "identifier") {
                        if(!LightScriptContains(varname.val, params)) {
                            if(!LightScriptContains(varname.val, locals)) {
                                locals.push(varname.val);
                            }
                        }
                    } else if(varname.id === "set") {
                        assert(varname.children[0].id === "identifier");
                        if(!LightScriptContains(varname.children[0].val, params)) {
                            if(!LightScriptContains(varname.children[0].val, locals)) {
                                locals.push(varname.children[0].val);
                            }
                        }
                        resolve_scope(varname.children[1]);
                    }
                }
                node.id = "begin";
            } else if(node.id === "identifier") {
                if(!LightScriptContains(node.val,params)) {
                    if(!LightScriptContains(node.val,locals)) {
                        if(!LightScriptContains(node.val, globals)) {
                            globals.push(node.val);
                        }
                    }
                }
            } else if(node.id === "function") {
                var elem;
                for(elem in LightScriptIterator(node.globals)) {
                    if(!LightScriptContains(elem.val,params)) {
                        if(!LightScriptContains(elem.val,locals)) {
                            if(!LightScriptContains(elem.val, globals)) {
                                globals.push(elem.val);
                            }
                        }
                    }
                }
            } else {
                var elem;
                for(elem in LightScriptIterator(node.children)) {
                    resolve_scope(elem);
                }
            }
        }
        assert(this.children.length === 2);
        resolve_scope(this.children[1]);
        var i = 0; 
        while(i<locals.length) {
            locals[i] = Node("identifier", locals[i]);
            i += 1;
        }
        i = 0; 
        while(i<globals.length) {
            globals[i] = Node("identifier", globals[i]);
            i += 1;
        }
        i = 0; 
        while(i<params.length) {
            params[i] = Node("identifier", params[i]);
            i += 1;
        }
        this.children = [this.children[0], Node("paren", locals), Node("paren", globals), this.children[1]];
        return result;
});
n("if", prefix2, function() {
        this.id = "cond";
        if(this.children[1].id === "else") {
            var tmp = this.children[1];
            this.children[1] = block(tmp.children[0]);
            if(tmp.children[1].id == "cond") {
                this.children = this.children.concat(tmp.children[1].children);
            } else {
                this.children.push(block(tmp.children[1]));
            }
        } else {
            this.children[1] = block(this.children[1]);
        }
        return this;
});
n("while", prefix2, function() {
        this.id = "while";
        this.children[1] = block(this.children[1]);
        return this;
});
n(";", atom, "separator");
n(":", atom, "separator");
n(",", atom, "separator");
n("undefined", atom, "nil");
n("this", atom, "this");
n("null", atom, "nil");
n("None", atom, "nil");
n("true", atom, "true");
n("false", atom, "false");
n("True", atom, "true");
n("False", atom, "false");
n("global", atom, "global");

var TOKEN_VOID = Token("identifier", "undefined", undefined).nud();
TOKEN_VOID.id = "void";

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
function f() {
    function g(a) {
        var b;
        c;
    }
}
;
