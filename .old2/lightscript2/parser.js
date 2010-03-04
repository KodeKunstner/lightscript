////////////////////////////////////////////
// Utility functions
//
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

function deepcopy(o) {
    if(typeof(o) === "string") {
        return o;
    } 
    var i, result;
    result = [];
    i = 0;
    while(i < o.length) {
        result.push(deepcopy(o[i]));
        ++i;
    }
    return result;
}


/////////////////////////////////////////////
// Tokeniser
//
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

////////////////////////////////////////////
// Token creation
//
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

///////////////////////////////////////
// Operator constructors
//
var infix = function(id, bp) {
    tok(id).bp = bp;
    tok(id).led = function(left) {
        return [this.id, left, parse(this.bp)];
    };
};

var infixswap = function(id, bp) {
    tok(id).bp = bp;
    tok(id).led = function(left) {
        return [this.id, parse(this.bp), left];
    };
};


var infixr = function(id, bp) {
    tok(id).bp = bp;
    tok(id).led = function(left) {
        return [this.id, left, parse(this.bp - 1)];
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
        if(t[0] !== '(sep)') {
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
        return [this.id, parse()];
    }
};

prefix2 = function(id) {
    tok(id).nud = function() {
        return [this.id, parse(), parse()];
    }
};

literal = function(id) {
    tok(id).nud = function() {
        return [this.id, this.val];
    }
}

var opassign = function(id) {
    tok(id).op = id.substring(0, id.length - 1);
    tok(id).bp = 100;
    tok(id).led = function(left) {
        return ['=', left, [this.op, deepcopy(left), parse(this.bp)]];
    };
};

var prefixop = function(id){
    tok(id).op = id.substring(0, id.length - 1);
    tok(id).nud = function() {
        var t = parse();
        return ['=', t, [this.op, deepcopy(t), ['(num)', '1']]];
    };
};

/////////////////////////////////////////////////////
// Definition of operator precedence and type
//
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
tok('!=').id = '!==';
infix('<=', 300);
infix('<', 300);
infixswap('>=', 300);
tok('>=').id = '<=';
infixswap('>', 300);
tok('>').id = '<';
infixr('&&', 200);
infixr('||', 200);
infix('=', 100);
infix('in', 50);
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
list('var', ';');
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
infixr('else', 200);
prefix2('while');
prefix2('if');
prefix2('for');
prefix2('function');
prefix2('try');
prefix2('catch');
prefixop('--');
prefixop('++');

//////////////////////////////////////////
// Build parse tree
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


//////////////////////////////////////////
// Change parsetree into valid lightscript parse tree
//

var block = function(node) {
    var i, j, result;
    if(node[0] !== 'list{') {
        return expr(node);
    }
    result = ['begin'];
    i = 1;
    while(i < node.length) {
        if(node[i][0] === '(comment)' && result[result.length - 1][0] === 'comment') {
            result[result.length-1][1] += '\n' + node[i][1];
        } else {
            var t = expr(node[i]);
            if(t[0] === 'begin') {
                j = 1;
                while(j < t.length) {
                    result.push(t[j]);
                    ++j;
                };
            } else {
                result.push(t);
            }
        }
        ++i;
    }
    return result;
};


var mapexpr = function(node) {
    var i = 1;
    while(i<node.length) {
        node[i] = expr(node[i]);
        ++i;
    }
};

var simple_mappings = {
    '+': 'add',
    '*': 'mul',
    '/': 'div',
    '%': 'mod',
    '!': 'not',
    '===': 'eq',
    '!==': 'neq',
    '<': 'less',
    '<=': 'leq',
    '&&': 'and',
    '||': 'or',
    'apply[': 'subscript',
    'apply(': 'call',
    'in': 'in',
    'list[': 'array',
    'list{': 'dict',
    '(identifier)': 'id',
    '(num)': 'num',
    '(string)': 'str',
    '(comment)': 'comment',
    'return': 'return',
    'arglist': 'arglist',
    'undefined': 'undefined',
    'true': 'true',
    'false': 'false'
};

locals = {};

var expr = function(node) {
    var type, result, i, t;
    if(typeof(node) === "string") {
        return node;
    }

    type = node[0];

    if(simple_mappings[type] !== undefined) {
        mapexpr(node);
        node[0] = simple_mappings[type];
        return node;
    } else if(type === '-') {
        mapexpr(node);
        if(node.length === 2) {
            node[0] = 'neg';
        } else {
            node[0] = 'sub';
        }
        return node;
    } else if(type === 'listvar') {
        result = ["list{"];
        i = 1;
        while(i < node.length) {
            if(node[i][0] === '(identifier)') {
                locals[node[i][1]] = true;
            } else if(node[i][0] === '=' && node[i][1][0] === '(identifier)') {
                locals[node[i][1][1]] = true;
                result.push(node[i]);
            } else {
                node.unshift("parse-error");
                return node;
            }
            ++i;
        }
        return block(result);
    } else if(type === 'function') {
        var prevlocals = locals;
        locals = {}
        if(node[1][0] === 'apply(') {
            result = ['set', expr(node[1][1]), node];
            node[1].shift();
            node[1][0] = 'arglist';
        } else if(node[1][0] == 'list(') {
            result = node;
            node[1][0] = 'arglist';
        } else {
            return ['parse-error', node];
        }
        mapexpr(node[1]);
        node[3] = block(node[2]);
        node[2] = ['locals'];
        for(name in locals) {
            node[2].push(name);
        }
        locals = prevlocals;
        return (result);
    } else if(type === 'while') {
        node[1] = expr(node[1]);
        node[2] = block(node[2]);
        return node;
    } else if(type === 'list(') {
        if(node.length !== 2) {
            return ['parse-error', 'expr', node];
        }
        return expr(node[1]);
    } else if(type === 'for') {
        node[1] = expr(node[1]);
        node[2] = block(node[2]);
        if(node[1][0] !== 'in') {
            return ['parse-error', node];
        }
        return ['for', node[1][1], node[1][2], node[2]];
    } else if(type === 'else') {
        node[1] = block(node[1]);
        node[2] = block(node[2]);
        return node;
    } else if(type === '.') {
        mapexpr(node);
        node[0] = 'subscript';
        if(node[2][0] !== 'id') {
            return ['parse-error', 'expr', node];
        } else {
            node[2][0] = 'str';
        }
        return node;
    } else if(type === '=') {
        mapexpr(node);
        if(node[1][0] === 'subscript') {
            node.unshift('put');
            node[1] = node[2][1];
            node[2] = node[2][2];
        } else {
            node[0] = 'set';
        }
        return node;
    } else if(type === 'if') {
        node[0] = 'cond';
        node[1] = expr(node[1]);
        node[2] = block(node[2]);
        if(node[2][0] === 'else') {
            t = node[2][2];
            node[2] = node[2][1];
            if(t[0] === 'cond') {
                i = 1;
                while(i < t.length) {
                    node.push(t[i]);
                    ++i;
                }
            } else {
                node.push(t);
            }
        } 
        return node;
    } 
    node.unshift('unsupported-node');
    node.unshift('parse-error');
    return node;
};


//
// dump
//
t = readlist(['list{'], '');
t = block(t);
i = 0;

var heads = { } ;

var list_to_str = function(t) {
    if(typeof(t) === "string") {
        return "'" + t + "'"
    } else  {
        var result = "(" + t[0];
        heads[t[0]] = true;
        var i = 1; 
        while(i < t.length) {
            result += " " + list_to_str(t[i]);
            ++i;
        }
        return result + ")";
    }
};

while(i<t.length) {
    print(list_to_str(t[i]));
    ++i;
}

t = [];
for(x in heads) {
    t.push(x);
};
print("\nfunctions used by parser:");
print(t.sort().join("\n"));

