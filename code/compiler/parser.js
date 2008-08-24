var parser = function (iter) {

   /*global copy, has_element */

	var c, id, isnum, nextc, nextid, nexttoken, oneof, readlist, 
	    skipws, token, apply, if_else, infix, infixr, list, parse, 
	    prefix, prefix2, var_decl, parserObject, passthrough, 
	    identifier;

	nextc = function () {
		iter.next();
		c = iter.val;
	};

	oneof = function (symbs) {
		return has_element(symbs, c);
	};
	
	skipws = function () {
		while (oneof(" \n\r\t")) {
			nextc();
		}
	};
	
	nextid =  function () {
		var num, ident, oper, symbs;
		num = "0123456789";
		ident = "$_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		oper = "<>/|=+-*&^%!~";
	
		isnum = false;
		symbs = "";
		id = c;
	
		if (oneof(num)) {
			symbs = num;
			isnum = true;
		} else if (oneof(ident)) {
			symbs = num + ident;
		} else if (oneof(oper)) {
			symbs = oper;
		}
	
		nextc();
	
		if (id === "/" && oneof("/*")) {
			id = id + c;
			nextc();
		} else {
			while (oneof(symbs)) {
				id = id + c;
				nextc();
			}
		}
	};

	nexttoken = function () {
		var val;
		skipws();
		nextid();

		if (id === undefined) {
			id = "(end)";
		} else if (isnum) {
			val = parseInt(id, 10);
			id = "(literal)";
		} else if (id === "//") {
			val = id;
			while (id !== undefined && id !== "\n") {
				nextid();
				val = val + id;
			}
			id = "(comment)";
		} else if (id === "/*") {
			val = id;
			while (id !== undefined && id !== "*/") {
				nextid();
				val = val + id;
			}
			id = "(comment)";
		} else if (id === "\"") {
			val = "";
			nextid();
			while (id !== undefined && id !== "\"") {
				if (id === "\\") {
					val = val + ({"n": "\n", "r": "\r", "t": "\t"}[c] || c);
					nextc();
				} else {
					val = val + id;
				}
				nextid();
			}
			id = "(literal)";
		} 

		token = copy(parserObject[id]);
		token.n = token.n || identifier;
		token.l = token.l || infix;
		token.p = token.p || 0;
		token.id = token.id || id;
		if (val !== undefined) {
			token.val = val;
		}
	};

	passthrough = function () {};
	
	identifier = function() {
		this.val = this.id;
		this.id = "(identifier)";
	};

	infix = function (prev) { 
		this.args = [prev, parse(this.p)];
	};

	infixr = function (prev) { 
		this.args = [prev, parse(this.p - 1)];
	};

	prefix = function () { 
		this.args = [parse()];
	};

	prefix2 = function () { 
		this.args = [parse(), parse()]; 
	};

	readlist = function (arr) {
		var t;
		t = parse();
		while (!t.rpar) {
			if (!t.sep) {
				arr.push(t);
			}
			t = parse();
		}
	};

	list = function () {
		this.id = "list" + this.id;
		this.args = [];
		readlist(this.args);
	};
	
	apply = function (prev) {
		this.id = "apply" + this.id;
		this.args = [prev];
		readlist(this.args);
	};
	
	if_else = function () {
		this.args = [parse(), parse()];
		if (token.id === "else") {
			nexttoken();
			this.args.push(parse());
		}
	};
	var_decl = function () {
		var t;
		this.args = [parse()];
		t = parse();
		while (t.id === ",") {
			this.args.push(parse());
			t = parse();
		}

	};
	
	parserObject = {
		"undefined": {"n" : passthrough},
		"true": {"n" : passthrough, "val": true, "id": "(literal)"},
		"false": {"n" : passthrough, "val": false, "id": "(literal)"},
		"return": {"n" : prefix},
		"var": {"n" : var_decl},
		"delete": {"n" : prefix},
		"function": {"n" : prefix2},
		"while": {"n" : prefix2},
		"if": {"n" : if_else},
		"+": {"p" : 400},
		".": {"p" : 600},
		"-": {"n" : prefix, "l" : infix, "p" : 400},
		"*": {"p" : 500},
		"===": {"p" : 300},
		"!==": {"p" : 300},
		"<=": {"p" : 300},
		">=": {"p" : 300},
		">": {"p" : 300},
		"<": {"p" : 300},
		"&&": {"l" : infixr, "p" : 200},
		"||": {"l" : infixr, "p" : 200},
		"=": {"l" : infixr, "p" : 100},
		"[": { "n" : list, "l" : apply, "p" : 600},
		"(": { "n" : list, "l" : apply, "p" : 600},
		"{": { "n" : list},
		"," : { "n": passthrough, sep: true, "p" : -100},
		":" : { "n": passthrough, sep: true, "p" : -100},
		";" : { "n": passthrough, sep: true, "p" : -200},
		")" : { "n": passthrough, rpar: true, "p" : -300},
		"}" : { "n": passthrough, rpar: true, "p" : -300},
		"]" : { "n": passthrough, rpar: true, "p" : -300},
		"(literal)" : {"n": passthrough},
		"(comment)" : {"n": passthrough},
		"(end)" : {"n": passthrough, rpar: true, "p" : -300}
	};

	nextc();
	nexttoken();

	parse = function (rbp) {
		var prev, t;

		t = token;
	
		rbp = rbp || 0;
	
		nexttoken();
		t.n();
	
		while (rbp < token.p && !(t.sep || t.rpar)) {
			delete t.p; 
			delete t.n; 
			delete t.l;

			prev = t; 
			t = token; 
			nexttoken();

			t.l(prev);
		}
		delete t.p; 
		delete t.n; 
		delete t.l;

		if (t.id === "(end)") {
			return undefined;
		}
		return t;
	};

	return { next: function() {
			this.val = parse();
			if(this.val === undefined) {
				return false;
			} else {
				return true;
			}
		} }
};
