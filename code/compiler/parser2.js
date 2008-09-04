load("stdmob2.js");

// functions 
var parse, is_multisymb, is_ws, is_num, is_alphanum, nextc, parse_rbp,
    nexttoken, decl, expect, register_local, pass, prefix, localvarnud, 
    readlist, binop, unop, infixr, clean_prop, error_mesg, printblock, toJS; 
// objects
var parsers, ctx, globals, localvar, token;
// arrays
var ctx_stack;
// string
var line, c, key;
// int 
var line_pos, line_nr, empty_line_count;

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
	token.line = currentline();
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
		token.val = toInt(str);
		str = "(integer literal)";

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
		token.type = "str";
		str = "(string literal)";

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
globals = {"readline": "fun", "print" : "fun", "load": "fun", 
	"print_r": "fun", "typeof": "fun", "getchar": "fun", 
	"currentline": "fun", "push": "fun", "pop": "fun", 
	"toInt": "fun", "toArr": "fun", "toVar": "fun", 
	"toBool": "fun", "toObj": "fun", "toFun": "fun", 
	"join": "fun", "concat": "fun", "int2str": "fun"};

// The parsing functions

expect = function(str) {
	if(str !== token.id) {
		print(join(["Unexpected token: \"", token.id, "\" at line ", token.line], ""));
		print(join(["Expected: \"", str, "\""], ""));
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
		push(args, token.id);
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
			push(arr, t);
		}
		t = parse();
	}
};

binop = function(left) {
	this.args = [left, parse_rbp(this.lbp)];
}

unop = function() {
	this.args = [parse()];
}

infixr = function(left) {
	this.args = [left, parse_rbp(this.lbp - 1)];
}

