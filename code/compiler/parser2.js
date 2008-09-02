
load("stdmob.js");


// functions 
var parse, getchar, is_multisymb, is_ws, is_num, is_alphanum, nextc, 
    nexttoken, decl, expect, register_local, pass, prefix, localvarnud, 
    readlist, binop, unop, logicop, clean_prop, error_mesg; 
// objects
var parsers, ctx, globals, localvar, token;
// arrays
var ctx_stack;
// string
var line, c, key;
// int 
var line_pos, line_nr, empty_line_count;

//////////////////////////////
// Utility functions for reading a char at a time
////


line = "";
line_pos = -1;
line_nr = 1;
empty_line_count = 0;

getchar = function() {
	line_pos = line_pos + 1;
	if(line[line_pos]) {
		empty_line_count = 0;
		return line[line_pos];
	} else {
		line_nr = line_nr + 1;
		line_pos = -1;
		line = readline();
		empty_line_count = empty_line_count + 1;
		if(empty_line_count > 10) {
			return undefined;
		} else {
			return "\n";
		} 
	}
}

////////////////////////////////
// Tokeniser
////

c = " "; 

//////
//Predicate functions
//////

is_multisymb = function() {
	return c === "|" || c === "<" || c === "=" || c === ">"
		|| c === "!" || c === "&";
};

is_ws = function() {
	return c === " " || c === "\n" || c === "\r" || c === "\t";
};

is_num = function() { 
	return c >= "0" && c <= "9"; 
};

is_alphanum = function() { 
	return ("a" <= c && c <= "z")
	    || ("A" <= c && c <= "Z")
	    || is_num() || c === "_";
};

nextc = function() {
	c = getchar();
}


nexttoken = function() {
	var key, str, default_token;

	// Skip whitespaces
	while(is_ws()) {
		nextc();
	};

	// Initialisation;
	token = {};
	token.line = line_nr;
	str = c;

	// Create token
	
	// End-token
	if(c === undefined) {
		str = "(end)";

	// Number literal
	} else if(is_num()) {

		nextc();
		while(is_num()) {
			str = str + c;
			nextc();
		}
		token.val = parseInt(str, 10);
		str = "(literal)";

	// Identifier
	} else if(is_alphanum()) {

		nextc();
		while(is_alphanum()) {
			str = str + c;
			nextc();
		}

	// String literal
	} else if(c === "\"") {

		nextc();
		str = "";
		while(c !== "\"") {
			if(c === "\\") {
				nextc();
				c = {"n": "\n",
					"r": "\r",
					"t": "\t",
					"f": "\f",
					"b": "\b",
					"l": "\l",
				}[c] || c;
			}
			str = str + c;
			nextc();
		}
		nextc();
		token.val = str;
		str = "(literal)";

	// Comment (and division passhtrough)
	} else if(c === "/") {
		nextc();
		if(c === "/") {
			nextc();
			str = "";
			while(c !== "\n") {
				str = str + c;
				nextc();
			}
			token.content = str;
			token.val = "comment";
			str = "(noop)";
		} 

	// Symbol consisting of several chars
	} else if(is_multisymb()) {
		nextc();
		while(is_multisymb()) {
			str = str + c;
			nextc();
		}
	// Single symbol
	} else {
		nextc();
	}

	// Add properties to token
	token.id = str;
	token.lbp = 0;
	token.nud = error_mesg;
	token.led = error_mesg;

	default_token = parsers[str];
	for(key in default_token) {
		token[key] = default_token[key];
	}
};

error_mesg = function() {
	print_r(["Er" + "ror at token", this]);
}

/////////////////////////////////
// The parser
////


ctx_stack = [];
ctx = {"locals": {}, };
globals = {"readline": "fun", "print" : "fun", "load": "fun", "parseInt": "fun", "print_r": "fun", "typeof": "fun"};

// The parsing functions

expect = function(str) {
	if(str !== token.id) {
		print("Unexpected token: \"" + token.id + "\" at line " + token.line);
		print("Expected: \"" + str + "\"");
	} 
	nexttoken();
}

