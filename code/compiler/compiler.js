

//\begin{verbatim}
load("stdmob.js");


//\end{verbatim}
//\subsection{Parser}
//\begin{verbatim}
parser = {};


//\end{verbatim}
//\subsubsection{Character reader}
//\begin{verbatim}
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
		this.str = getchar();
	}),
};


//\end{verbatim}
//\subsubsection{Token reader}
//\begin{verbatim}
parser.nexttoken = (function () {
	var key, str, default_token, t, c, error_mesg;
	c = parser.char_reader;
	while (c.is_ws()) {
		c.next();
	};
	parser.token = {};
	parser.token.line = current_line();
	str = c.str;
	if ((c.str === undefined)) {
		str = "(end)";


//// Number literal
	} else if (c.is_num()) {
		c.next();
		while (c.is_num()) {
			str = join([str, c.str], "");
			c.next();
		};
		parser.token.val = str;
		str = "(integer literal)";


//// Identifier
	} else if (c.is_alphanum()) {
		c.next();
		while (c.is_alphanum()) {
			str = join([str, c.str], "");
			c.next();
		};


//// String literal
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
			str = join([str, c.str], "");
			c.next();
		};
		c.next();
		parser.token.val = str;
		str = "(string literal)";


//// Comment (and division passhtrough)
	} else if ((c.str === "/")) {
		c.next();
		if ((c.str === "/")) {
			c.next();
			str = "";
			while ((c.str !== "\n")) {
				str = join([str, c.str], "");
				c.next();
			};
			parser.token.content = str;
			parser.token.val = "comment";
			str = "(noop)";
		};


//// Symbol consisting of several chars
	} else if (c.is_multisymb()) {
		c.next();
		while (c.is_multisymb()) {
			str = join([str, c.str], "");
			c.next();
		};


//// Single symbol
	} else {
		c.next();
	};


//// Add properties to token
	parser.token.id = str;
	parser.token.lbp = 0;
	error_mesg = (function () {
		println(["Error at token", this]);
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


//\end{verbatim}
//\subsubsection{Parsing functions}
// First we have a context for the parser, used for...
//\begin{verbatim}
parser.ctx_stack = [];
parser.ctx = {
	"locals": {},
	"prev": {},
};


//\end{verbatim}
// Then there are the definitions of the parsing functions...
//\begin{verbatim}
parser.handlers = (function () {
	var expect, register_local, decl, pass, prefix, localvarnud, localvar, readlist, binop, unop, infixr;
	expect = (function (str) {
		if ((str !== parser.token.id)) {
			println(join(["Unexpected token: \"", parser.token.id, "\" at line ", parser.token.line], ""));
			println(join(["Expected: \"", str, "\""], ""));
		};
		parser.nexttoken();
	});
	register_local = (function (name, type) {
		parser.ctx.locals[name] = type;
		parser.ctx.prev[name] = parser.handlers[name];
		parser.handlers[name] = localvar;
	});
	decl = (function () {
		var type, args;
		type = this.id;
		args = [parser.token.id];
		register_local(parser.token.id, type);
		parser.nexttoken();
		while ((parser.token.val !== ";")) {
			expect(",");
			push(args, parser.token.id);
			register_local(parser.token.id, type);
			parser.nexttoken();
		};
		this.id = "(noop)";
		this.val = join(["decl-", type], "");
		this.elems = args;
	});
	pass = (function () {
	});
	prefix = (function () {
		this.args = [parser.parse()];
	});
	localvarnud = (function () {
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
				push(arr, t);
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
			"id": "(nil)",
		},
		"this": {
			"nud": pass,
			"id": "(this)",
		},
		"true": {
			"nud": pass,
			"id": "(true)",
			"val": true,
		},
		"false": {
			"nud": pass,
			"id": "(false)",
			"val": false,
		},
		"return": {
			"nud": prefix,
			"id": "(return)",
		},
		"delete": {
			"nud": prefix,
			"id": "(delete)",
		},
		"function": {
			"id": "(function)",
			"nud": (function () {
				var key, t, assign;
				assign = undefined;
				push(parser.ctx_stack, parser.ctx);
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
						push(this.parameters, parser.token.id);
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
				parser.ctx = pop(parser.ctx_stack);
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
						push(this.args, {
							"id": "(block)",
							"args": t,
						});
					} else {
						push(this.args, parser.parse());
					};
				};
			}),
		},
		"for": {
			"id": "(for)",
			"nud": (function () {
				var t, t2;
				t = [];
				expect("(");
				push(t, parser.parse());
				t2 = parser.parse();
				if ((t2.val !== "in")) {
					println(join(["\"for\" missing \"in\", in line ", this.line]), "");
					println(t2);
				};
				push(t, parser.parse());
				expect("(end)");
				expect("{");
				this.args = t;
				t = [];
				readlist(t);
				push(this.args, {
					"id": "(block)",
					"args": t,
				});
			}),
		},
		"while": {
			"id": "(while)",
			"nud": (function () {
				var t;
				this.args = [parser.parse()];
				expect("{");
				t = [];
				readlist(t);
				push(this.args, {
					"id": "(block)",
					"args": t,
				});
			}),
		},
		"{": {
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
				expect("(end)");
			}),
			"nud": (function () {
				this.id = "(array literal)";
				this.args = [];
				readlist(this.args);
			}),
		},
		"(": {
			"lbp": 600,
			"led": (function (left) {
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
					println(join(["Error, wrong lval for assignment in line ", this.line]), "");
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
				parser.nexttoken();
			}),
		},
		"-": {
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
		},
		"&&": {
			"led": infixr,
			"id": "(and)",
			"lbp": 200,
		},
		"!": {
			"nud": unop,
			"id": "(not)",
			"lbp": 300,
		},
		"===": {
			"led": binop,
			"lbp": 300,
			"id": "(equals)",
		},
		"!==": {
			"lbp": 300,
			"id": "(not equals)",
			"led": binop,
		},
		"<": {
			"led": binop,
			"lbp": 300,
			"id": "(less)",
		},
		">": {
			"lbp": 300,
			"id": "(less)",
			"led": (function (left) {
				this.args = [parser.parse_rbp(this.lbp), left];
			}),
		},
		"<=": {
			"led": binop,
			"lbp": 300,
			"id": "(less or equal)",
		},
		">=": {
			"lbp": 300,
			"id": "(less or equal)",
			"led": (function (left) {
				this.args = [parser.parse_rbp(this.lbp), left];
			}),
		},
	};
})();


