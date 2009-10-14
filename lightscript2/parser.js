Object.create = function(obj) {
    var F = function() { };
    F.prototype = obj;
    return new F();
}


getch = (function() {
    var line = "";
    var pos;
    var newl= 0;
    return function() {
        ++pos;
        if(line[pos] !== undefined) {
            newl= 0;
            return line[pos];
        } else {
            pos = -1;
            line = readline();
            ++newl;
            if(newl > 10) {
                return undefined;
            } else {
                return "\n";
            } 
        }
    }
})();


// Tokeniser
var c = ' ';
var str = "";
function char_is(str) {
    return str.indexOf(c) !== -1;
};
var skip_char, push_char, pop_string;
skip_char = function() {
    c = getch();
}
push_char = function() {
    str += c;
    skip_char();
}
pop_string = function() {
    var result = str;
    str = "";
    return result;
}
var symb = '=!<>&|/*+-%';
var num = "1234567890";
var alphanum = num + '_qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
next_token = function() {
    while(char_is(' \n\r\t')) {
            skip_char();
    }
    if(c === '\'' || c === '"') {
                var quote = c;
                skip_char();
        while(c !== undefined && c !== quote) {
            if(c === '\\') {
                                skip_char();
                if(c === 'n') {
                    c = '\n';
                } else if(c === 't') {
                    c = '\t';
                }
            }
                        push_char();
        }
                skip_char();
                return fetch_token("(string)", pop_string());
    } else if(char_is(num)) {
        while(char_is(num)) {
                        push_char();
        }
                return fetch_token("(num)", pop_string());
    } else if(char_is(alphanum)) {
        while(char_is(alphanum)) {
                        push_char();
        }
                return fetch_token(pop_string());

    // read comment or multi character symbol
    } else if(char_is(symb)) {
                if(c === '/') {
                    push_char();
                    if(c === '/') {
                        skip_char();
                        pop_string();
                        while(c !== undefined && c !== '\n') {
                            push_char();
                        }
                        return fetch_token("(comment)", pop_string());
                    }
                }
        while(char_is(symb)) {
                        push_char();
        }
                return fetch_token(pop_string());
    } else if(c === undefined) {
            return fetch_token("(eof)");
    } else  {
            push_char();
            return fetch_token(pop_string());
    }
};

var filter = function(fn, list) {
    var result = [];
    for(var i = 0; i < list.length; ++i) {
        if(fn(list[i])) {
            result.push(list[i]);
        }
    }
    return result;
}

// Token creation
var fetch_token = function(type, val) {
    var obj = token_types[type];
    if(obj === undefined) {
        val = type;
        obj = token_types["(identifier)"];
    }

    if(val !== undefined) {
        obj = Object.create(obj);
        obj.val = val;
    }
    return obj;
};

super_token = {
    "nud": function() { 
        return ["parse-error", "undefined-nud", this.id, this.val]; 
    },
    "bp": 0,
    "led": function(left) { 
        return ["parse-error", "undefined-led", this.id, this.val, left]; 
    }
};

token_types = {};
var tok = function(name) {
    var result = token_types[name];
    if(result === undefined) {
        result = Object.create(super_token);
        result.id = name;
        token_types[name] = result;
    }
    return result;
};

// Operator constructors
var infix = function(id, bp) {
    tok(id).bp = bp;
    tok(id).led = function(left) {
        return [this.id, expr(left), expr(parse(this.bp))];
    };
};

var infixr = function(id, bp) {
    tok(id).bp = bp;
    tok(id).led = function(left) {
        return [this.id, expr(left), expr(parse(this.bp - 1))];
    };
};

var infixlist = function(id, endsymb, bp) {
    tok(id).bp = bp;
    tok(id).endsymb = endsymb;
    tok(id).led = function(left) {
        return readlist(['apply' + this.id, left], this.endsymb);
    };
};
var readlist = function(acc, endsymb) {
    while(token.id !== endsymb && token.id !== "(eof)") {
        var t = parse();
        if(!t.sep) {
            acc.push(t);
        } 
    }
    token = next_token();
    return acc;
};

var list = function(id, endsymb) {
    tok(id).endsymb = endsymb;
    tok(id).nud = function() {
        return readlist(['list' + this.id], this.endsymb);
    }
};

atom = function(id) {
    tok(id).nud = function() {
        return [this.id];
    }
};
sep = function(id) {
    tok(id).sep = true;
    tok(id).nud = function() {
        return ['(sep)', this.id];
    }
};

prefix = function(id) {
    tok(id).nud = function() {
        return [this.id, expr(parse())];
    }
};


literal = function(id) {
    tok(id).nud = function() {
        return [this.id, this.val];
    }
}

