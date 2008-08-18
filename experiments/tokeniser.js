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

	var iter2 =  function() {

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

	var cleanup = function() {
		var token;
		var str = "";
		var type;
		var i = 0;

		token = iter2() || {"id":"(end)"};

		// Comments
		if(token.id === "//") {
			token = iter2();
			while(token !== undefined && token.id !== "\n") {
				str = str + token.id ;
				token = iter2();
			}
			token.val = str;
			token.id = "(comment)";

		} else if(token.id === "/*" || token.id === "/**") {
			token = iter2();
			while(token !== undefined && token.id !== "*/") {
				str = str + token.id;
				token = iter2();
			}
			token.val = str;
			token.id = "(comment)";

		// String
		} else if(token.id === "\"") {
			token = iter2();
			while(token !== undefined && token.id !== "\"") {
				if(token.id === "\\") {
					token = iter2();
				}
				str += token.id;
				token = iter2();
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

	// remove whitespaces
	return filter(function(elem) { return elem.id === " " || elem.id === "\n" || elem.id === "\t" || elem.id === "\r"; },cleanup);
};
