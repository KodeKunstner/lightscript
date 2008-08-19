var parser = function(iter) {
	/**
	 * Character reader
	 */
	var c; // current character
	
	var nextc = function() {
		c = iter();
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
	
		if(isnum) {
			isnum = false;
			token = {"id": "literal", "val": parseInt(id), "nud": literal};
		} else if(id === "//") {
			val = id;
			while(id !== undefined && id !== "\n") {
				nextid();
				val = val + id;
			}
			token = {"id": "comment", "val": val, "nud": literal};
		} else if(id === "/*") {
			val = id;
			while(id !== undefined && id !== "*/") {
				nextid();
				val = val + id;
			}
			token = {"id": "comment", "val": val, "nud": literal};
		} else if(id === "\"") {
			val = "";
			nextid();
			while(id !== undefined && id !== "\"") {
				if(id === "\\") {
					nextid();
				}
				val = val + id;
				nextid();
			}
			token = {"id": "literal", "val": val, "nud": literal};
		} else {
			token = copy(parserObject[id] || {"nud" : ident});
			token.id = id;
		}
	};
	
	/** tree building functions */
	var ident = function() { 
		if(this.id === undefined) {
			return undefined;
		}
		return ["ident", this.id]; 
	};
	var literal = function() { 
		return [this.id, this.val]; 
	};
	var infix = function (left) { 
		return [this.id, left, parse(this.lbp)]; 
	};
	var infixr = function (left) { 
		return [this.id, left, parse(this.lbp - 1)] 
	};
	var prefix = function() { 
		return [this.id, parse()]; 
	};
	var prefix2 = function() { 
		return [this.id, parse(), parse()]; 
	};
	
	var list = function() {
		var result = ["list"+this.id];
		var t = parse();
		while(t && t !== this.end) {
			result.push(t);
			t = parse();
		}
		return result;
	};
	
	var apply = function(left) {
		var result = ["apply" + this.id, left];
		var t = parse();
		while(t && t !== this.end) {
			result.push(t);
			t = parse();
		}
		return result;
	};
	
	var if_else = function() {
		var result = [this.id, parse(), parse()];
		if(token.id === "else") {
			nexttoken();
			result.push(parse());
		}
		return result;
	};
	
	var seperator = function() {
		this.seperator = true;
		return this.id;
	};
	
	var parserObject = {
		"return": {"nud" : prefix},
		"var": {"nud" : prefix},
		"delete": {"nud" : prefix},
		"function": {"nud" : prefix2},
		"while": {"nud" : prefix2},
		"if": {"nud" : if_else},
		"+": {"led" : infix, "lbp" : 50},
		".": {"led" : infix, "lbp" : 80},
		"-": {"nud" : prefix, "led" : infix, "lbp" : 50},
		"*": {"led" : infix, "lbp" : 60},
		"===": {"led" : infix, "lbp" : 40},
		"!==": {"led" : infix, "lbp" : 40},
		"<=": {"led" : infix, "lbp" : 40},
		">=": {"led" : infix, "lbp" : 40},
		">": {"led" : infix, "lbp" : 40},
		"<": {"led" : infix, "lbp" : 40},
		"&&": {"led" : infixr, "lbp" : 30},
		"||": {"led" : infixr, "lbp" : 30},
		"=": {"led" : infixr, "lbp" : 10},
		"[": { "nud" : list, "end": "]",  "led" : apply, "lbp" : 80},
		"(": { "nud" : list, "end": ")",  "led" : apply, "lbp" : 80},
		"{": { "nud" : list, "end": "}"},
		"," : { "nud" : seperator, "lbp" : -100},
		":" : { "nud" : seperator, "lbp" : -100},
		";" : { "nud" : seperator, "lbp" : -200},
		")" : { "nud" : seperator, "lbp" : -300},
		"}" : { "nud" : seperator, "lbp" : -300},
		"]" : { "nud" : seperator, "lbp" : -300},
		"(end)" : { "nud" : function() { return undefined;}}
	};
	
	nexttoken();
	
	var parse = function (rbp) {
		var left;
		var t = token;;
	
		rbp = rbp || 0;
	
		t = token;
		nexttoken();
		//print_r(["nud", t, rbp, token]);
		left = t.nud();
	
		while (!t.seperator && rbp < (token.lbp || 0)) {
			t = token;
			nexttoken();
			//print_r(["led", t]);
			left = t.led(left);
		}
		//print_r({"result": left});
		return left;
	}

	return parse;
};
