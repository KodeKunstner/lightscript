
load("stdmob.js");

// functions 
var parse, getchar, is_multisymb, is_ws, is_num, is_alphanum, nextc, parse_rbp,
    nexttoken, decl, expect, register_local, pass, prefix, localvarnud, 
    readlist, binop, unop, logicop, clean_prop, error_mesg, concat, toString; 
// objects
var parsers, ctx, globals, localvar, token;
// arrays
var ctx_stack;
// string
var line, c, key;
// int 
var line_pos, line_nr, empty_line_count;

concat = function(a, b) {
	return a+b;
}

toString = function(o) {
	return concat("", o);
}

//////////////////////////////
// Utility functions for reading a char at a time
////


line = "";
line_pos = -1;
line_nr = 1;
empty_line_count = 0;

getchar = function() {
	line_pos = line_pos + 1;
	if(line[line_pos] !== undefined) {
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
	var key, str, default_token, t;

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
			str = concat(str, c);
			nextc();
		}
		token.val = parseInt(str, 10);
		token.type = "int";
		str = "(literal)";

	// Identifier
	} else if(is_alphanum()) {

		nextc();
		while(is_alphanum()) {
			str = concat(str, c);
			nextc();
		}

	// String literal
	} else if(c === "\"") {

		nextc();
		str = "";
		while(c !== "\"") {
			if(c === "\\") {
				nextc();
				t = {"n": "\n",
					"r": "\r",
					"t": "\t",
					"f": "\f",
					"b": "\b",
				}[c];
				if(t !== undefined) {
					c = t;
				}
			}
			str = concat(str, c);
			nextc();
		}
		nextc();
		token.val = str;
		token.type = "string";
		str = "(literal)";

	// Comment (and division passhtrough)
	} else if(c === "/") {
		nextc();
		if(c === "/") {
			nextc();
			str = "";
			while(c !== "\n") {
				str = concat(str, c);
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
			str = concat(str, c);
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
	print_r(["Error at token", this]);
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
		print(["Unexpected token: \"", token.id, "\" at line ", token.line].join(""));
		print(["Expected: \"", str, "\""].join(""));
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
	this.val = concat("decl-", type);
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
	pos = 0;
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
	this.args = [left, parse_rbp(this.lbp)];
	this.id = "(builtin)";
}

unop = function() {
	this.val = this.id;
	this.args = [parse()];
	this.id = "(builtin)";
}

logicop = function(left) {
	this.val = this.id;
	this.args = [left, parse_rbp(this.lbp - 1)];
	this.id = "(logical)";
}

// The table of parser functions
parsers = {
	"var": {"nud": decl},
	"undefined": {"nud": pass, "type": "null", "id": "(literal)"},
	"true": {"nud": pass, "type": "bool", "id": "(literal)", "val": true},
	"false": {"nud": pass, "type" : "bool", "id": "(literal)", "val": false},
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
			this.parameters = [];
			while(token.id !== "(end)") {
				if(token.id !== ",") {
					register_local(token.id, "param");
					this.parameters.push(token.id);
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
			var t;
			this.args = [parse()];
			expect("{");
			t = [];
			readlist(t);
			this.args.push({"id":"(block)", "args":t});
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
	".": {"lbp": 700, "led": 
		function(left) {
			this.id = "(subscript)";
			this.args = [left, {"type": "string", "id": "(literal)", "val": token.id}];
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
			this.args = [{"id":"(builtin)",  "val": "===", "args":[left, parse_rbp(this.lbp)]}];
		}
	},
	"<": {"led": binop, "lbp": 300},
	">": {"lbp": 300, "id": "(builtin)", "val": "<", "led":
		function(left) {
			this.args = [parse_rbp(this.lbp), left];
		}
	},
	"<=": {"lbp": 300, "id": "(builtin)", "val": "!", "led":
		function(left) {
			this.args = [{"id":"(builtin)",  "val": "<", "args":[parse_rbp(this.lbp), left]}];
		}
	},
	">=": {"lbp": 300, "id": "(builtin)", "val": "!", "led":
		function(left) {
			this.args = [{"id":"(builtin)",  "val": "<", "args":[left, parse_rbp(this.lbp)]}];
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
	//delete obj.line;
}
// The parser itself
parse = function() {
	return parse_rbp(0);
}
parse_rbp = function (rbp) {
	var prev, t;

	t = token;
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

/////////////////////////////
// to js-compiler
////

var toJS, tab;

tab = function(i) {
	var str;
	str = "";
	while(i > 0) {
		str = concat(str, " ");
		i = i - 1;
	}
	return str;
}

var block_toJS;

block_toJS = function(elem, indent, acc) {
	acc.push(" {\n");
	acc.push(tab(indent));
	acc.push("} ");
}

toJS = function(elem, indent, acc) {
	var i, t, x;

	if(elem.id === "(literal)") {
		if(elem.type === "int") {
			acc.push("");
			acc.push(toString(elem.val));
		} else if(elem.type === "null") {
			acc.push("undefined");
		} else if(elem.type === "bool") {
			acc.push(toString(elem.val));
		} else if(elem.type === "string") {
			acc.push("\"");
			for(i in elem.val) {
				t = {"\n": "\\n", "\"": "\\\"", 
					"\\": "\\\\", "\r": "\\r",
					"\t": "\\t"}[elem.val[i]];
				if(t === undefined) {
					t = elem.val[i];
				}
				acc.push(t);
			}
			acc.push("\"");
		} else {
			print(concat("Unknown literal type: ", elem.type));
		}
	} else if(elem.id === "(noop)") {
	} else if(elem.id === "(if)") {
		acc.push("if (");
		toJS(elem.args[0], indent, acc);
		acc.push(") ");
		toJS(elem.args[1], indent, acc);
		if(3 === elem.args.length) {
			acc.push(" else ");
			toJS(elem.args[2], indent, acc);
		} 
	} else if(elem.id === "(block)") {
		acc.push("{\n");
		indent = indent + 4;
		for(i in elem.args) {
			if(elem.args[i].id !== "(noop)") {
				acc.push(tab(indent));
				toJS(elem.args[i], indent, acc);
				acc.push(";\n");
			}
		}
		indent = indent - 4;
		acc.push(tab(indent));
		acc.push("}");
	} else if(elem.id === "(object literal)") {
		if(elem.args.length > 0) {
			acc.push("{\n");
			indent = indent + 4;
			i = 0;
			while(i<elem.args.length) {
				acc.push(tab(indent));
				toJS(elem.args[i], indent, acc);
				acc.push(": ");
				toJS(elem.args[i + 1], indent, acc);
				acc.push(",\n");
				i = i + 2;
			}
			indent = indent - 4;
			acc.push(tab(indent));
			acc.push("}");
		} else {
			acc.push("{}");
		}
	} else if(elem.id === "(array literal)") {
		if(elem.args.length > 0) {
			t = [];
			acc.push("[");
			for(i in elem.args) {
				x = [];
				toJS(elem.args[i], indent, x);
				t.push(x.join(""));
			}
			acc.push(t.join(", "));
			acc.push("]");
		} else {
			acc.push("[]");
		}
	} else if(elem.id === "(for)") {
		acc.push("for (");
		toJS(elem.args[0], indent, acc);
		acc.push(" in ");
		toJS(elem.args[1], indent, acc);
		acc.push(") ");
		toJS(elem.args[2], indent, acc);
	} else if(elem.id === "(while)") {
		acc.push("while (");
		toJS(elem.args[0], indent, acc);
		acc.push(") ");
		toJS(elem.args[1], indent, acc);
	} else if(elem.id === "(logical)") {
		acc.push("(");
		toJS(elem.args[0], indent, acc);
		acc.push(elem.val);
		toJS(elem.args[1], indent, acc);
		acc.push(")");
	} else if(elem.id === "(local)") {
		acc.push(elem.val);
	} else if(elem.id === "(assign)") {
		toJS(elem.args[0], indent, acc);
		acc.push(" = ");
		toJS(elem.args[1], indent, acc);
	} else if(elem.id === "(subscript)") {
		toJS(elem.args[0], indent, acc);
		acc.push("[");
		toJS(elem.args[1], indent, acc);
		acc.push("]");
	} else if(elem.id === "(put)") {
		toJS(elem.args[0], indent, acc);
		acc.push("[");
		toJS(elem.args[1], indent, acc);
		acc.push("] = ");
		toJS(elem.args[2], indent, acc);
	} else if(elem.id === "(function)") {
		acc.push("function(");
		t = [];
		for(i in elem.parameters) {
			t.push(elem.parameters[i]);
		}
		acc.push(t.join(", "));
		acc.push(") {\n");
		indent = indent + 4;

		t = {};
		for(i in elem.locals) {
			if(t[elem.locals[i]] === undefined) {
				t[elem.locals[i]]  = [];
			}
			t[elem.locals[i]].push(i);
		}

		if(t["var"] !== undefined) {
			x = [];
			for(i in t["var"]) {
				x.push(t["var"][i]);
			}
			acc.push(tab(indent));
			acc.push("var ");
			acc.push(x.join(", "));
			acc.push(";\n");
		}

		if(t["shared"] !== undefined) {
			x = [];
			for(i in t["shared"]) {
				x.push(t["shared"][i]);
			}
			acc.push(tab(indent));
			acc.push("var ");
			acc.push(x.join(", "));
			acc.push(";\n");
		}
		for(i in elem.args) {
			acc.push(tab(indent));
			toJS(elem.args[i], indent, acc);
			acc.push(";\n");
		}

		indent = indent - 4;
		acc.push(tab(indent));
		acc.push("}");

	} else if(elem.id === "(global)") {
		acc.push(toString(elem.val));
	} else if(elem.id === "(function call)") {
		t = [];
		toJS(elem.args[0], indent, acc);
		acc.push("(");
		i = 1;
		while(i<elem.args.length) {
			t.push(toJS(elem.args[i], indent, []).join(""));
			i = i + 1;
		}
		acc.push(t.join(", "));
		acc.push(")");
	} else if(elem.id === "(delete)") {
		acc.push("delete ");
		toJS(elem.args[0], indent, acc);
	} else if(elem.id === "(return)") {
		acc.push("return ");
		toJS(elem.args[0], indent, acc);
	} else if(elem.id === "(builtin)") {
		acc.push("(");
		if(elem.val === "===") {
			toJS(elem.args[0], indent, acc);
			acc.push("===");
			toJS(elem.args[1], indent, acc);
		} else if(elem.val === "<") {
			toJS(elem.args[0], indent, acc);
			acc.push("<");
			toJS(elem.args[1], indent, acc);
		} else if(elem.val === "!") {
			acc.push("!");
			toJS(elem.args[0], indent, acc);
		} else if(elem.val === "+") {
			toJS(elem.args[0], indent, acc);
			acc.push("+");
			toJS(elem.args[1], indent, acc);
		} else if(elem.val === "-") {
			if(elem.args.length === 1) {
				acc.push("-");
				toJS(elem.args[0], indent, acc);
			} else {
				toJS(elem.args[0], indent, acc);
				acc.push("-");
				toJS(elem.args[1], indent, acc);
			}
		} else {
			print(concat("Unknown builtin: ", elem.val));
			print_r(elem);
		}
		acc.push(")");
	} else {
		print(concat("Unknown id: ", elem.id));
		print_r(elem);
	}
	return acc;
}

////////////////////////////////
// Test code
////



var tree, st;
while(tree = parse()) {
//	print_r(tree);
	st = toJS(tree, 0, []).join("");
	if(st) {
		print(concat(st, ";"));
	}
}

