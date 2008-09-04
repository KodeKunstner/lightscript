load("stdmob3.js");


/////////////
// Parser //
///////////
parser = {};


//////////////////
// Char reader //
////////////////
parser.char_reader = {
	"str": " ",
	"is_multisymb": (function () {
		var c;
		c = this.str;
		return ((c === "|") || ((c === "<") || ((c === "=") || ((c === ">") || ((c === "!") || (c === "&"))))));
	}),
	"is_ws": (function () {
		var c;
		c = this.str;
		return ((c === " ") || ((c === "\n") || ((c === "\r") || (c === "\t"))));
	}),
	"is_num": (function () {
		var c;
		c = this.str;
		return (("0" <= c) && (c <= "9"));
	}),
	"is_alphanum": (function () {
		var c;
		c = this.str;
		return ((("a" <= c) && (c <= "z")) || ((("A" <= c) && (c <= "Z")) || (this.is_num() || (c === "_"))));
	}),
	"next": (function () {
		this.str = std.io.getchar();
	}),
};


///////////////////
// Token reader //
/////////////////
parser.nexttoken = (function () {
	var key, str, default_token, t, c, error_mesg;
	c = parser.char_reader;


// Skip whitespaces
	while (c.is_ws()) {
		c.next();
	};


// Initialisation;
	parser.token = {};
	parser.token.line = std.io.currentline();
	str = c.str;


// Create token
// End-token
	if ((c.str === undefined)) {
		str = "(end)";


// Number literal
	} else if (c.is_num()) {
		c.next();
		while (c.is_num()) {
			str = strcat(str, c.str);
			c.next();
		};
		parser.token.val = str;
		str = "(integer literal)";


// Identifier
	} else if (c.is_alphanum()) {
		c.next();
		while (c.is_alphanum()) {
			str = strcat(str, c.str);
			c.next();
		};


// String literal
	} else if ((c.str === "\"")) {
		c.next();
		str = "";
		while ((c.str !== "\"")) {
			if ((c.str === "\\")) {
				c.next();
				t = {
					"n": "\n",
					"r": "\r",
					"t": "\t",
				}[c.str];
				if ((t !== undefined)) {
					c.str = t;
				};
			};
			str = strcat(str, c.str);
			c.next();
		};
		c.next();
		parser.token.val = str;
		parser.token.type = "str";
		str = "(string literal)";


// Comment (and division passhtrough)
	} else if ((c.str === "/")) {
		c.next();
		if ((c.str === "/")) {
			c.next();
			str = "";
			while ((c.str !== "\n")) {
				str = strcat(str, c.str);
				c.next();
			};
			parser.token.content = str;
			parser.token.val = "comment";
			str = "(noop)";
		};


// Symbol consisting of several chars
	} else if (c.is_multisymb()) {
		c.next();
		while (c.is_multisymb()) {
			str = strcat(str, c.str);
			c.next();
		};


// Single symbol
	} else {
		c.next();
	};


// Add properties to token
	parser.token.id = str;
	parser.token.lbp = 0;
	error_mesg = (function () {
		std.io.printerror(["Error at token", this]);
	});
	parser.token.nud = error_mesg;
	parser.token.led = error_mesg;
	default_token = parser.handlers[str];
	if ((default_token === undefined)) {
		default_token = {
			"nud": (function () {
				this.val = this.id;
				this.id = "(global)";
			}),
		};
	};
	for (key in default_token) {
		parser.token[key] = default_token[key];
	};
});


//////////////////////
// Parsing context //
////////////////////
parser.ctx_stack = [];
parser.ctx = {
	"locals": {},
	"prev": {},
};


