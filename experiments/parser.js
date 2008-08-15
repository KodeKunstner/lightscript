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

var filter = function(predicate, iter) {
	return function() {
		var result;
		do {
			result = iter();
		} while (result !== undefined && predicate(result));
		return result
	}
}

var cleanup = function(iter) {
	return function() {
		var token;
		var str = "";
		var type;
		var i = 0;

		// Skip whitespace, and return if end of stream
		do {
			i++;
			token = iter();
			if(token === undefined) {
				return undefined;
			}
		} while (token.id === " " || token.id === "\t" 
		   || token.id === "\n" || token.id === "\r");

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
			token.val = parseInt(token.str);
			token.id = "(literal)"
		} 
		return token;
	}
}

var qwerty = 123;

iter = tokeniser(getch);
iter = cleanup(iter);
iter = filter(function(elem) { return elem.id === "(comment)"},iter);

var appendObject = function(dst, src) {
	for(elem in src) {
		dst[elem] = src[elem];
	}
}


/////////////////////////////
// Beginning of parser
//
	var parserObject= {
		"(literal)": {"foo": "stringify"}
	};

	var defaultdenom = {
		"foo": "bar"
	}

	var adddenom = function(elem) {
		appendObject(elem, parserObject[elem.id] || defaultdenom);
	};

iter = filter(adddenom, iter);

	//var token = iter();
	var next = function() {
		token = iter();
	}

	var expression = function (rbp) {
		var left;
		var t = token;
		next();
		left = t.nud();
		while (rbp < token.lbp) {
			t = token;
			next();
			left = t.led(left);
		}
		return left;
	}

while((token = iter()) !== undefined) {
	print_r(token);
}
