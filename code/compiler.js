load("stdmob.js");
// 
// Tokeniser state
//

token_c = " ";

//
// Tokeniser
// 

is_num = function(c) {
	return "0" <= c && c <= "9";
};

is_alphanum = function(c) {
	return ("0" <= c && c <= "9") || c === "_" || ("a" <= c && c <= "z") || ("A" <= c && c <= "Z");
};

is_symb = function(c) {
	return c === "=" || c === "!" || c === "<" || c === "&" || c === "|";
};

next_token = function() {
	var c = token_c;
	var token;
	var str = [];
	var nud = undefined;

	while(c === " " || c === "\n" || c === "\t") {
		c = getch();
	}

	//
	// Read string
	//
	if(c === "\"") {
		c = getch();
		while(c !== undefined && c !== "\"") {
			if(c === "\\") {
				c = getch();
				if(c === "n") {
					c = "\n";
				} else if(c === "t") {
					c = "\t";
				}
			}
			push(str, c);
			c = getch();
		}
		c = getch();
		nud = function() { return ["string", this.str]; };

	//
	// Read number [0-9]*
	//
	} else if(is_num(c)) {
		while(is_num(c)) {
			push(str, c);
			c = getch();
		}
		nud = function() { return ["number", this.str]; };

	//
	// read identifier [_a-zA-Z][_a-zA-Z0-9]*
	//
	} else if(is_alphanum(c)) {
		while(is_alphanum(c)) {
			push(str, c);
			c = getch();
		}

	//
	// read multi character symbol
	//
	} else if(is_symb(c)) {
		while(is_symb(c)) {
			push(str, c);
			c = getch();
		}
	
	// read comments or division symbol
	} else if(c === "/") {
		c = getch();
		if(c === "/") {
			c = getch();
			while(c !== "\n") {
				push(str, c);
				c = getch();
			}
			nud = function() { return ["comment", this.str]; };
		} else {
			push(str, "/");
		}

	// read single symbol
	} else {
		push(str, c);
		c = getch();
	}

	// save state
	token_c = c;

	// handle end-of-file, and join characters to token
	if(c === undefined) {
		str = "(EOF)";
	} else {
		str = join(str);
	};

	// create result object
	token = {};
	token.str = str;
	token.nud = nud || nuds[str] || function() { return ["identifier", this.str]; };
	if(leds[str] === undefined) {
		token.bp = 0;
	} else {
		token.led = leds[str].fn;
		token.bp = leds[str].bp;
	};
	token.sep = seps[str];
	return token;
};

//
// tables of parsing functions and options for a given token string
//
nuds = {};
leds = {};
seps = {};

//
// functions for defining operator precedence and type
//
infix = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return [this.str, left, parsep(this.bp)];
	};
};

infixr = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return [this.str, left, parsep(this.bp - 1)];
	};
};

infixlist = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return readlist([join(["apply ", this.str]), left]);
	};
};


readlist = function(acc) {
	var p = parse();
	while(p[0] !== "(end)") {
		if(p[0] !== "(sep)") {
			push(acc, p);
		}
		p = parse();
	}
	return acc;
};

end = function(str) {
	seps[str] = true;
	nuds[str] = function() {
		return ["(end)", this.str];
	}
};

seperator = function(str) {
	seps[str] = true;
	nuds[str] = function() {
		return ["(sep)", this.str];
	}
};

list = function(str) {
	nuds[str] = function() {
		return readlist([this.str]);
	}
};

atom = function(str) {
	nuds[str] = function() {
		return [this.str];
	}
};

prefix = function(str) {
	nuds[str] = function() {
		return [this.str, parse()];
	}
};

prefix2 = function(str) {
	nuds[str] = function() {
		return [this.str, parse(), parse()];
	}
};

//
// Definition of operator precedence and type
//
infix(".", 700);
infixlist("(", 600);
infixlist("[", 600);
infix("*", 500);
infix("%", 500);
infix("+", 400);
infix("-", 400);
infix("===", 300);
infix("!==", 300);
infix("<=", 300);
infix("<", 300);
infixr("&&", 200);
infixr("||", 200);
infixr("else", 200);
infix("=", 100);
end("]");
end(")");
end("}");
end("(EOF)");
seperator(":");
seperator(";");
seperator(",");
list("(");
list("{");
list("[");
prefix("var");
prefix("return");
prefix("-");
prefix("!");
prefix2("function");
prefix2("if");
prefix2("while");
atom("undefined");
atom("true");
atom("false");

//
// The core parser
//
parse = function() {
	return parsep(0);
};

token = next_token();
parsep = function(rbp) {
	var t = token;
	token = next_token();
	var left = t.nud();
	while(rbp < token.bp && !t.sep) {
		t = token;
		token = next_token();
		left = t.led(left);
	}
	return left
};

//
// dump
//

println(readlist([]));
