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
		var token = current;
		var tokensymbs = "";
		var type = "single";

		if(oneof(num)) {
			tokensymbs = num;
			type = "num";
		}

		if(oneof(ident)) {
			tokensymbs = num + ident;
			type = "ident";
		}

		if(oneof(oper)) {
			tokensymbs = oper;
			type = "oper";
		}

		next();
		if(current === undefined) {
			return undefined;
		}

		while(oneof(tokensymbs)) {
			token += current;
			next();
		}
		return {"str": token, "type": type};
	}

	return nextToken;
}

var filter = function(predicate, iter) {
	return function() {
		var result;
		do {
			result = iter();
		} while (result !== undefined && !predicate(result));
		return result
	}
}

var cleanup = function(iter) {
	return function() {
		var token = ""; 
		var str = "";
		var type;

		// Skip whitespace, and return if end of stream
		do {
			token = iter();
			if(token === undefined) {
				return undefined;
			}
		} while (token.str === " " || token.str === "\t" 
		   || token.str === "\n" || token.str === "\r");

		// Comments
		if(token.str === "//") {
			token = iter();
			while(token !== undefined && token.str !== "\n") {
				str = str + token.str;
				token = iter();
			}
			type = "comment";

		} else if(token.str === "/*" || token.str === "/**") {
			token = iter();
			while(token !== undefined && token.str !== "*/") {
				str = str + token.str;
				token = iter();
			}
			type = "comment";

		// String
		} else if(token.str === "\"") {
			token = iter();
			while(token !== undefined && token.str !== "\"") {
				if(token.str === "\\") {
					token = iter();
				}
				str += token.str;
				token = iter();
			}
			type = "string";

		} else {
			return token;
		}
		return {"str": str, "type": type};
	}
}

iter = cleanup(tokeniser(getch));



while((token = iter()) !== undefined) {
	print_r(token);
}
