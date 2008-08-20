var parser = function(iter) {
	/**
	 * Character reader
	 */
	var c; // current character
	
	var nextc = function() {
		iter.next();
		c = iter.val;
	}
	
	nextc();
	
	/**
	 * Id-reader 
	 */
	var id;
	var isnum;
	
	var oneof = function(symbs) {
		return has_element(symbs, c);
	}
	
	var skipws = function() {
		while(oneof(" \n\r\t")) {
			nextc();
		}
	}
	
	var nextid =  function() {
		var num = "0123456789";
		var ident = "$_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		var oper = "<>/|=+-*&^%!~";
	
		var symbs = "";
		id = c;
	
		if(oneof(num)) {
			symbs = num;
			isnum = true;
		} else if(oneof(ident)) {
			symbs = num + ident;
		} else if(oneof(oper)) {
			symbs = oper;
		}
	
		nextc();
	
		if(id === "/" && oneof("/*")) {
			id = id + c;
			nextc();
		} else while(oneof(symbs)) {
			id = id + c;
			nextc();
		}
	};
	
	/**
	 * Skip white-space, literals and comments;
	 */
	
	// value, being used as is_number flag, and later containing value of literals or comments.
	var token; 
	
	var nexttoken = function() {
		var val;
		var t;
		skipws();
		nextid();

		if(id === undefined) {
			id = "(end)";
		} else if(isnum) {
			isnum = false;
			val = parseInt(id);
			id = "(literal)";
		} else if(id === "//") {
			val = id;
			while(id !== undefined && id !== "\n") {
				nextid();
				val = val + id;
			}
			id = "(comment)";
		} else if(id === "/*") {
			val = id;
			while(id !== undefined && id !== "*/") {
				nextid();
				val = val + id;
			}
			id = "(comment)";
		} else if(id === "\"") {
			val = "";
			nextid();
			while(id !== undefined && id !== "\"") {
				if(id === "\\") {
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
		token.n = token.n || function() {};
		token.l = token.l || infix;
		token.p = token.p || 0;
		token.id = id;
		if(val !== undefined) {
			token.val = val;
		}
	};
	
	var infix = function (prev) { 
		this.args = [prev, parse(this.p)]
	};

	var infixr = function (prev) { 
		this.args = [prev, parse(this.p - 1)]
	};

	var prefix = function() { 
		this.args = [parse()];
	};

	var prefix2 = function() { 
		this.args = [parse(), parse()]; 
	};

	var readlist = function(arr) {
		var t = parse();
		while(!t.rpar) {
			if(!t.sep) {
				arr.push(t);
			}
			t = parse();
		}
	}

	var list = function() {
		this.id = "list" + this.id;
		this.args = [];
		readlist(this.args);
	};
	
	var apply = function(prev) {
		this.id = "apply" + this.id;
		this.args = [prev]
		readlist(this.args);
	};
	
	var if_else = function() {
		this.args = [parse(), parse()];
		if(token.id === "else") {
			nexttoken();
			this.args.push(parse());
		}
	};
	
	var parserObject = {
		"return": {"n" : prefix},
		"var": {"n" : prefix},
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
		"," : { sep: true, "p" : -100},
		":" : { sep: true, "p" : -100},
		";" : { sep: true, "p" : -200},
		")" : { rpar: true, "p" : -300},
		"}" : { rpar: true, "p" : -300},
		"]" : { rpar: true, "p" : -300},
		"(end)" : {rpar: true, "p" : -300}
	};
	
	nexttoken();
	
	var deltmp = function(obj) {
	}

	var parse = function (rbp) {
		var prev;
		var t = token;;
	
		rbp = rbp || 0;
	
		nexttoken();
		t.n();
	
		while (rbp < token.p && !(t.sep || t.rpar)) {
			delete t.p; delete t.n; delete t.l;
			prev = t; t = token; nexttoken();
			t.l(prev);
		}
		delete t.p; delete t.n; delete t.l;
		if(t.id === "(end)") {
			return undefined;
		}
		return t;
	}

	return parse;
};