opassign = function(id) {
    var op = id.substring(0, id.length - 1);
    tok(id).bp = 100;
    tok(id).led = function(left) {
                left = expr(left);
        return ['=', left, [op, left, expr(parse(this.bp))]];
    };
}
function tailmap(fn, list) {
    var i = 1;
    while(i < list.length) {
        list[i] = fn(list[i]);
        ++i;
    }
}
function expr(node) {
    var type = node[0]
    if(type === 'list(') {
        if(node.length !== 2) {
            return ['parse-error', 'expression', node];
        } else {
            return expr(node[1]);
        }
    } else if(type === 'list[') {
        node[0] = 'array';
        tailmap(expr, node);
    } else if(type === 'list{') {
        node[0] = 'dictionary';
        tailmap(expr, node);
    } else if(type === 'apply(') {
        node[0] = 'call';
        tailmap(expr, node);
    } else if(type === 'apply[') {
        if(node.length !== 3) {
            return ['parse-error', 'expression', node];
        }
        node[0] = 'subscript';
        node[1] = expr(node[1]);
    } else if(type === '.') {
        node[0] = 'subscript';
        if(node[2][0] !== '(identifier)') {
            return ['parse-error', 'expression', node];
        } else {
            node[2][0] = '(string)';
        }
    } else if(type == '=') {
        if(node[1][0] === 'subscript') {
            node.unshift('put');
            node[1] = node[2][1];
            node[2] = node[2][2];
        } else {
            node[0] = 'set';
        }
    } 
    return node;
}
function block(node) {
    if(node[0] !== 'list{') {
        return expr(node[0]);
    }
    var result = ['begin'];
    var i = 1;
    while(i < node.length) {
        // skip separators
        if(node[i][0] === '(sep)') {

        // join comments
        } else if(node[i][0] === '(comment)' && result[result.length - 1][0] === '(comment)') {
            result[result.length-1][1] += '\n' + node[i][1];
        } else {
            result.push(expr(node[i]));
        }
        ++i;
    }
    return result;
}

// Definition of operator precedence and type
infix('.', 700);
infixlist('(', ')', 600);
infixlist('[', ']', 600);
infix('*', 500);
infix('%', 500);
infix('/', 500);
infix('+', 400);
infix('-', 400);
infix('===', 300);
infix('==', 300);
tok('==').id = '===';
infix('!==', 300);
infix('!=', 300);
tok('==').id = '===';
infix('<=', 300);
infix('<', 300);
infix('>=', 300);
infix('>', 300);
infixr('&&', 200);
infixr('||', 200);
infix('=', 100);
opassign('+=');
opassign('-=');
opassign('*=');
opassign('/=');
opassign('%=');
sep(':');
sep(';');
sep(',');
list('(', ')');
list('{', '}');
list('[', ']');
tok('var').nud = function() {
    return filter( function(x) { 
            return x[0] !== "(sep)" && x[1] !== ",";
        }, readlist(['var'], ';'));
}
prefix('return');
prefix('-');
prefix('!');
prefix('throw');
atom('undefined');
atom('null');
atom(']');
atom(')');
atom('}');
atom('(eof)');
tok('null').id = 'undefined';
atom('true');
atom('false');
literal('(identifier)');
literal('(string)');
literal('(num)');
literal('(comment)');
tok('else').bp = 200;
// TODO: try-catch
tok('else').led = function(left) {
    return [this.id, left, parse(this.bp - 1)];
};
tok('while').nud = function() {
    return ['while', expr(parse()), block(parse())];
}
tok('if').nud = function() {
    var cond = parse();
    var body = parse();
    var result;
    if(body[0] === 'else') {
        if(body[2][0] === 'cond') {
            result = body[2];
            result.unshift('cond', expr(cond));
            result[2] = block(body[1]);
            return result;
        }
        return ['cond', expr(cond), block(body[1]), block(body[2])];
    } else {
        return ['cond', expr(cond), block(body)];
    }
}
tok('function').nud = function() {
    var args = parse();
    var result = ['function', args, block(parse())];
    if(args[0] === "apply(") {
        var name = args[1];
        args.shift();
        result =  ['=', name, result];
    } else if(args[0] !== "list(") {
        return ['parse-error', 'function', result];
    }
    // todo: scope analysis
    args[0] = 'arglist'
    return result;
}
tok('--').nud = function() {
    var t = parse();
    return ['=', t, ['-', t, ['(num)', '1']]];
}
tok('++').nud = function() {
    var t = parse();
    return ['=', t, ['+', t, ['(num)', '1']]];
}

// Core parser
token = next_token();
parse = function(rbp) {
    rbp = rbp || 0;
    var t = token;
    token = next_token();
    var left = t.nud();
    while(rbp < token.bp && !t.sep) {
        t = token;
        token = next_token();
        left = t.led(left);
    }
    return left
};

// TODO: renaming of ops

//
// dump
//
// TODO: prettyprinter
t = block(readlist(['list{'], '}'));
i = 0;
var list_to_str = function(t) {
    if(t instanceof Array) {
        var result = "( ";
        for(var i = 0; i < t.length; ++i) {
            result += list_to_str(t[i]) + " ";
        }
        return result + ")";
    } else if(typeof(t) === "string") {
        return "'" + t + "'"
    } else {
        print("ERROR: wrong type for listprint - " + t);
    }
}

while(i<t.length) {
    print(list_to_str(t[i]));
    ++i;
}
