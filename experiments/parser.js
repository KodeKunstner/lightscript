// require standard mobyscript library
load("stdmob.js");

var tokeniser = function(iter) {
	var num = "0123456789";
	var ident = "$_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	var oper = "<>/|=+-*&^%!~";

	var current = iter();

	var next = function() {
		current = iter();
	}

	/* return whether ch is contained in the symbol set */
	var oneof = function(symbs) {
		symbiter = iterator(symbs);
		while((symb = symbiter()) !== undefined) {
			if (current === symb) {
				return true;
			}
		}
		return false;
	}

	var nextToken =  function() {

		var token = {}
		var tokensymbs = "";
		var str = current;

		if(current === undefined) {
			return undefined;
		}

		if(oneof(num)) {
			tokensymbs = num;
			token.type = "num";
		}

		if(oneof(ident)) {
			tokensymbs = num + ident;
		}

		if(oneof(oper)) {
			tokensymbs = oper;
		}

		// perhaps comment-catcher here
		next();

		while(oneof(tokensymbs)) {
			str += current;
			next();
		}
		token.id = str;
		return token;
	}

	return nextToken;
}

var cleanup = function(iter) {
	return function() {
		var token;
		var str = "";
		var type;
		var i = 0;

		token = iter() || {"id":"(end)"};

		// Comments
		if(token.id === "//") {
			token = iter();
			while(token !== undefined && token.id !== "\n") {
				str = str + token.id ;
				token = iter();
			}
			token.val = str;
			token.id = "(comment)";

		} else if(token.id === "/*" || token.id === "/**") {
			token = iter();
			while(token !== undefined && token.id !== "*/") {
				str = str + token.id;
				token = iter();
			}
			token.val = str;
			token.id = "(comment)";

		// String
		} else if(token.id === "\"") {
			token = iter();
			while(token !== undefined && token.id !== "\"") {
				if(token.id === "\\") {
					token = iter();
				}
				str += token.id;
				token = iter();
			}
			token.val = str;
			token.id = "(literal)";

		} else if(token.type === "num") {
			delete token.type;
			token.val = parseInt(token.id);
			token.id = "(literal)"
		} 
		return token;
	}
}

///////////////////////////////////
// Create an iterator for testing
////

iter = tokeniser(getch);
iter = cleanup(iter);

// remove comments
iter = filter(function(elem) { return elem.id === "(comment)"; },iter);

// remove whitespaces
iter = filter(function(elem) { return elem.id === " " || elem.id === "\n" || elem.id === "\t" || elem.id === "\r"; },iter);

/////////////////////////////
// Beginning of parser
//
	var infix = function (left) {
		var result = [this.id, left]
		result.push(parse(this.lbp));
		return result;
	}

	var dotsub = function (left) {
		assert(token.id === "(literal)" 
				&& typeof(token.val) === "string");
		var result = ["(subscript)", left, ["(string)", token.val]];
		return result;
	}

	var simplenud = function() {
		return this.id;
	}

	var literal = function() {
		return ["(" + typeof(this.val) + ")", this.val];
	}

	var parserObject= {
		"+": {"led" : infix, "lbp" : 50},
		"-": {"led" : infix, "lbp" : 50},
		"*": {"led" : infix, "lbp" : 60},
		"/": {"led" : infix, "lbp" : 60},
		"===": {"led" : infix, "lbp" : 40},
		"!==": {"led" : infix, "lbp" : 40},
		"<=": {"led" : infix, "lbp" : 40},
		">=": {"led" : infix, "lbp" : 40},
		">": {"led" : infix, "lbp" : 40},
		"<": {"led" : infix, "lbp" : 40},
		"(literal)" : { "nud" : literal},
		"(end)" : { "nud" : function() { return undefined;}}
		//"else": {"lbp" : 0},
		//"(literal)": {"foo": "stringify"}
	};

	var defaultdenom = {
		"nud" : simplenud
	}

	var adddenom = function(elem) {
		appendObject(elem, parserObject[elem.id] || defaultdenom);
	};

	iter = filter(adddenom, iter);

	var token = iter();

	var next = function() {
		token = iter();
	}

	var parse = function (rbp) {
		var left;
		var t = token;

		next();
		left = t.nud();
		while (rbp < (token.lbp || 0)) {
			t = token;
			next();
			left = t.led(left);
		}
		return left;
	}

//////////////////////////////
// Some testing
//



while((x = parse(0)) !== undefined) {
	print_r(x);
}

1+2*4*5+4+3*4-4*3-2;
1*2-3*4*5;
