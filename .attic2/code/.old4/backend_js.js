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
		str = strcat(str, "\t");
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
			arrpush(acc, this.tab(indent));
			this.node2js(arr[i], indent, acc);
			arrpush(acc, ";\n");
			prevcomment = false;
		} else if ((arr[i].val === "comment")) {
			if ((prevcomment === false)) {
				arrpush(acc, "\n");
				arrpush(acc, "\n");
			};
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


// transform an AST-node into javascript
backend_js.node2js = (function (elem, indent, acc) {
	var i, t, x;
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
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, ") ");
		this.node2js(elem.args[1], indent, acc);
		if ((3 === elem.args.length)) {
			arrpush(acc, " else ");
			this.node2js(elem.args[2], indent, acc);
		};
	} else if ((elem.id === "(block)")) {
		arrpush(acc, "{\n");
		this.printblock(elem.args, (indent + 1), acc);
		arrpush(acc, this.tab(indent));
		arrpush(acc, "}");
	} else if ((elem.id === "(object literal)")) {
		if ((0 < elem.args.length)) {
			arrpush(acc, "{\n");
			indent = (indent + 1);
			i = 0;
			while ((i < elem.args.length)) {
				arrpush(acc, this.tab(indent));
				this.node2js(elem.args[i], indent, acc);
				arrpush(acc, ": ");
				this.node2js(elem.args[(i + 1)], indent, acc);
				arrpush(acc, ",\n");
				i = (i + 2);
			};
			indent = (indent - 1);
			arrpush(acc, this.tab(indent));
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
				this.node2js(elem.args[i], indent, x);
				arrpush(t, arrjoin(x, ""));
			};
			arrpush(acc, arrjoin(t, ", "));
			arrpush(acc, "]");
		} else {
			arrpush(acc, "[]");
		};
	} else if ((elem.id === "(for)")) {
		arrpush(acc, "for (");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " in ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ") ");
		this.node2js(elem.args[2], indent, acc);
	} else if ((elem.id === "(while)")) {
		arrpush(acc, "while (");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, ") ");
		this.node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(and)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " && ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(or)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " || ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(local)")) {
		arrpush(acc, elem.val);
	} else if ((elem.id === "(global)")) {
		arrpush(acc, elem.val);
	} else if ((elem.id === "(setglobal)")) {
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " = ");
		this.node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(setlocal)")) {
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " = ");
		this.node2js(elem.args[1], indent, acc);
	} else if ((elem.id === "(subscript)")) {
		this.node2js(elem.args[0], indent, acc);
		if ((elem.args[1].id === "(string literal)")) {
			arrpush(acc, ".");
			arrpush(acc, elem.args[1].val);
		} else {
			arrpush(acc, "[");
			this.node2js(elem.args[1], indent, acc);
			arrpush(acc, "]");
		};
	} else if ((elem.id === "(put)")) {
		this.node2js(elem.args[0], indent, acc);
		if ((elem.args[1].id === "(string literal)")) {
			arrpush(acc, ".");
			arrpush(acc, elem.args[1].val);
		} else {
			arrpush(acc, "[");
			this.node2js(elem.args[1], indent, acc);
			arrpush(acc, "]");
		};
		arrpush(acc, " = ");
		this.node2js(elem.args[2], indent, acc);
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
			arrpush(acc, this.tab(indent));
			arrpush(acc, "var ");
			arrpush(acc, arrjoin(x, ", "));
			arrpush(acc, ";\n");
		};
		if ((t.shared !== undefined)) {
			x = [];
			for (i in t.shared) {
				arrpush(x, t.shared[i]);
			};
			arrpush(acc, this.tab(indent));
			arrpush(acc, "var ");
			arrpush(acc, arrjoin(x, ", "));
			arrpush(acc, ";\n");
		};
		this.printblock(elem.args, indent, acc);
		indent = (indent - 1);
		arrpush(acc, this.tab(indent));
		arrpush(acc, "})");
	} else if ((elem.id === "(function call)")) {
		t = [];
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, "(");
		i = 1;
		while ((i < elem.args.length)) {
			arrpush(t, arrjoin(this.node2js(elem.args[i], indent, []), ""));
			i = (i + 1);
		};
		arrpush(acc, arrjoin(t, ", "));
		arrpush(acc, ")");
	} else if ((elem.id === "(delete)")) {
		arrpush(acc, "delete ");
		this.node2js(elem.args[0], indent, acc);
	} else if ((elem.id === "(return)")) {
		arrpush(acc, "return ");
		this.node2js(elem.args[0], indent, acc);
	} else if ((elem.id === "(sign)")) {
		arrpush(acc, "(-");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(plus)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " + ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(minus)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " - ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(equals)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " === ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(not equals)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " !== ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(not)")) {
		arrpush(acc, "(!");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(less)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " < ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
	} else if ((elem.id === "(less or equal)")) {
		arrpush(acc, "(");
		this.node2js(elem.args[0], indent, acc);
		arrpush(acc, " <= ");
		this.node2js(elem.args[1], indent, acc);
		arrpush(acc, ")");
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


// translate the AST into a string
backend_js.toJS = (function (parser) {
	var node, nodes, acc;
	nodes = [];
	node = parser();
	while ((node !== undefined)) {
		arrpush(nodes, node);
		node = parser();
	};
	return arrjoin(this.printblock(nodes, 0, []), "");
});

//\end{verbatim}
