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
    } else if (c === undefined) {
        return EOF;
    } else  {
        push_char();
        return [pop_string()];
    }
};

//////////////////////////////////////
// Pretty printer
//

var pp = {};
var indentinc = 4;
var indentstr = function(n) {
    var i = 0;
    var result = "";
    while(i < n) {
        result = result + " ";
        i = i + 1;
    }
    return result;
};
var tailstr = function(node, indent, str) {
    node = node.slice(1);
    return node.map(function(node) {
        return prettyprint(node, indent);
    }).join(str);
};
var infixstr = function(node, indent) {
        return prettyprint(node[1], indent) + " " + node[0] + " " + prettyprint(node[2], indent);
}
var prefixstr = function(node, indent) {
        return node[0] + " " + tailstr(node, indent, " ");
}

var prettyprint = function(node, indent) {
    indent = indent || 0;
    fn = pp[node[0]];
    if(fn) {
        return fn(node, indent);
    } else {
        return "" + node[0];
    }
};

///////////////////////////////////////
// Operator constructors
//
var bp = {};
var led = {};
var nud = {};

// utility functions
var readlist = function(acc, endsymb) {
    while (token[0] !== endsymb && token !== EOF) {
        var t = parse();
        if(!is_separator(t[0])) {
            acc.push(t);
        }
    }
    token = next_token();
    return acc;
};

// syntax constructors
var infix = function(id, prio) {
    bp[id] = prio;
    led[id] = function(left, token) { 
        return [id, left, parse(prio)];
    };
    pp[id] = pp[id] || infixstr;
};

var infixr = function(id, prio) {
    bp[id] = prio;
    led[id] = function(left, token) { 
        return [id, left, parse(prio - 1)];
    };
    pp[id] = pp[id] || infixstr;
};

var infixlist = function(id, endsymb, prio) {
    bp[id] = prio;
    led[id] = function(left, token) { 
        return readlist(["apply" + id, left], endsymb);
    };
    pp["apply" + id] = pp[id] || function(node, indent) {
        return prettyprint(node[1], indent) + id + tailstr(node.slice(1), indent, ", ") + endsymb;
    };
};

var list = function(id, endsymb) {
    nud[id] = function() { return readlist(["list" + id], endsymb); };
    pp["list" + id] = pp[id] || function(node, indent) {
        return id + tailstr(node, indent, ", ") + endsymb;
    }
};

var passthrough = function(id) {
    nud[id] = function(token) { return token; };
    pp[id] = pp[id] || function(node, indent) {
        return node[node.length - 1];
    }
};


var prefix = function(id) {
    nud[id] = function() { return [id, parse()]; };
    pp[id] = pp[id] || prefixstr;
};

var prefix2 = function(id) {
    nud[id] = function() { return [id, parse(), parse()]}; 
    pp[id] = pp[id] || prefixstr;
};

/////////////////////////////////////////
// Parser
//
var default_nud= function(o) { o.unshift("id"); return o;};

var parse = function(rbp) {
    rbp = rbp || 0;
    var t = token;
    token = next_token();
    var left = (nud[t[0]] || default_nud)(t);
    while (rbp < (bp[token[0]] || 0)
            && !is_separator(t[0])) {
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
var is_separator = function(c) {
    return ";,:".indexOf(c) !== -1;
}
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
infix('!==', 300); 
infix('!=', 300);
infix('<=', 300); 
infix('<', 300);
infix('>=', 300);
infix('>', 300);
infixr('&&', 200); 
infixr('||', 200); 
infixr('else', 200);
infix('=', 100);
infix('in', 50);
list('(', ')');
list('{', '}');
list('[', ']');
['var', 'return', '-', '!', 'throw'].map(prefix);
['while', 'for', 'if', 'function', 'try', 'catch'].map(prefix2);
['undefined', 'null', ';', ':', ',', ')', '}', '(eof)', 'false', 'true', 'id', 'string', 'number', 'comment'].map(passthrough);

// pretty printing
pp["string"] = function(node) { 
    return '"' + node[1].replace("\\", "\\\\").replace("\n", "\\n").replace('\t', '\\t').replace('"', '\\"') + '"'; 
};
pp["comment"] = function(node) { 
    return "//" + node[1]; 
};
pp["-"] = function(node, indent) {
    if(node.length === 2) {
        return "-" + prettyprint(node[1], indent);
    } else {
        pp["+"](node, indent);
    }
};

//
// dump
//

token = next_token();
t = readlist(['list{'], '');
for (elem in t) {
    print(prettyprint(t[elem]));
}
