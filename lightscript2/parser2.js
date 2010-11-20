////////////////////////////////////////////
// Utility functions
//
clone = function(obj) {
    var F = function() { };
    F.prototype = obj;
    return new F();
}

getch = (function() {
    var line = '';
    var pos;
    var newl = 0;
    return function() {
        pos = pos + 1;
        if (line[pos] !== undefined) {
            newl= 0;
            return line[pos];
        } else {
            pos = -1;
            line = readline();
            newl = newl + 1;
            if (newl > 10) {
                return undefined;
            } else {
                return '\n';
            }
        }
    }
})();

function isArray(o) {
    return o.constructor === Array;
};

function deepcopy(o) {
    var i, result;
    if (typeof(o) === 'string') {
        return o;
    } else if(isArray(o)) {
        result = [];
        i = 0;
        while (i < o.length) {
            result.push(deepcopy(o[i]));
            i = i + 1;
        }
        return result;
    } else {
        result = {};
        for(i in o) {
            result[i] = deepcopy(o[i]);
        }
        return result
    }
};

var identity = function(x) { return x; };

/////////////////////////////////////////////
// Tokeniser
//
var c = ' ';
var str = '';
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
    str = '';
    return result;
}
var symb = '=!<>&|/*+-%';
var num = '1234567890';
var alphanum = num + '_qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';

