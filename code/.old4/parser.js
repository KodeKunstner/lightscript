//\begin{verbatim}

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
		this.str = std.io.getchar();
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
	parser.token.line = std.io.currentline();
	str = c.str;


	if ((c.str === undefined)) {
		str = "(end)";


//// Number literal
	} else if (c.is_num()) {
		c.next();
		while (c.is_num()) {
			str = strcat(str, c.str);
			c.next();
		};
		parser.token.val = str;
		str = "(integer literal)";


//// Identifier
	} else if (c.is_alphanum()) {
		c.next();
		while (c.is_alphanum()) {
			str = strcat(str, c.str);
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
			str = strcat(str, c.str);
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
				str = strcat(str, c.str);
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
			str = strcat(str, c.str);
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
			std.io.printerror(arrjoin(["Unexpected token: \"", parser.token.id, "\" at line ", parser.token.line], ""));
			std.io.printerror(arrjoin(["Expected: \"", str, "\""], ""));
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
