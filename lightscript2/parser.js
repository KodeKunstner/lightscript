load('stdmob.js');
var char_is = function(str) {
    return f(x) !== -1;
//    return str.indexOf(c) !== -1;
};

////////////////////////
// fetch_tokeniser
////
//
// String constants
var symb = '=!<>&|/*+-%';
var num = '1234567890';
var alpha = '_qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM';
var alphanum = alpha + num;
var whitespace = ' \n\r\t';

// current character
var c = ' ';

// resulting string
var str = "";

// functions to access current character and string
var char_is = function(str) {
    return str.indexOf(c) !== -1;
};
var skip_char = function() {
    c = getch();
}
var push_char = function() {
    str += c;
    skip_char();
}
var get_string = function() {
    var result = str;
    str = "";
    return result;
}

next_token = function() {
        // Skip whitespaces
	while(char_is(whitespace)) {
            skip_char();
	}

	// String
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
                return fetch_token("(string)", get_string());

	// Read number [0-9]*
	} else if(char_is(num)) {
		while(char_is(num)) {
                        push_char();
		}
                return fetch_token("(num)", get_string());

	// read identifier [_a-zA-Z][_a-zA-Z0-9]*
	} else if(char_is(alphanum)) {
		while(char_is(alphanum)) {
                        push_char();
		}
                return fetch_token(get_string());

        // read comment
	// read multi character symbol
	} else if(char_is(symb)) {
                if(c === '/') {
                    push_char();
                    if(c === '/') {
                        skip_char();

                        // clear the string
                        get_string();

                        // read the rest of the line
                        while(c !== undefined && c !== '\n') {
                            push_char();
                        }
                        return fetch_token("(comment)", get_string());
                    }
                }
		while(char_is(symb)) {
                        push_char();
		}
                return fetch_token(get_string());
	
        // end of stream
	} else  if(c === undefined) {
            return fetch_token("(eof)");

	// single symbol
	} else  {
            push_char();
            return fetch_token(get_string());
	}
};


super_token = {
    "nud": function() { 
        return ["parse-error", "undefined-nud", this.id, this.val]; 
    },
    "bp": 0,
    "led": function(left) { 
        return ["parse-error", "undefined-led", this.id, this.val, left]; 
    }
}

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
}

// functions for defining operator precedence and type
infix = function(id, bp) {
	tok(id).bp = bp;
	tok(id).led = function(left) {
		return [this.id, left, parse(this.bp)];
	};
};

infixr = function(id, bp) {
	tok(id).bp = bp;
	tok(id).led = function(left) {
		return [this.id, left, parse(this.bp - 1)];
	};
};

infixlist = function(id, bp) {
	tok(id).bp = bp;
	tok(id).led = function(left) {
		return readlist(['apply' + this.id, left]);
	};
};


readlist = function(acc) {
        while(!token.end) {
            acc.push(parse());
	}
        token = next_token();
	return acc;
};

end = function(id) {
        tok(id).end = true;
	tok(id).nud = function() {
		return ['(end)', this.id];
	}
};

list = function(id) {
        tok(id).nud = function() {
		return readlist(['list' + this.id]);
	}
};

atom = function(id) {
	tok(id).nud = function() {
		return [this.id];
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

//
// Definition of operator precedence and type
//
infix('.', 700);
infixlist('(', 600);
infixlist('[', 600);
infix('*', 500);
infix('%', 500);
infix('+', 400);
infix('-', 400);
infix('===', 300);
infix('!==', 300);
infix('<=', 300);
infix('<', 300);
infixr('&&', 200);
infixr('||', 200);
infixr('else', 200);
infix('=', 100);
end(']');
end(')');
end('}');
end('(eof)');
atom(':');
atom(';');
atom(',');
list('(');
list('{');
list('[');
prefix('var');
prefix('return');
prefix('-');
prefix('!');
prefix2('function');
prefix2('if');
prefix2('while');
atom('undefined');
atom('true');
atom('false');
literal('(identifier)');
literal('(string)');
literal('(num)');
literal('(comment)');

// The core parser
token = next_token();
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

//
// dump
//


x = parse();
while(x[0] !== "(end)") {
    println(x);
    x = parse();
}
