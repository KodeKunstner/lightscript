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
		var i = 0;
		while(i < symbs.length) {
			if (c === symbs[i]) {
				return true;
			}
			i = i + 1;
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
		} 
	
		if(isnum) {
			isnum = false;
			token = {"id": "literal", "val": parseInt(id), "n": literal};
		} else if(id === "//") {
			val = id;
			while(id !== undefined && id !== "\n") {
				nextid();
				val = val + id;
			}
			token = {"id": "comment", "val": val, "n": literal};
		} else if(id === "/*") {
			val = id;
			while(id !== undefined && id !== "*/") {
				nextid();
				val = val + id;
			}
			token = {"id": "comment", "val": val, "n": literal};
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
			token = {"id": "literal", "val": val, "n": literal};
		} else {
			token = copy(parserObject[id]);
			token.n = token.n || ident;
			token.l = token.l || infix;
			token.p = token.p || 0;
			token.id = id;
		}
	};
	
	/** tree building functions */
	var ident = function() { 
		if(this.id === undefined) {
			return undefined;
		}
	};
	var literal = function() { 
	};
	var infix = function (left) { 
		this.args = [left, parse(this.p)]
	};
	var infixr = function (left) { 
		this.args = [left, parse(this.p - 1)]
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
	
	var apply = function(left) {
		this.id = "apply" + this.id;
		this.args = [left]
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
	
	var sep = function() {
		this.sep = true;
	};
	
	var parserObject = {
		"return": {"n" : prefix},
		"var": {"n" : prefix},
		"delete": {"n" : prefix},
		"function": {"n" : prefix2},
		"while": {"n" : prefix2},
		"if": {"n" : if_else},
		"+": {"l" : infix, "p" : 50},
		".": {"l" : infix, "p" : 80},
		"-": {"n" : prefix, "l" : infix, "p" : 50},
		"*": {"l" : infix, "p" : 60},
		"===": {"l" : infix, "p" : 40},
		"!==": {"l" : infix, "p" : 40},
		"<=": {"l" : infix, "p" : 40},
		">=": {"l" : infix, "p" : 40},
		">": {"l" : infix, "p" : 40},
		"<": {"l" : infix, "p" : 40},
		"&&": {"l" : infixr, "p" : 30},
		"||": {"l" : infixr, "p" : 30},
		"=": {"l" : infixr, "p" : 10},
		"[": { "n" : list, "end": "]",  "l" : apply, "p" : 80},
		"(": { "n" : list, "end": ")",  "l" : apply, "p" : 80},
		"{": { "n" : list, "end": "}"},
		"," : { "n" : sep, "p" : -100},
		":" : { "n" : sep, "p" : -100},
		";" : { "n" : sep, "p" : -200},
		")" : { "n" : sep, "p" : -300},
		"}" : { "n" : sep, "p" : -300},
		"]" : { "n" : sep, "p" : -300},
		"(end)" : {"n" : sep, "p" : -300}
	};
	
	nexttoken();
	
	var parse = function (rbp) {
		var left;
		var t = token;;
	
		rbp = rbp || 0;
	
		t = token;
		nexttoken();
		t.n();
		left = t;
		delete left.p; delete left.n; delete left.l;
	
		while (!t.sep && rbp < token.p) {
			t = token;
			nexttoken();
			t.l(left);
			left = t;
			delete left.p; delete left.n; delete left.l;
		}
		if(left.id === "(end)") {
			return undefined;
		}
		return left;
	}

	return parse;
};

var f = function(x) { if(x > 1) { return f(x - 1) } else {return 1 } }