register_local = function(name, type) {
	ctx.locals[name] = type;
	parsers[name] = localvar;
	parsers[name].type = type;
}

decl = function() {
	var type, args;
	type = this.id;

	args = [token.id];
	register_local(token.id, type);
	nexttoken();

	while(token.val !== ";") {
		expect(",");
		args.push(token.id);
		register_local(token.id, type);
		nexttoken();
	}

	this.id = "(noop)";
	this.val = "decl-" + type;
	this.elems = args;
};

pass = function () { };

prefix = function() {
	this.args = [parse()];
};

localvarnud = function() {
	var pos, i, t;
	this.type = ctx.locals[this.id];
	this.val = this.id;
	this.id = "(local)";
	pos = 57;
	if(this.type === -1) {
		for(i in ctx_stack) {
			t = ctx_stack[i].locals[this.val];
			if(t !== undefined && typeof(t) !== "number") {
				pos = i;
			}
		}
		ctx_stack[pos].locals[this.val] = "shared";
		pos = ctx_stack.length - pos;
		this.type = pos;
		ctx.locals[this.val] = pos;
	}
}

localvar = {
	"nud": localvarnud
};

readlist = function(arr) {
	var t;
	t = parse();
	while(t) {
		if(!t.sep) {
			arr.push(t);
		}
		t = parse();
	}
};

binop = function(left) {
	this.val = this.id;
	this.args = [left, parse(this.lbp)];
	this.id = "(builtin)";
}

unop = function() {
	this.val = this.id;
	this.args = [parse()];
	this.id = "(builtin)";
}

logicop = function(left) {
	this.val = this.id;
	this.args = [left, parse(this.lbp - 1)];
	this.id = "(logical)";
}