////////////////////////////
// The parsing functions //
//////////////////////////
parser.handlers = (function () {
	var expect, register_local, decl, pass, prefix, localvarnud, localvar, readlist, binop, unop, infixr;
	expect = (function (str) {
		if ((str !== parser.token.id)) {
			std.io.printerror(arrjoin(["Unexpected token: \"", parser.token.id, "\" at line ", parser.token.line], ""));
			std.io.printerror(arrjoin(["Expected: \"", str, "\""], ""));
		};
		parser.nexttoken();
	});
	register_local = (function (name, type) {
		parser.ctx.locals[name] = type;
		parser.ctx.prev[name] = parser.handlers[name];
		parser.handlers[name] = localvar;
		parser.handlers[name].type = type;
	});
	decl = (function () {
		var type, args;
		type = this.id;
		args = [parser.token.id];
		register_local(parser.token.id, type);
		parser.nexttoken();
		while ((parser.token.val !== ";")) {
			expect(",");
			arrpush(args, parser.token.id);
			register_local(parser.token.id, type);
			parser.nexttoken();
		};
		this.id = "(noop)";
		this.val = strcat("decl-", type);
		this.elems = args;
	});
	pass = (function () {
	});
	prefix = (function () {
		this.args = [parser.parse()];
	});
	localvarnud = (function () {
		this.type = parser.ctx.locals[this.id];
		this.val = this.id;
		this.id = "(local)";
	});
	localvar = {
		"nud": localvarnud,
	};
	readlist = (function (arr) {
		var t;
		t = parser.parse();
		while (t) {
			if ((!t.sep)) {
				arrpush(arr, t);
			};
			t = parser.parse();
		};
	});
	binop = (function (left) {
		this.args = [left, parser.parse_rbp(this.lbp)];
	});
	unop = (function () {
		this.args = [parser.parse()];
	});
	infixr = (function (left) {
		this.args = [left, parser.parse_rbp((this.lbp - 1))];
	});
	return {
		"var": {
			"nud": decl,
		},
		"undefined": {
			"nud": pass,
			"type": "nil",
			"id": "(nil)",
		},
		"globals": {
			"nud": pass,
			"id": "(globals)",
		},
		"strcat": {
			"nud": pass,
			"id": "(string concat)",
		},
		"arrjoin": {
			"nud": pass,
			"id": "(array join)",
		},
		"arrpop": {
			"nud": pass,
			"id": "(array pop)",
		},
		"arrpush": {
			"nud": pass,
			"id": "(array push)",
		},
		"this": {
			"nud": pass,
			"id": "(this)",
		},
		"true": {
			"nud": pass,
			"type": "bool",
			"id": "(true)",
			"val": true,
		},
		"false": {
			"nud": pass,
			"type": "bool",
			"id": "(false)",
			"val": false,
		},
		"return": {
			"nud": prefix,
			"type": "void",
			"id": "(return)",
		},
		"delete": {
			"nud": prefix,
			"type": "void",
			"id": "(delete)",
		},
		"function": {
			"id": "(function)",
			"nud": (function () {
				var key, t, assign;
				assign = undefined;
				arrpush(parser.ctx_stack, parser.ctx);
				parser.ctx = {
					"locals": {},
					"prev": {},
				};
				if ((parser.token.id !== "(")) {
					assign = parser.token.id;
					parser.nexttoken();
				};
				expect("(");
				this.parameters = [];
				while ((parser.token.id !== "(end)")) {
					if ((parser.token.id !== ",")) {
						register_local(parser.token.id, "param");
						arrpush(this.parameters, parser.token.id);
					};
					parser.nexttoken();
				};
				parser.nexttoken();
				this.args = [];
				expect("{");
				readlist(this.args);
				this.locals = parser.ctx.locals;
				for (key in parser.ctx.prev) {
					parser.handlers[key] = parser.ctx.prev[key];
				};
				parser.ctx = arrpop(parser.ctx_stack);
				if ((assign !== undefined)) {
					this.args = [{
						"id": "(global)",
						"val": assign,
					}, {
						"id": this.id,
						"locals": this.locals,
						"nud": this.nud,
						"args": this.args,
						"parameters": this.parameters,
					}];
					this.id = "(setglobal)";
					delete this.locals;
					delete this.parameters;
				};
			}),
		},
		"if": {
			"id": "(if)",
			"type": "void",
			"nud": (function () {
				var t;
				t = [];
				this.args = [parser.parse(), {
					"id": "(block)",
					"args": t,
				}];
				expect("{");
				readlist(t);
				if ((parser.token.id === "else")) {
					t = [];
					parser.nexttoken();
					if ((parser.token.id === "{")) {
						parser.nexttoken();
						readlist(t);
						arrpush(this.args, {
							"id": "(block)",
							"args": t,
						});
					} else {
						arrpush(this.args, parser.parse());
					};
				};
			}),
		},
		"for": {
			"id": "(for)",
			"type": "void",
			"nud": (function () {
				var t, t2;
				t = [];
				expect("(");
				arrpush(t, parser.parse());
				t2 = parser.parse();
				if ((t2.val !== "in")) {
					std.io.printerror(strcat("\"for\" missing \"in\", in line ", this.line));
					std.io.printerror(t2);
				};
				arrpush(t, parser.parse());
				expect("(end)");
				expect("{");
				this.args = t;
				t = [];
				readlist(t);
				arrpush(this.args, {
					"id": "(block)",
					"args": t,
				});
			}),
		},
		"while": {
			"id": "(while)",
			"type": "void",
			"nud": (function () {
				var t;
				this.args = [parser.parse()];
				expect("{");
				t = [];
				readlist(t);
				arrpush(this.args, {
					"id": "(block)",
					"args": t,
				});
			}),
		},
		"{": {
			"type": "obj",
			"nud": (function () {
				this.id = "(object literal)";
				this.args = [];
				readlist(this.args);
			}),
		},
		"[": {
			"lbp": 600,
			"led": (function (left) {
				this.id = "(subscript)";
				this.args = [left, parser.parse()];
				this.type = "var";
				expect("(end)");
			}),
			"nud": (function () {
				this.id = "(array literal)";
				this.args = [];
				this.type = "arr";
				readlist(this.args);
			}),
		},
		"(": {
			"lbp": 600,
			"led": (function (left) {
				this.type = "var";
				this.id = "(function call)";
				this.args = [left];
				readlist(this.args);
			}),
			"nud": (function () {
				var t;
				t = parser.parse();
				for (key in t) {
					this[key] = t[key];
				};
				expect("(end)");
			}),
		},
		")": {
			"nud": pass,
			"id": "(end)",
			"val": ")",
			"lbp": (-300),
			"sep": true,
		},
		"}": {
			"nud": pass,
			"id": "(end)",
			"val": "}",
			"lbp": (-300),
			"sep": true,
		},
		"]": {
			"nud": pass,
			"id": "(end)",
			"val": "]",
			"lbp": (-300),
			"sep": true,
		},
		";": {
			"nud": pass,
			"id": "(noop)",
			"val": ";",
			"lbp": (-200),
			"sep": true,
		},
		"(noop)": {
			"nud": pass,
		},
		"(end)": {
			"nud": pass,
		},
		"(string literal)": {
			"nud": pass,
		},
		"(integer literal)": {
			"nud": pass,
		},
		"=": {
			"lbp": 100,
			"type": "void",
			"led": (function (left) {
				if ((left.id === "(subscript)")) {
					this.id = "(put)";
					this.args = [left.args[0], left.args[1], parser.parse()];
				} else if ((left.id === "(global)")) {
					this.id = "(setglobal)";
					this.args = [left, parser.parse()];
				} else if ((left.id === "(local)")) {
					this.id = "(setlocal)";
					this.args = [left, parser.parse()];
				} else {
					std.io.printerror(strcat("Error, wrong lval for assignment in line ", this.line));
				};
			}),
		},
		".": {
			"lbp": 700,
			"led": (function (left) {
				this.id = "(subscript)";
				this.args = [left, {
					"id": "(string literal)",
					"val": parser.token.id,
				}];
				this.type = "var";
				parser.nexttoken();
			}),
		},
		"-": {
			"type": "int",
			"lbp": 400,
			"nud": (function () {
				this.args = [parser.parse()];
				this.id = "(sign)";
			}),
			"led": (function (left) {
				this.args = [left, parser.parse_rbp(this.lbp)];
				this.id = "(minus)";
			}),
		},
		"+": {
			"lbp": 400,
			"type": "int",
			"led": binop,
			"id": "(plus)",
		},
		",": {
			"nud": pass,
			"sep": true,
			"lbp": (-100),
		},
		":": {
			"nud": pass,
			"sep": true,
			"lbp": (-100),
		},
		"||": {
			"led": infixr,
			"id": "(or)",
			"lbp": 200,
			"type": "bool",
		},
		"&&": {
			"led": infixr,
			"id": "(and)",
			"lbp": 200,
			"type": "bool",
		},
		"!": {
			"nud": unop,
			"id": "(not)",
			"lbp": 300,
			"type": "bool",
		},
		"===": {
			"led": binop,
			"lbp": 300,
			"type": "bool",
			"id": "(equals)",
		},
		"!==": {
			"lbp": 300,
			"id": "(not equals)",
			"type": "bool",
			"led": binop,
		},
		"<": {
			"led": binop,
			"lbp": 300,
			"type": "bool",
			"id": "(less)",
		},
		">": {
			"lbp": 300,
			"id": "(less)",
			"type": "bool",
			"led": (function (left) {
				this.args = [parser.parse_rbp(this.lbp), left];
			}),
		},
		"<=": {
			"led": binop,
			"lbp": 300,
			"type": "bool",
			"id": "(less or equal)",
		},
		">=": {
			"lbp": 300,
			"id": "(less or equal)",
			"type": "bool",
			"led": (function (left) {
				this.args = [parser.parse_rbp(this.lbp), left];
			}),
		},
	};
})();