// The table of parser functions
parsers = {
	"var": {"nud": decl},
	"undefined": {"nud": pass, "type": "nil", "id": "(nil)"},
	"true": {"nud": pass, "type": "bool", "id": "(true)", "val": true},
	"false": {"nud": pass, "type" : "bool", "id": "(false)", "val": false},
	"return": {"nud": prefix, "type": "void", "id": "(return)"},
	"delete": {"nud": prefix, "type": "void", "id": "(delete)"},
	"function": {"id": "(function)", "type":"fun", "nud":
		function() {
			var prev_ctx;
			prev_ctx = ctx;
			push(ctx_stack, ctx);

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
					push(this.parameters, token.id);
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
			ctx = pop(ctx_stack);
		}
	},
	"if": {"id": "(if)", "type": "void", "nud":
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
					push(this.args, {"id":"(block)", "args":t});
				} else {
					push(this.args, parse());
				}
			}

		}
	},
	"for": {"id": "(for)", "type": "void", "nud":
		function() {
			var t;
			t = [];
			expect("(");
			push(t, parse());
			expect("in");
			push(t, parse());
			expect("(end)");
			expect("{");
			this.args = t;
			t = [];
			readlist(t);
			push(this.args, {"id":"(block)", "args":t});
		}
	},
	"while": {"id": "(while)", "type": "void", "nud":
		function() {
			var t;
			this.args = [parse()];
			expect("{");
			t = [];
			readlist(t);
			push(this.args, {"id":"(block)", "args":t});
		}
	},
	"{": {"type": "obj",
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
			this.type = "var";
			expect("(end)");
		},
		"nud": function() {
			this.id =  "(array literal)";
			this.args = [];
			this.type = "arr";
			readlist(this.args);
		}
	},
	"(": {"lbp": 600, 
		"led": function(left) {
			this.type = "var";
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
	"(string literal)": {"nud": pass},
	"(integer literal)": {"nud": pass},
	"=": {"lbp": 100, "type": "void", "led": 
		function(left) {
			if(left.id === "(subscript)") {
				this.id = "(put)";
				this.args = [left.args[0], left.args[1], parse()];
			} else if(left.id === "(global)") {
				this.id = "(setglobal)";
				this.args = [left, parse()];
			} else {
				this.id = "(assign)";
				this.args = [left, parse()];
			}
		}
	},
	".": {"lbp": 700, "led": 
		function(left) {
			this.id = "(subscript)";
			this.args = [left, {"id": "(string literal)", "val": token.id}];
			this.type = "var";
			nexttoken();
		}
	},
	"-": {"type": "int", "lbp": 400,
		"nud": function() {
			this.args = [parse()];
			this.id = "(sign)";
		},
		"led": 
		function(left) {
			this.args = [left, parse_rbp(this.lbp)];
			this.id = "(minus)";
		},
	},
	"+": {"lbp": 400, "type": "int", "led": binop, "id": "(plus)"},
	",": {"nud": pass, "sep": true, "lbp": -100},
	":": {"nud": pass, "sep": true, "lbp": -100},
	"||": {"led": infixr, "id": "(or)", "lbp": 200, "type": "bool"},
	"&&": {"led": infixr, "id": "(and)", "lbp": 200, "type": "bool"},
	"!": {"nud": unop, "id": "(not)", "lbp": 300, "type": "bool"},
	"===": {"led": binop, "lbp": 300, "type": "bool", "id": "(equals)"},
	"!==": {"lbp": 300, "id": "(not equals)", "type": "bool", "led": binop },
	"<": {"led": binop, "lbp": 300, "type": "bool", "id": "(less)"},
	">": {"lbp": 300, "id": "(less)", "type": "bool", "led":
		function(left) {
			this.args = [parse_rbp(this.lbp), left];
		}
	},
	"<=": {"led": binop, "lbp": 300, "type": "bool", "id": "(less or equal)"},
	">=": {"lbp": 300, "id": "(less or equal)", "type": "bool", "led":
		function(left) {
			this.args = [parse_rbp(this.lbp), left];
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

var node2js, tab;

tab = function(i) {
	var str;
	str = "";
	while(i > 0) {
		str = concat(str, "\t");
		i = i - 1;
	}
	return str;
}

printblock = function(arr, indent, acc) {
	var prevcomment;
	prevcomment = false;
	for(i in arr) {
		if(arr[i].id !== "(noop)") {
			push(acc, tab(indent));
			node2js(arr[i], indent, acc);
			push(acc, ";\n");
			prevcomment = false;
		} else if(arr[i].val === "comment") {
			if(prevcomment === false) {
				push(acc, "\n");
				push(acc, "\n");
			}
			//push(acc, tab(indent));
			push(acc, "//");
			push(acc, arr[i].content);
			push(acc, "\n");
			prevcomment = true;

		} else {
			prevcomment = false;
		}
	}

	return acc;
}

node2js = function(elem, indent, acc) {
	var i, t, x;

	if(elem.id === "(string literal)") {
		push(acc, "\"");
		for(i in elem.val) {
			t = {"\n": "\\n", "\"": "\\\"", 
				"\\": "\\\\", "\r": "\\r",
				"\t": "\\t"}[elem.val[i]];
			if(t === undefined) {
				t = elem.val[i];
			}
			push(acc, t);
		}
		push(acc, "\"");
	} else if(elem.id === "(integer literal)") {
		push(acc, "");
		push(acc, int2str(elem.val));
	} else if(elem.id === "(nil)") {
		push(acc, "undefined");
	} else if(elem.id === "(true)") {
		push(acc, "true");
	} else if(elem.id === "(false)") {
		push(acc, "false");
	} else if(elem.id === "(noop)") {
	} else if(elem.id === "(if)") {
		push(acc, "if (");
		node2js(elem.args[0], indent, acc);
		push(acc, ") ");
		node2js(elem.args[1], indent, acc);
		if(3 === elem.args.length) {
			push(acc, " else ");
			node2js(elem.args[2], indent, acc);
		} 
	} else if(elem.id === "(block)") {
		push(acc, "{\n");

		printblock(elem.args, indent+1, acc);

		push(acc, tab(indent));
		push(acc, "}");
	} else if(elem.id === "(object literal)") {
		if(elem.args.length > 0) {
			push(acc, "{\n");
			indent = indent + 1;
			i = 0;
			while(i<elem.args.length) {
				push(acc, tab(indent));
				node2js(elem.args[i], indent, acc);
				push(acc, ": ");
				node2js(elem.args[i + 1], indent, acc);
				push(acc, ",\n");
				i = i + 2;
			}
			indent = indent - 1;
			push(acc, tab(indent));
			push(acc, "}");
		} else {
			push(acc, "{}");
		}
	} else if(elem.id === "(array literal)") {
		if(elem.args.length > 0) {
			t = [];
			push(acc, "[");
			for(i in elem.args) {
				x = [];
				node2js(elem.args[i], indent, x);
				push(t, join(x, ""));
			}
			push(acc, join(t, ", "));
			push(acc, "]");
		} else {
			push(acc, "[]");
		}
	} else if(elem.id === "(for)") {
		push(acc, "for (");
		node2js(elem.args[0], indent, acc);
		push(acc, " in ");
		node2js(elem.args[1], indent, acc);
		push(acc, ") ");
		node2js(elem.args[2], indent, acc);
	} else if(elem.id === "(while)") {
		push(acc, "while (");
		node2js(elem.args[0], indent, acc);
		push(acc, ") ");
		node2js(elem.args[1], indent, acc);
	} else if(elem.id === "(and)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " && ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(or)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " || ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(local)") {
		push(acc, elem.val);
		push(acc, "/*");
		push(acc, elem.type);
		push(acc, "*/");
	} else if(elem.id === "(setglobal)") {
		node2js(elem.args[0], indent, acc);
		push(acc, " = ");
		node2js(elem.args[1], indent, acc);
	} else if(elem.id === "(assign)") {
		node2js(elem.args[0], indent, acc);
		push(acc, " = ");
		node2js(elem.args[1], indent, acc);
	} else if(elem.id === "(subscript)") {
		node2js(elem.args[0], indent, acc);
		push(acc, "[");
		node2js(elem.args[1], indent, acc);
		push(acc, "]");
	} else if(elem.id === "(put)") {
		node2js(elem.args[0], indent, acc);
		push(acc, "[");
		node2js(elem.args[1], indent, acc);
		push(acc, "] = ");
		node2js(elem.args[2], indent, acc);
	} else if(elem.id === "(function)") {
		push(acc, "(function (");
		t = [];
		for(i in elem.parameters) {
			push(t, elem.parameters[i]);
		}
		push(acc, join(t, ", "));
		push(acc, ") {\n");

		t = {};
		for(i in elem.locals) {
			if(t[elem.locals[i]] === undefined) {
				t[elem.locals[i]]  = [];
			}
			push(t[elem.locals[i]], i);
		}
		indent = indent + 1;

		if(t["var"] !== undefined) {
			x = [];
			for(i in t["var"]) {
				push(x, t["var"][i]);
			}
			push(acc, tab(indent));
			push(acc, "var ");
			push(acc, join(x, ", "));
			push(acc, ";\n");
		}

		if(t["shared"] !== undefined) {
			x = [];
			for(i in t["shared"]) {
				push(x, t["shared"][i]);
			}
			push(acc, tab(indent));
			push(acc, "var ");
			push(acc, join(x, ", "));
			push(acc, ";\n");
		}
		printblock(elem.args, indent, acc);

		indent = indent - 1;
		push(acc, tab(indent));
		push(acc, "})");

	} else if(elem.id === "(global)") {
		push(acc, int2str(elem.val));
	} else if(elem.id === "(function call)") {
		t = [];
		node2js(elem.args[0], indent, acc);
		push(acc, "(");
		i = 1;
		while(i<elem.args.length) {
			push(t, join(node2js(elem.args[i], indent, []), ""));
			i = i + 1;
		}
		push(acc, join(t, ", "));
		push(acc, ")");
	} else if(elem.id === "(delete)") {
		push(acc, "delete ");
		node2js(elem.args[0], indent, acc);
	} else if(elem.id === "(return)") {
		push(acc, "return ");
		node2js(elem.args[0], indent, acc);
	} else if(elem.id === "(sign)") {
		push(acc, "(-");
		node2js(elem.args[0], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(plus)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " + ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(minus)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " - ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(equals)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " === ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(not equals)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " !== ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(not)") {
		push(acc, "(!");
		node2js(elem.args[0], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(less)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " < ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if(elem.id === "(less or equal)") {
		push(acc, "(");
		node2js(elem.args[0], indent, acc);
		push(acc, " <= ");
		node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else {
		print(concat("Unknown id: ", elem.id));
		print_r(elem);
	}
	return acc;
}

toJS = function(parser) {
	var node, nodes, acc;
	nodes = [];
	while((node = parser()) !== undefined) {
		push(nodes, node);
	}
	return join(printblock(nodes, 0, []), "")
}

////////////////////////////////
// Test code
////




//var tree, st;
//st = [];
//while(tree = parse()) {
//	print_r(tree);
// push(st, tree);
// }
// print(join(printblock(st, 0, []), ""));
//
print(toJS(parse));
