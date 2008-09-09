load("stdmob.js");

var is_num = function(c) {
	return "0" <= c && c <= "9";
}

var is_alphanum = function(c) {
	return is_num(c) || c === "_" || ("a" <= c && c <= "z") || ("A" <= c && c <= "Z");
}

var is_symb = function(c) {
	return c === "=" || c === "!" || c === "<" || c === "&" || c === "|";
}


var tokeniser = {
	"c": " ",
	"next": function() {
		var c = this.c;
		while(c === " " || c === "\n" || c === "\t") {
			c = getch();
		}
		var token = [];
		this.type = "token";
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
				push(token, c);
				c = getch();
			}
			c = getch();
			this.type = "string";
		} else if(is_num(c)) {
			while(is_num(c)) {
				push(token, c);
				c = getch();
			}
			this.type = "number";
		} else if(is_alphanum(c)) {
			while(is_alphanum(c)) {
				push(token, c);
				c = getch();
			}
		} else if(is_symb(c)) {
			while(is_symb(c)) {
				push(token, c);
				c = getch();
			}
		} else {
			push(token, c);
			c = getch();
		}

		this.c = c;
		this.token = join(token, "");
	}
}

var parser = function() {
	infix(".", 700);
	infixl("(", 600);
	infixl("[", 600);
	comment(infix("*", 500));
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
	prefix2("function");
	prefix2("if");
	prefix2("while");
	atom("undefined");
	atom("true");
	atom("false");
}

while(tokeniser.c !== undefined) {
	tokeniser.next();
	println(join([tokeniser.type, tokeniser.token], " "));
}
