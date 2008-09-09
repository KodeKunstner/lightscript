load("stdmob.js");

is_num = function(c) {
	return "0" <= c && c <= "9";
}

is_alphanum = function(c) {
	return is_num(c) || c === "_" || ("a" <= c && c <= "z") || ("A" <= c && c <= "Z");
}

is_symb = function(c) {
	return c === "=" || c === "!" || c === "<" || c === "&" || c === "|";
}


token_c = " ";
next_token = function() {
	var c = token_c;
	var val = undefined;
	var token;
	while(c === " " || c === "\n" || c === "\t") {
		c = getch();
	}
	var str = [];
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
		val= join(str, "");
		str= ["(string)"];
	} else if(is_num(c)) {
		while(is_num(c)) {
			push(str, c);
			c = getch();
		}
		val = join(str, "");
		str = ["(number)"];
	} else if(is_alphanum(c)) {
		while(is_alphanum(c)) {
			push(str, c);
			c = getch();
		}
	} else if(is_symb(c)) {
		while(is_symb(c)) {
			push(str, c);
			c = getch();
		}
	} else {
		push(str, c);
		c = getch();
	}

	token_c = c;
	str = join(str, "");
	token = {};
	token.str = str;
	token.nud = nuds[str] || defaultnud;
	if(leds[str] === undefined) {
		token.bp = 0;
	} else {
		token.led = leds[str].fn;
		token.bp = leds[str].bp;
	}
	token.sep = seps[str];
	token.val = val;
	return token;
}

nuds = {};
leds = {};
seps = {};

defaultnud = function() {
	return ["(symb)", this.str];
}

defaultled = { "bp": 0 }

infix = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return ["(binop)", this.str, left, parsep(this.led.bp)];
	}
}

infixr = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return ["(binop)", this.str, left, parsep(this.led.bp - 1)];
	}
}

infixl = function(str, bp) {
	leds[str] = {};
	leds[str].bp = bp;
	leds[str].fn = function(left) {
		return readlist(["(apply)", this.str, left]);
	}
}


readlist = function(acc) {
	var p = parse();
	while(p[0] !== "(end)") {
		if(p[0] !== "(sep)") {
			push(acc, p);
		}
		p = parse();
	}
	return acc;
}

end = function(str) {
	seps[str] = true;
	nuds[str] = function() {
		return ["(end)", this.str];
	}
}

seperator = function(str) {
	seps[str] = true;
	nuds[str] = function() {
		return ["(sep)", this.str];
	}
}

list = function(str) {
	nuds[str] = function() {
		return readlist(["(list)", str]);
	}
}

atom = function(str) {
	nuds[str] = function() {
		return ["(atom)", this.str];
	}
}

prefix = function(str) {
	nuds[str] = function() {
		return ["(unary)", this.str, parse()];
	}
}

prefix2 = function(str) {
	nuds[str] = function() {
		return ["(control)", this.str, parse(), parse()];
	}
}

nuds["(string)"] = function() {
	return [this.str, this.val];
}

nuds["(number)"] = function() {
	return [this.str, this.val];
}

infix(".", 700);
infixl("(", 600);
infixl("[", 600);
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
prefix2("function");
prefix2("if");
prefix2("while");
atom("undefined");
atom("true");
atom("false");

var parse = function() {
	return parsep(0);
};

token = next_token();
var parsep = function(rbp) {
	var t = token;
	token = next_token();
	var left = t.nud();
	while(rbp < token.bp && !t.sep) {
		t = token;
		token = next_token();
		left = t.led(left);
	}
	return left
}

while(token_c !== undefined) {
	println(parse());
}
