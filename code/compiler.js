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
	var val = undefined;
	var token;
	var str = [];

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
		val= join(str);
		str= ["(string)"];

	//
	// Read number [0-9]*
	//
	} else if(is_num(c)) {
		while(is_num(c)) {
			push(str, c);
			c = getch();
		}
		val = join(str);
		str = ["(number)"];

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
			val = join(str);
			str = ["(comment)"];
			//token_c = getch();
			//return next_token();
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
	if(str[0] === undefined) {
		str = "(end)";
	} else {
		str = join(str);
	};

	// create result object
	token = {};
	token.str = str;
	token.nud = nuds[str] || function() { return {"id": this.str}; };
	if(leds[str] === undefined) {
		token.bp = 0;
	} else {
		token.led = leds[str].fn;
		token.bp = leds[str].bp;
	};
	token.sep = seps[str];
	token.val = val;
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
		return {"id": this.str, "args": [left, parsep(this.bp)]};
	};
};

infixr = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return {"id": this.str, "args": [left, parsep(this.bp - 1)]};
	};
};

infixl = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return {"id": join(["apply ", this.str]), "args": readlist([left])};
	};
};


readlist = function(acc) {
	var p = parse();
	while(p.id !== "(end)") {
		if(p.id !== "(sep)") {
			push(acc, p);
		}
		p = parse();
	}
	return acc;
};

end = function(str) {
	seps[str] = true;
	nuds[str] = function() {
		return {"id": "(end)", "val": this.str};
	}
};

seperator = function(str) {
	seps[str] = true;
	nuds[str] = function() {
		return {"id": "(sep)", "val": this.str};
	}
};

list = function(str) {
	nuds[str] = function() {
		return {"id": this.str, "args": readlist([])};
	}
};

atom = function(str) {
	nuds[str] = function() {
		return {"id": this.str};
	}
};

prefix = function(str) {
	nuds[str] = function() {
		return {"id": this.str, "args": [parse()]};
	}
};

prefix2 = function(str) {
	nuds[str] = function() {
		return {"id": this.str, "args": [parse(), parse()]};
	}
};

nuds["(string)"] = function() {
	return {"id": this.str, "val": this.val};
};

nuds["(number)"] = function() {
	return {"id": this.str, "val": this.val};
};

nuds["(comment)"] = function() {
	return {"id": this.str, "val": this.val};
};

nuds["var"] = function() {
	var acc = [];
	var p = parse();
	while(p.id !== "(end)" && p.val !== ";") {
		if(p.id !== "(sep)") {
			push(acc, p);
		}
		p = parse();
	}
	return {"id": this.str, "args": acc};
};

//
// Definition of operator precedence and type
//
infix(".", 700);
infixl("(", 600);
infixl("[", 600);
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
end(undefined);
seperator(":");
seperator(";");
seperator(",");
list("(");
list("{");
list("[");
prefix("return");
prefix("-");
prefix("!");
prefix2("function");
prefix2("if");
prefix2("while");
atom("undefined");
atom("true");
atom("false");
builtins = ["println", "getch", "push", "length", "stackdump", "load", "printglobals", "join"];

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
// Test
//

stmts = readlist([]);
println(stmts);