// The table of parser functions
parsers = {
	"var": {"nud": decl},
	"undefined": {"nud": pass, "id": "(literal)"},
	"true": {"nud": pass, "id": "(literal)", "val": true},
	"false": {"nud": pass, "id": "(literal)", "val": false},
	"return": {"nud": prefix, "id": "(return)"},
	"delete": {"nud": prefix, "id": "(delete)"},
	"function": {"id": "(function)", "nud":
		function() {
			var prev_ctx;
			prev_ctx = ctx;
			ctx_stack.push(ctx);

			ctx = {"locals": {}, };
			for(key in prev_ctx.locals) {
				register_local(key, -1);
			}

			register_local("this", "this");
			expect("(");
			this.parameters = {};
			while(token.id !== "(end)") {
				if(token.id !== ",") {
					register_local(token.id, "param");
					this.parameters[token.id] = "param";
				}
				nexttoken();
			}
			nexttoken();

			this.args = [];
			expect("{");
			readlist(this.args);

			for(key in ctx.locals) {
				if(ctx.locals[key] === -1) {
					delete ctx.locals[key];
				}
			}
			this.locals = ctx.locals;
			ctx = ctx_stack.pop();
		}
	},
	"if": {"id": "(if)", "nud":
		function() {
			var t;
			t = [];
			this.args = [parse(), {"id":"(block)", "args": t}];
			expect("{");
			readlist(t);
			if(token.id === "else") {
				t = [];
				nexttoken();

				if(token.id === "{") {
					nexttoken();
					readlist(t);
					this.args.push({"id":"(block)", "args":t});
				} else {
					this.args.push(parse());
				}
			}

		}
	},
	"for": {"id": "(for)", "nud":
		function() {
			var t;
			t = [];
			expect("(");
			t.push(parse());
			expect("in");
			t.push(parse());
			expect("(end)");
			expect("{");
			this.args = t;
			t = [];
			readlist(t);
			this.args.push({"id":"(block)", "args":t});
		}
	},
	"while": {"id": "(while)", "nud":
		function() {
			this.args = [parse()];
			expect("{");
			readlist(this.args);
		}
	},
	"{": {
		"nud": function() {
			this.id =  "(object literal)";
			this.args = [];
			readlist(this.args);
		}
	},
	"[": {"lbp": 600,
		"led": function(left) {
			this.id = "(subscript)";
			this.args = [left, parse()];
			expect("(end)");
		},
		"nud": function() {
			this.id =  "(array literal)";
			this.args = [];
			readlist(this.args);
		}
	},
	"(": {"lbp": 600, 
		"led": function(left) {
			// TODO: var analysis
			this.id = "(function call)";
			this.args = [left];
			readlist(this.args);
		}, 
		"nud": function() {
			var t;
			t = parse();
			for(key in t) {
				this[key] = t[key];
			}
			expect("(end)");
		}
	},
	")": {"nud": pass, "id": "(end)", "val": ")", "lbp": -300, "sep": true},
	"}": {"nud": pass, "id": "(end)", "val": "}", "lbp": -300, "sep": true},
	"]": {"nud": pass, "id": "(end)", "val": "]", "lbp": -300, "sep": true},
	";": {"nud": pass, "id": "(noop)", "val": ";", "lbp": -200, "sep": true},
	"(noop)": {"nud": pass},
	"(end)": {"nud": pass},
	"(literal)": {"nud": pass},
	"=": {"lbp": 100, "led": 
		function(left) {
			if(left.id === "(subscript)") {
				this.id = "(put)";
				this.args = [left.args[0], left.args[1], parse()];
			} else {
				this.id = "(assign)";
				this.args = [left, parse()];
			}
		}
	},
	".": {"lbp": 600, "led": 
		function(left) {
			this.id = "(subscript)";
			this.args = [left, {"id": "(literal)", "val": token.id}];
			nexttoken();
		}
	},
	"-": {"nud": unop, "led": binop, "lbp": 400},
	"+": {"led": binop, "lbp": 400},
	",": {"nud": pass, "sep": true, "lbp": -100},
	":": {"nud": pass, "sep": true, "lbp": -100},
	"||": {"led": logicop, "lbp": 200},
	"&&": {"led": logicop, "lbp": 200},
	"!": {"nud": unop, "lbp": 300},
	"===": {"led": binop, "lbp": 300},
	"!==": {"lbp": 300, "id": "(builtin)", "val": "!", "led":
		function(left) {
			this.args = [{"id":"(builtin)",  "val": "===", "args":[left, parse()]}];
		}
	},
	"<": {"led": binop, "lbp": 300},
	">": {"lbp": 300, "id": "(builtin)", "val": "<", "led":
		function(left) {
			this.args = [parse(), left];
		}
	},
	"<=": {"lbp": 300, "id": "(builtin)", "val": "!", "led":
		function(left) {
			this.args = [{"id":"(builtin)",  "val": "<", "args":[parse(), left]}];
		}
	},
	">=": {"lbp": 300, "id": "(builtin)", "val": "!", "led":
		function(left) {
			this.args = [{"id":"(builtin)",  "val": "<", "args":[left, parse()]}];
		}
	},
};

for(key in globals) {
	parsers[key] = { "nud": pass, "id": "(global)", "val": key};
}

clean_prop = function(obj) {
	delete obj.nud;
	delete obj.lbp;
	delete obj.led;
	delete obj.type;
	delete obj.line;
}
// The parser itself
parse = function (rbp) {
	var prev, t;

	t = token;
	rbp = rbp || 0;
	nexttoken();
	t.nud();

	while (rbp < token.lbp && !t.sep) {
		clean_prop(t);
		prev = t;
		t = token;
		nexttoken();
		t.led(prev);
	}
	clean_prop(t);

	if (t.id === "(end)") {
		return undefined;
	}
	return t;
};


//////////////////////////////
// initialisation
////

nexttoken();

////////////////////////////////
// Test code
////


var tree;
while(tree = parse()) {
	print_r(tree);
	print();
}

print_r(ctx.locals);

var test;

test = function() {
	var foo, bar, baz;
	foo = function(baz) {
		var quux;
		return function() {
			return foo+bar+baz+test;
		}
	}
}
