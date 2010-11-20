////////////////////////////////////////////
// Utility functions
//
var getch = (function() {
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

same = function(a, b) {
    return uneval(a) === uneval(b);
}


/////////////////////////////////////////////
// Tokeniser
//
var c = ' ';
var str = '';
var char_is = function(str) {
    return str.indexOf(c) !== -1;
};
var skip_char = function() {
    c = getch();
};
var push_char = function() {
    str = str + c;
    skip_char();
};
var pop_string = function() {
    var result = str;
    str = '';
    return result;
};
var symb = '=!<>&|/*+-%';
var num = '1234567890';
var alphanum = num + '_qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
var separator = ';,:';

var EOF = ["(eof)"];

var next_token = function() {
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
        return ['string', pop_string()];
    } else if (char_is(num)) {
        while (char_is(num)) {
            push_char();
        }
        return ['number', pop_string()];

    // varname
    } else if (char_is(alphanum)) {
        while (char_is(alphanum)) {
            push_char();
        }
        return [pop_string()];

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
                return ['comment', pop_string()];
            }
        }
        while (char_is(symb)) {
            push_char();
        }
        return [pop_string()];
    } else if (char_is(separator)) {
        push_char();
        return ['(sep)', pop_string()];
    } else if (c === undefined) {
        return EOF;
    } else  {
        push_char();
        return [pop_string()];
    }
};

///////////////////////////////////////
// Operator constructors
//

bp = {};
led = {};
nud = {};

var infix = function(id, prio) {
    bp[id] = prio;
    led[id] = function(left, token) { 
        return [id, left, parse(prio)];
    };
};

var infixr = function(id, prio) {
    bp[id] = prio;
    led[id] = function(left, token) { 
        return [id, left, parse(prio - 1)];
    };
};

var infixlist = function(id, endsymb, prio) {
    bp[id] = prio;
    led[id] = function(left, token) { 
        return readlist(["apply" + id, left], endsymb);
    };
};

var readlist = function(acc, endsymb) {
    while (!same(token, endsymb) && token !== EOF) {
        var t = parse();
        if(t[0] !== "(sep)") {
            acc.push(t);
        }
    }
    token = next_token();
    return acc;
};

var list = function(id, endsymb) {
    nud[id] = function() { return readlist(["list" + id], endsymb); };
};

var passthrough = function(id) {
    nud[id] = function(token) { return token; };
};


var prefix = function(id) {
    nud[id] = function() { return [id, parse()]; };
};

var prefix2 = function(id) {
    nud[id] = function() { return [id, parse(), parse()]}; 
};

//
// Parser
//
var default_nud= function(o) { o.unshift("id"); return o;};

parse = function(rbp) {
    rbp = rbp || 0;
    var t = token;
    token = next_token();
    var left = (nud[t[0]] || default_nud)(t);
    while (rbp < (bp[token[0]] || 0)
            && t[0] !== "(sep)") {
        t = token;
        token = next_token();
        left = led[t[0]](left, t);
    };
    return left;
};

//
// Syntax definitions
//
// Definition of operator precedence and type
//
infix('.', 700);
infixlist('(', [')'], 600);
infixlist('[', [']'], 600);
infix('*', 500); 
infix('%', 500);
infix('/', 500);
infix('+', 400);
infix('-', 400);
infix('===', 300);
infix('==', 300);
infix('!==', 300); 
infix('!=', 300);
infix('<=', 300); 
infix('<', 300);
infix('>=', 300);
infix('>', 300);
infixr('&&', 200); 
infixr('||', 200); 
infix('=', 100);
infix('in', 50);
list('(', [')']);
list('{', ['}']);
list('[', [']']);
list('var', ['(sep)', ';']);
prefix('return'); 
prefix('-'); 
prefix('!'); 
prefix('throw'); 
passthrough('undefined'); 
passthrough('null'); 
passthrough('(sep)');
passthrough(')');
passthrough('}');
passthrough('(eof)');
passthrough('true'); 
passthrough('false'); 
passthrough('identifier');
passthrough('string');
passthrough('number');
passthrough('comment');
infixr('else', 200);
prefix2('while'); 
prefix2('for'); 
prefix2('if');
prefix2('function');
prefix2('try');
prefix2('catch');

// dump
//

token = next_token();
while((x = parse()) !== EOF) {
    print(uneval(x));
}