////////////////////////
// The parser itself //
//////////////////////
parser.parse = (function () {
	return parser.parse_rbp(0);
});
parser.parse_rbp = (function (rbp) {
	var prev, t, clean_prop;
	clean_prop = (function (obj) {
		delete obj.nud;
		delete obj.lbp;
		delete obj.led;
	});
	t = parser.token;
	parser.nexttoken();
	t.nud();
	while (((rbp < parser.token.lbp) && (!t.sep))) {
		clean_prop(t);
		prev = t;
		t = parser.token;
		parser.nexttoken();
		t.led(prev);
	};
	clean_prop(t);
	if ((t.id === "(end)")) {
		return undefined;
	};
	return t;
});


//////////////////////////////
// initialisation
////
parser.nexttoken();


/////////////////////////////
// to js-compiler
////
tab = (function (i) {
	var str;
	str = "";
	while ((0 < i)) {
		str = strcat(str, "\t");
		i = (i - 1);
	};
	return str;
});
printblock = (function (arr, indent, acc) {
	var prevcomment;
	prevcomment = false;


//std.io.printerror(strcat("/", "*")); println("ARR:", arr); println(strcat("*", "/"));
	for (i in arr) {
		if ((arr[i].id !== "(noop)")) {
			arrpush(acc, tab(indent));
			node2js(arr[i], indent, acc);
			arrpush(acc, ";\n");
			prevcomment = false;
		} else if ((arr[i].val === "comment")) {
			if ((prevcomment === false)) {
				arrpush(acc, "\n");
				arrpush(acc, "\n");
			};


//arrpush(acc, tab(indent));
			arrpush(acc, "//");
			arrpush(acc, arr[i].content);
			arrpush(acc, "\n");
			prevcomment = true;
		} else {
			prevcomment = false;
		};
	};
	return acc;
});
node2js = (function (elem, indent, acc) {
	var i, t, x;


//std.io.printerror(strcat("/", "*")); println("ELEM:", elem); println(strcat("*", "/"));
	if ((elem.id === "(string literal)")) {
		arrpush(acc, "\"");
		for (i in elem.val) {
			t = {
				"\n": "\\n",
				"\"": "\\\"",
				"\\": "\\\\",
				"\r": "\\r",
				"\t": "\\t",
			}[elem.val[i]];
			if ((t === undefined)) {
				t = elem.val[i];
			};
			arrpush(acc, t);
		};
		arrpush(acc, "\"");
	} else if ((elem.id === "(integer literal)")) {
		arrpush(acc, "");
		arrpush(acc, elem.val);
	} else if ((elem.id === "(nil)")) {
		arrpush(acc, "undefined");
	} else if ((elem.id === "(true)")) {
		arrpush(acc, "true");
	} else if ((elem.id === "(false)")) {
		arrpush(acc, "false");
	} else if ((elem.id === "(noop)")) {
	} else if ((elem.id === "(if)")) {
		arrpush(acc, "if (");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, ") ");
		node2js(elem.args[1], indent, acc);
		if ((3 === elem.args.length)) {
			arrpush(acc, " else ");
			node2js(elem.args[2], indent, acc);
		};
	} else if ((elem.id === "(block)")) {
		arrpush(acc, "{\n");
		printblock(elem.args, (indent + 1), acc);
		arrpush(acc, tab(indent));
		arrpush(acc, "}");
	} else if ((elem.id === "(object literal)")) {
		if ((0 < elem.args.length)) {
			arrpush(acc, "{\n");
			indent = (indent + 1);
			i = 0;
			while ((i < elem.args.length)) {
				arrpush(acc, tab(indent));
				node2js(elem.args[i], indent, acc);
				arrpush(acc, ": ");
				node2js(elem.args[(i + 1)], indent, acc);
				arrpush(acc, ",\n");
				i = (i + 2);
			};
			indent = (indent - 1);
			arrpush(acc, tab(indent));
			arrpush(acc, "}");
		} else {
			arrpush(acc, "{}");
		};
	} else if ((elem.id === "(array literal)")) {
		if ((0 < elem.args.length)) {
			t = [];
			arrpush(acc, "[");
			for (i in elem.args) {
				x = [];
				node2js(elem.args[i], indent, x);
				arrpush(t, arrjoin(x, ""));
			};
			arrpush(acc, arrjoin(t, ", "));
			arrpush(acc, "]");
		} else {
			arrpush(acc, "[]");
		};
	} else if ((elem.id === "(for)")) {
		arrpush(acc, "for (");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " in ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ") ");
		node2js(elem.args[2], indent, acc);
	} else if ((elem.id === "(while)")) {
		arrpush(acc, "while (");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, ") ");
		node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(and)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " && ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(or)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " || ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(local)")) {
		arrpush(acc, elem.val);
	} else if ((elem.id === "(global)")) {
		arrpush(acc, elem.val);
	} else if ((elem.id === "(setglobal)")) {
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " = ");
		node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(setlocal)")) {
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " = ");
		node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(subscript)")) {
		node2js(elem.args[0], indent, acc);
		if ((elem.args[1].id === "(string literal)")) {
			arrpush(acc, ".");
			arrpush(acc, elem.args[1].val);
		} else {
			arrpush(acc, "[");
			node2js(elem.args[1], indent, acc);
			arrpush(acc, "]");
		};
	} else if ((elem.id === "(put)")) {
		node2js(elem.args[0], indent, acc);
		if ((elem.args[1].id === "(string literal)")) {
			arrpush(acc, ".");
			arrpush(acc, elem.args[1].val);
		} else {
			arrpush(acc, "[");
			node2js(elem.args[1], indent, acc);
			arrpush(acc, "]");
		};
		arrpush(acc, " = ");
		node2js(elem.args[2], indent, acc);
	} else if ((elem.id === "(function)")) {
		arrpush(acc, "(function (");
		t = [];
		for (i in elem.parameters) {
			arrpush(t, elem.parameters[i]);
		};
		arrpush(acc, arrjoin(t, ", "));
		arrpush(acc, ") {\n");
		t = {};
		for (i in elem.locals) {
			if ((t[elem.locals[i]] === undefined)) {
				t[elem.locals[i]] = [];
			};
			arrpush(t[elem.locals[i]], i);
		};
		indent = (indent + 1);
		if ((t.var !== undefined)) {
			x = [];
			for (i in t.var) {
				arrpush(x, t.var[i]);
			};
			arrpush(acc, tab(indent));
			arrpush(acc, "var ");
			arrpush(acc, arrjoin(x, ", "));
			arrpush(acc, ";\n");
		};
		if ((t.shared !== undefined)) {
			x = [];
			for (i in t.shared) {
				arrpush(x, t.shared[i]);
			};
			arrpush(acc, tab(indent));
			arrpush(acc, "var ");
			arrpush(acc, arrjoin(x, ", "));
			arrpush(acc, ";\n");
		};
		printblock(elem.args, indent, acc);
		indent = (indent - 1);
		arrpush(acc, tab(indent));
		arrpush(acc, "})");
	} else if ((elem.id === "(function call)")) {
		t = [];
		node2js(elem.args[0], indent, acc);
		arrpush(acc, "(");
		i = 1;
		while ((i < elem.args.length)) {
			arrpush(t, arrjoin(node2js(elem.args[i], indent, []), ""));
			i = (i + 1);
		};
		arrpush(acc, arrjoin(t, ", "));
		arrpush(acc, ")");
	} else if ((elem.id === "(delete)")) {
		arrpush(acc, "delete ");
		node2js(elem.args[0], indent, acc);
	} else if ((elem.id === "(return)")) {
		arrpush(acc, "return ");
		node2js(elem.args[0], indent, acc);
	} else if ((elem.id === "(sign)")) {
		arrpush(acc, "(-");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(plus)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " + ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(minus)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " - ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(equals)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " === ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(not equals)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " !== ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(not)")) {
		arrpush(acc, "(!");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(less)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " < ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(less or equal)")) {
		arrpush(acc, "(");
		node2js(elem.args[0], indent, acc);
		arrpush(acc, " <= ");
		node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(globals)")) {
		std.io.printerror(strcat("The globals variable is not supported yet in the JS-backend"));
	} else if ((elem.id === "(string concat)")) {
		arrpush(acc, "strcat");
	} else if ((elem.id === "(array join)")) {
		arrpush(acc, "arrjoin");
	} else if ((elem.id === "(array push)")) {
		arrpush(acc, "arrpush");
	} else if ((elem.id === "(array pop)")) {
		arrpush(acc, "arrpop");
	} else if ((elem.id === "(this)")) {
		arrpush(acc, "this");
	} else {
		std.io.printerror(strcat("Unknown id: ", elem.id));
		std.io.printerror(elem);
	};
	return acc;
});
toJS = (function (parser) {
	var node, nodes, acc;
	nodes = [];
	node = parser();
	while ((node !== undefined)) {
		arrpush(nodes, node);
		node = parser();
	};
	return arrjoin(printblock(nodes, 0, []), "");
});


////////////////////////////////
// Test code
////
std.io.println(toJS(parser.parse));

