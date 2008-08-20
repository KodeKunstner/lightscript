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
		var i = iterator(symbs);
		while(i.next()) {
			if (c === i.val) {
				return true;
			}
		}
		return false;
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

	var list = function() {
		this.id = "list" + this.id;
		this.args = []
		var t = parse();
		while(t && t.id !== this.end) {
			this.args.push(t);
			t = parse();
		}
	};
	
	var apply = function(prev) {
		this.id = "apply" + this.id;
		this.args = [prev]
		var t = parse();
		while(t && t.id !== this.end) {
			this.args.push(t);
			t = parse();
		}
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
		"+": {"p" : 50},
		".": {"p" : 80},
		"-": {"n" : prefix, "l" : infix, "p" : 50},
		"*": {"p" : 60},
		"===": {"p" : 40},
		"!==": {"p" : 40},
		"<=": {"p" : 40},
		">=": {"p" : 40},
		">": {"p" : 40},
		"<": {"p" : 40},
		"&&": {"l" : infixr, "p" : 30},
		"||": {"l" : infixr, "p" : 30},
		"=": {"l" : infixr, "p" : 10},
		"[": { "n" : list, "end": "]",  "l" : apply, "p" : 80},
		"(": { "n" : list, "end": ")",  "l" : apply, "p" : 80},
		"{": { "n" : list, "end": "}"},
		"," : { sep: true, "p" : -100},
		":" : { sep: true, "p" : -100},
		";" : { sep: true, "p" : -200},
		")" : { sep: true, "p" : -300},
		"}" : { sep: true, "p" : -300},
		"]" : { sep: true, "p" : -300},
		"(end)" : {sep: true, "p" : -300}
	};
	
	nexttoken();
	
	var deltmp = function(obj) {
		delete obj.p; 
		delete obj.n; 
		delete obj.l;
		delete obj.sep;
		delete obj.end;
	}
	var parse = function (rbp) {
		var prev;
		var t = token;;
	
		rbp = rbp || 0;
	
		t = token;
		nexttoken();
		t.n();
	
		while (!t.sep && rbp < token.p) {
			deltmp(t);
			prev = t;
			t = token;
			nexttoken();
			t.l(prev);
		}
		deltmp(t);
		if(t.id === "(end)") {
			return undefined;
		}
		return t;
	}

	return parse;
};

var f = function(x) { if(x > 1) { return f(x - 1) } else {return 1 } }