next_token = function() {
    while (char_is(' \n\r\t')) {
        skip_char();
    }
    if (c === '\'' || c === '"') {
        var quote = c;
        skip_char();
        while (c !== undefined && c !== quote) {
            if (c === '\\') {
                skip_char();
                if (c === 'n') {
                    c = '\n';
                } else if (c === 't') {
                    c = '\t';
                }
            }
            push_char();
        }
        skip_char();
        return fetch_token('string', pop_string());
    } else if (char_is(num)) {
        while (char_is(num)) {
            push_char();
        }
        return fetch_token('number', pop_string());
    } else if (char_is(alphanum)) {
        while (char_is(alphanum)) {
            push_char();
        }
        return fetch_token(pop_string());

        // read comment or multi character symbol
    } else if (char_is(symb)) {
        if (c === '/') {
            push_char();
            if (c === '/') {
                skip_char();
                pop_string();
                while (c !== undefined && c !== '\n') {
                    push_char();
                }
                return fetch_token('comment', pop_string());
            }
        }
        while (char_is(symb)) {
            push_char();
        }
        return fetch_token(pop_string());
    } else if (c === undefined) {
        return fetch_token('(eof)');
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
    if (obj === undefined || obj.nud === undefined) {
        val = type;
        obj = token_types['identifier'];
    }

    if (val !== undefined) {
        obj = clone(obj);
        obj.val = val;
    }
    return obj;
};

super_token = {
'nud': function() {
        return ['parse-error', 'undefined-nud', this.id, this.val];
    },
'bp':
    0,
'led':
    function(left) {
        return ['parse-error', 'undefined-led', this.id, this.val, left];
    }
};

token_types = {};

var tok = function(name) {
    var result = token_types[name];
    if (result === undefined) {
        result = clone(super_token);
        result.id = name;
        token_types[name] = result;
    }
    return result;
};

///////////////////////////////////////
// Operator constructors
//
fnMacro = function(expr) {
    expr.child.unshift({name: "identifier", val: expr.name, child: []});
    expr.name = 'call';
}

var infix = function(id, bp, fn) {
    tok(id).bp = bp;
    tok(id).led = function(left) {
    return {name: this.id, 'child': [left, parse(this.bp)] }; };
    macros[id] = fn || fnMacro;
};

var infixswap = function(id, newid, bp) {
    tok(id).bp = bp;
    tok(id).led = function(left) { return {name: this.id, 'child': [parse(this.bp), left] }; };
    tok(id).id = newid;
};


var infixr = function(id, bp) {
    tok(id).bp = bp;
    tok(id).led = function(left) { return {name: this.id, 'child': [left, parse(this.bp - 1)] }; };
};

var infixlist = function(id, endsymb, bp, macro) {
    var name = 'apply' + id;
    tok(id).bp = bp;
    tok(id).endsymb = endsymb;
    tok(id).led = function(left) { return {"name": name, 'child': readlist([left], this.endsymb) }; };
    macros[name] = macro;
};

var readlist = function(acc, endsymb) {
    while (token.id !== endsymb && token.id !== '(eof)') {
        var t = parse();
        if (!t.isSeparator) {
            acc.push(t);
        }
    }
    token = next_token();
    return acc;
};

var list = function(id, endsymb, name) {
    tok(id).endsymb = endsymb;
    tok(id).nud = function() { return {name: name, 'child': readlist([], this.endsymb)}; }
};

atom = function(id) {
    tok(id).nud = function() { return {name: "identifier", val: this.id, 'child': [] }; }
};

sep = function(id) {
    tok(id).sep = true;
    tok(id).nud = function() { return {name: '(separator)' , isSeparator: true, 'child': [id] }; }
};

prefix = function(id) {
    tok(id).nud = function() { return {name: this.id, 'child': [parse()] }; }
};

prefix2 = function(id, macro) {
    macro = macro || identity;
    tok(id).nud = function() { return {name: this.id, 'child': [parse(), parse()]}; }
    macros[id] = macro;
};

literal = function(id) {
    tok(id).nud = function() { 
        return {
            name: this.id, 
            'val': this.val, 
            'child': []}; 
    };
};

var opassign = function(id) {
    tok(id).op = id.substring(0, id.length - 1);
    tok(id).bp = 100;
    tok(id).led = function(left) {
        return {
            name: '=', 
            child: [left, applymacros({
                name: this.op, 
                child: [deepcopy(left), parse(this.bp)]})]};
    };
};

//
// Parser
//
macros = { default: identity};

applymacros = function(expr) {
    (macros[expr.name] 
        || macros.default)(expr);
    return expr;
}

parse = function(rbp) {
    rbp = rbp || 0;
    var t = token;
    token = next_token();
    var left = applymacros(t.nud());
    while (rbp < token.bp 
            && !t.sep) {
        t = token;
        token = next_token();
        left = applymacros(t.led(left));
    };
    return left;
};


// return a function, that if the first expression argument is a subscript
// changes the expression to a function call, and otherwise changes the expression type
// to a given name.
function subscriptFunctionOrName(fn, name) {
    return function(expr) {
        if (expr.child[0].name == 'call' 
                && expr.child[0].child[0].name == 'identifier'
                && expr.child[0].child[0].val == '[]') {
            expr.child.unshift(expr.child[0].child[1]);
            expr.child[1] = expr.child[1].child[2];
            expr.name = fn;
            fnMacro(expr);
        } else {
            expr.name = name;
        };
    };
};

//
// Syntax definitions
//
// Definition of operator precedence and type
//
infix('.', 700, function(expr) {
    expr.child[1].name = 'string';
    expr.name = '[]';
    fnMacro(expr);
});

infixlist('(', ')', 600, subscriptFunctionOrName(".()", "call"));

infixlist('[', ']', 600, function(expr) { 
    expr.name = '[]'; 
    fnMacro(expr); 
}); 

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

infixswap('>=', '<=', 300);
infixswap('>', '<', 300);

infixr('&&', 200); 
tok('&&').id = 'and';
infixr('||', 200); 
tok('||').id = 'or';

infix('=', 100, subscriptFunctionOrName("[]=", "set"));

infix('in', 50, identity);

opassign('+=');
opassign('-=');
opassign('*=');
opassign('/=');
opassign('%=');
sep(':');
sep(';');
sep(',');
list('(', ')', 'paren');
list('{', '}', 'object');
macros['object'] = function(expr) {
    var i = 0;
    while(i < expr.child.length) {
        if(expr.child[i].name === 'identifier') {
            expr.child[i].name = 'string';
        }
        i = i + 2;
    };
};
list('[', ']', 'array');
list('var', ';', 'var'); 
prefix('return'); 
prefix('-'); 
prefix('!'); 
macros['!'] = fnMacro;
prefix('throw'); 
macros['throw'] = fnMacro;
atom('undefined'); 
atom('null'); 
atom(']');
atom(')');
atom('}');
atom('(eof)');
tok('null').id = 'undefined';
atom('true'); 
atom('false'); 
literal('identifier');
literal('string');
literal('number');
literal('comment');
infixr('else', 200);

prefix2('while', function(node) {
    // assume node.child[0] === { name:'paren', child:[?]}
    node.child[0] = node.child[0].child[0];
    // assume node.child[1] is curlybraces
    node.child[1].name = 'block';
}); 
prefix2('for', function(node) {
    // assume node.child[0] === { name:'paren', child:[{name:'in', child:[?, ?]}]}
    node.child.unshift(node.child[0].child[0].child[0]);
    node.child[0] = node.child[1].child[0].child[0];
    node.child[1] = node.child[1].child[0].child[1];
    node.child[2].name = 'block';
    node.name = "for-in"
}); 
prefix2('if', function(node) {
    // assume node.child[0] === { name:'paren', child:[?]}
    var node.child[0] = node.child[0].child[0];

    //
    if(node.child[1].name === 'else') {
        node.child[2] = node.child[1].child[1];
        node.child[1] = node.child[1].child[0];
    } else {
        node.child[2] = {name: 'block', child: []};
    }
});

prefix2('function', function(node) {
}); // TODO

prefix2('try'); // TODO
prefix2('catch'); //TODO

//////////////////////////////////////////
// Build parse tree
token = next_token();

//
// dump
//
t = readlist(['list{'], '');
for (elem in t) {
    print('');
    print(uneval(t[elem]));
}
