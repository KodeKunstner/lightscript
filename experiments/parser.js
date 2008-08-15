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

		token = iter();
		if(token === undefined) {
			return undefined;
		}

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


	//var token = iter();
	var next = function() {
		token = iter();
	}

	var parse = function (rbp) {
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

//////////////////////////////
// Some testing
//
iter = tokeniser(getch);
iter = cleanup(iter);

// remove comments
iter = filter(function(elem) { return elem.id === "(comment)"; },iter);

// remove whitespaces
iter = filter(function(elem) { return elem.id === " " || elem.id === "\n" || elem.id === "\t" || elem.id === "\r"; },iter);

// add functions for building syntax tree to token
iter = filter(adddenom, iter);

while((token = iter()) !== undefined) {
	print_r(token);
}