//\end{verbatim}
//\subsubsection{The core parser}
//\begin{verbatim}
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


//\end{verbatim}
//\subsubsection{Initialisation}
//\begin{verbatim}
parser.nexttoken();


//\end{verbatim}
//\begin{verbatim}
/////////////////////////////
// to js-compiler
////
backend_js = {};


// indent
backend_js.tab = (function (i) {
	var str;
	str = "";
	while ((0 < i)) {
		str = join([str, "\t"], "");
		i = (i - 1);
	};
	return str;
});


// print an indented sequence of statements
backend_js.printblock = (function (arr, indent, acc) {
	var prevcomment;
	prevcomment = false;
	for (i in arr) {
		if ((arr[i].id !== "(noop)")) {
			push(acc, this.tab(indent));
			this.node2js(arr[i], indent, acc);
			push(acc, ";\n");
			prevcomment = false;
		} else if ((arr[i].val === "comment")) {
			if ((prevcomment === false)) {
				push(acc, "\n");
				push(acc, "\n");
			};
			push(acc, "//");
			push(acc, arr[i].content);
			push(acc, "\n");
			prevcomment = true;
		} else {
			prevcomment = false;
		};
	};
	return acc;
});


// transform an AST-node into javascript
backend_js.node2js = (function (elem, indent, acc) {
	var i, t, x;
	if ((elem.id === "(string literal)")) {
		push(acc, "\"");
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
			push(acc, t);
		};
		push(acc, "\"");
	} else if ((elem.id === "(integer literal)")) {
		push(acc, "");
		push(acc, elem.val);
	} else if ((elem.id === "(nil)")) {
		push(acc, "undefined");
	} else if ((elem.id === "(true)")) {
		push(acc, "true");
	} else if ((elem.id === "(false)")) {
		push(acc, "false");
	} else if ((elem.id === "(noop)")) {
	} else if ((elem.id === "(if)")) {
		push(acc, "if (");
		this.node2js(elem.args[0], indent, acc);
		push(acc, ") ");
		this.node2js(elem.args[1], indent, acc);
		if ((3 === elem.args.length)) {
			push(acc, " else ");
			this.node2js(elem.args[2], indent, acc);
		};
	} else if ((elem.id === "(block)")) {
		push(acc, "{\n");
		this.printblock(elem.args, (indent + 1), acc);
		push(acc, this.tab(indent));
		push(acc, "}");
	} else if ((elem.id === "(object literal)")) {
		if ((0 < elem.args.length)) {
			push(acc, "{\n");
			indent = (indent + 1);
			i = 0;
			while ((i < elem.args.length)) {
				push(acc, this.tab(indent));
				this.node2js(elem.args[i], indent, acc);
				push(acc, ": ");
				this.node2js(elem.args[(i + 1)], indent, acc);
				push(acc, ",\n");
				i = (i + 2);
			};
			indent = (indent - 1);
			push(acc, this.tab(indent));
			push(acc, "}");
		} else {
			push(acc, "{}");
		};
	} else if ((elem.id === "(array literal)")) {
		if ((0 < elem.args.length)) {
			t = [];
			push(acc, "[");
			for (i in elem.args) {
				x = [];
				this.node2js(elem.args[i], indent, x);
				push(t, join(x, ""));
			};
			push(acc, join(t, ", "));
			push(acc, "]");
		} else {
			push(acc, "[]");
		};
	} else if ((elem.id === "(for)")) {
		push(acc, "for (");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " in ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ") ");
		this.node2js(elem.args[2], indent, acc);
	} else if ((elem.id === "(while)")) {
		push(acc, "while (");
		this.node2js(elem.args[0], indent, acc);
		push(acc, ") ");
		this.node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(and)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " && ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(or)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " || ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(local)")) {
		push(acc, elem.val);
	} else if ((elem.id === "(global)")) {
		push(acc, elem.val);
	} else if ((elem.id === "(setglobal)")) {
		this.node2js(elem.args[0], indent, acc);
		push(acc, " = ");
		this.node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(setlocal)")) {
		this.node2js(elem.args[0], indent, acc);
		push(acc, " = ");
		this.node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(subscript)")) {
		this.node2js(elem.args[0], indent, acc);
		if ((elem.args[1].id === "(string literal)")) {
			push(acc, ".");
			push(acc, elem.args[1].val);
		} else {
			push(acc, "[");
			this.node2js(elem.args[1], indent, acc);
			push(acc, "]");
		};
	} else if ((elem.id === "(put)")) {
		this.node2js(elem.args[0], indent, acc);
		if ((elem.args[1].id === "(string literal)")) {
			push(acc, ".");
			push(acc, elem.args[1].val);
		} else {
			push(acc, "[");
			this.node2js(elem.args[1], indent, acc);
			push(acc, "]");
		};
		push(acc, " = ");
		this.node2js(elem.args[2], indent, acc);
	} else if ((elem.id === "(function)")) {
		push(acc, "(function (");
		t = [];
		for (i in elem.parameters) {
			push(t, elem.parameters[i]);
		};
		push(acc, join(t, ", "));
		push(acc, ") {\n");
		t = {};
		for (i in elem.locals) {
			if ((t[elem.locals[i]] === undefined)) {
				t[elem.locals[i]] = [];
			};
			push(t[elem.locals[i]], i);
		};
		indent = (indent + 1);
		if ((t.var !== undefined)) {
			x = [];
			for (i in t.var) {
				push(x, t.var[i]);
			};
			push(acc, this.tab(indent));
			push(acc, "var ");
			push(acc, join(x, ", "));
			push(acc, ";\n");
		};
		if ((t.shared !== undefined)) {
			x = [];
			for (i in t.shared) {
				push(x, t.shared[i]);
			};
			push(acc, this.tab(indent));
			push(acc, "var ");
			push(acc, join(x, ", "));
			push(acc, ";\n");
		};
		this.printblock(elem.args, indent, acc);
		indent = (indent - 1);
		push(acc, this.tab(indent));
		push(acc, "})");
	} else if ((elem.id === "(function call)")) {
		t = [];
		this.node2js(elem.args[0], indent, acc);
		push(acc, "(");
		i = 1;
		while ((i < elem.args.length)) {
			push(t, join(this.node2js(elem.args[i], indent, []), ""));
			i = (i + 1);
		};
		push(acc, join(t, ", "));
		push(acc, ")");
	} else if ((elem.id === "(delete)")) {
		push(acc, "delete ");
		this.node2js(elem.args[0], indent, acc);
	} else if ((elem.id === "(return)")) {
		push(acc, "return ");
		this.node2js(elem.args[0], indent, acc);
	} else if ((elem.id === "(sign)")) {
		push(acc, "(-");
		this.node2js(elem.args[0], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(plus)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " + ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(minus)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " - ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(equals)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " === ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(not equals)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " !== ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(not)")) {
		push(acc, "(!");
		this.node2js(elem.args[0], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(less)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " < ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(less or equal)")) {
		push(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		push(acc, " <= ");
		this.node2js(elem.args[1], indent, acc);
		push(acc, ")");
	} else if ((elem.id === "(array join)")) {
		push(acc, "join");
	} else if ((elem.id === "(array push)")) {
		push(acc, "push");
	} else if ((elem.id === "(array pop)")) {
		push(acc, "pop");
	} else if ((elem.id === "(this)")) {
		push(acc, "this");
	} else {
		println(strcat("Unknown id: ", elem.id));
		println(elem);
	};
	return acc;
});


// translate the AST into a string
backend_js.toJS = (function (parser) {
	var node, nodes, acc;
	nodes = [];
	node = parser();
	while ((node !== undefined)) {
		push(nodes, node);
		node = parser();
	};
	return join(this.printblock(nodes, 0, []), "");
});


//\end{verbatim}
println(backend_js.toJS(parser.parse));

