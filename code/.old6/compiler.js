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
// Compiler
//

fnvars = [];
simplify = function(elem) {
	var id = elem.id;
	var result, i;
	if(id === "=") {
		map(simplify, elem.args);
		result = elem;
		if(elem.args[0].id === "apply [") {
			result = {};
			result.id = "(put)";
			result.args = [];
			result.args[0] = elem.args[0].args[0];
			result.args[1] = elem.args[0].args[1];
			result.args[2] = elem.args[1];

		}
		return result;
	} else if(id === "-") {
		if(length(elem.args) === 1) {
			elem.id = "(neg)";
		}
	} else if(id === ".") {
		result = {};
		result.id = "apply [";
		result.args = [];
		result.args[0] = elem.args[0];
		result.args[1] = { "id": "(string)", "val": elem.args[1].id };
		return simplify(result);
	} else if(id === "function") {
		var prevvars = fnvars;
		fnvars = [];
		elem.vars = fnvars;
		elem.parameters = [];

		i = 0;
		while(i < length(elem.args[0].args)) {
			push(elem.parameters, elem.args[0].args[i].id);
			i = i + 1;
		}

		elem.args = map(simplify, elem.args);
		fnvars = prevvars;
		return elem;
	} else if(id === "if") {
		map(simplify, elem.args);
		result = elem;
		if(elem.args[1].id === "else") {
			result = {};
			result.id = "(if-else)";
			result.args = [];
			result.args[0] = elem.args[0];
			result.args[1] = elem.args[1].args[0];
			result.args[2] = elem.args[1].args[1];

		}
		return result;
	} else if(id === "var") {
		i = 0;
		while(i < length(elem.args)) {
			var child = elem.args[i];
			if(child.id === "=") {
				push(fnvars, child.args[0].id);
			} else {
				push(fnvars, child.id);
			}
			i = i + 1;
		}
	}	
	elem.args = map(simplify, elem.args);
	return elem;
}

//
// sol compiler
//
solunop = function (elem, acc)  {
	moby2sol(elem.args[0], acc);
	push(acc, elem.id);
}
solbinop = function (elem, acc)  {
	moby2sol(elem.args[0], acc);
	moby2sol(elem.args[1], acc);
	push(acc, elem.id);
}
locals = [];
localid = function(name) {
	var i = 0;
	while(i < length(locals)) {
		if(locals[i] === name) {
			return i;
		}
		i = i + 1;
	}
	return undefined;
	println(name);
}

pushstring = function(acc, str) {
	var result = ["\""];
	var i = 0;
	var c;
	while(i < length(str)) {
		c = str[i];
		if(c === "\"" || c === "\\") {
			push(result, "\\");
		} else if(c === "\n") {
			push(result, "\\");
			c = "n";
		} else if(c === "\t") {
			push(result, "\\");
			c = "t";
		}
		push(result, c);
		i = i + 1;
	}
	push(result, "\"");
	push(acc, join(result));
}
mobyblock2sol = function(arr, acc) {
	var i, id;
	i = 0;
	while(i < length(arr)) {
		moby2sol(arr[i], acc);
		i = i + 1;
		if(i < length(arr)) {
			push(acc, "(drop)");
		}
		push(acc, "\n");
	}
	if(length(arr) === 0) {
		push(acc, "false");
	}
	return acc;
}
moby2sol = function(elem, acc) {
	var id = elem.id;
	var i;
	if(id === "<") {
		solbinop(elem, acc);
	} else if(id === "<=") {
		solbinop(elem, acc);
	} else if(id === "=") {
		moby2sol(elem.args[1], acc);

		i = localid(elem.args[0].id);
		if(i === undefined) {
			pushstring(acc,elem.args[0].id)
			push(acc, "(set-global)");
		} else {
			push(acc, i);
			push(acc, "(set-local)");
		}
	} else if(id === "===") {
		solbinop(elem, acc);
	} else if(id === "||") {
		solbinop(elem, acc);
	} else if(id === "-") {
		solbinop(elem, acc);
	} else if(id === "!") {
		solunop(elem, acc);
	} else if(id === "!==") {
		solbinop(elem, acc);
	} else if(id === "(") {
		if(elem.args > 1) {
			println("Error: more than one expr in parenthesis");
		}
		moby2sol(elem.args[0], acc);
	} else if(id === "[") {
		push(acc, "(new-array)");
		i;
		while(i < length(elem.args)) {
			moby2sol(elem.args[i], acc);
			push(acc, "push");
			i = i + 1;
		}
	} else if(id === "{") {
		push(acc, "(new-array)");
		i;
		while(i < length(elem.args)) {
			moby2sol(elem.args[i], acc);
			push(acc, "push");
			i = i + 1;
		}
		push(acc, "(array-to-object)");
	} else if(id === "&&") {
		solbinop(elem, acc);
	} else if(id === "+") {
		solbinop(elem, acc);
	} else if(id === "apply (") {
		// push new this
		if(elem.args[0].id === "apply [") {
			moby2sol(elem.args[0].args[0], acc);
		} else {
			push(acc, "false");
		}
		// push function and args
		i = 0;
		while(i < length(elem.args)) {
			moby2sol(elem.args[i], acc);
			i = i + 1;
		}
		// push argc
		push(acc, length(elem.args) - 1);
		push(acc, "(call)");
	} else if(id === "apply [") {
		moby2sol(elem.args[0], acc);
		moby2sol(elem.args[1], acc);
		push(acc, "(get)");
	} else if(id === "(comment)") {
		pushstring(acc, elem.val);
	} else if(id === "function") {
		var prevlocals = locals;
		push(acc, "{");
		push(acc, length(elem.parameters));
		push(acc, length(elem.parameters) + length(elem.vars));
		push(acc, "(initialise-function)");
		locals = [];
		i = 0;
		while(i < length(elem.parameters)) {
			push(locals, elem.parameters[i]);
			i = i + 1;
		}
		i = 0;
		while(i < length(elem.vars)) {
			push(locals, elem.vars[i]);
			i = i + 1;
		}
		mobyblock2sol(elem.args[1].args, acc);
		push(acc, "}");
		//push(acc, "function");
		locals = prevlocals;
	} else if(id === "if") {
		moby2sol(elem.args[0], acc);
		push(acc, "{");
		mobyblock2sol(elem.args[1].args, acc);
		push(acc, "}");
		push(acc, "if");
	} else if(id === "(if-else)") {
		moby2sol(elem.args[0], acc);
		push(acc, "{");
		mobyblock2sol(elem.args[1].args, acc);
		push(acc, "}");
		push(acc, "{");
		if(elem.args[2].id === "{") {
			mobyblock2sol(elem.args[2].args, acc);
		} else {
			moby2sol(elem.args[2], acc);
		}
		push(acc, "}");
		push(acc, "if-else");
	} else if(id === "(neg)") {
		solunop(elem, acc);
	} else if(id === "(number)") {
		push(acc, elem.val);
	} else if(id === "(put)") {
		moby2sol(elem.args[0], acc);
		moby2sol(elem.args[1], acc);
		moby2sol(elem.args[2], acc);
		push(acc, id);
	} else if(id === "return") {
		solunop(elem, acc);
	} else if(id === "(string)") {
		pushstring(acc, elem.val);
	} else if(id === "this") {
		push(acc, id);
	} else if(id === "true") {
		push(acc, id);
	} else if(id === "false") {
		push(acc, id);
	} else if(id === "undefined") {
		push(acc, "false");
	} else if(id === "var") {
		mobyblock2sol(elem.args, acc);
	} else if(id === "while") {
		push(acc, "{");
		moby2sol(elem.args[0], acc);
		push(acc, "}");
		push(acc, "{");
		mobyblock2sol(elem.args[1].args, acc);
		push(acc, "}");
		push(acc, "while");
	} else {
		i = localid(id);
		if(i === undefined) {
			pushstring(acc, id);
			push(acc, "(get-global)");
		} else {
			push(acc, i);
			push(acc, "(get-local)");
		}
	}
	return acc;
}
	
//
// Test
//

stmts = readlist([]);
ids = [];
map(simplify, stmts);
sol = [];
mobyblock2sol(stmts, sol);
//println(stmts);
//println(sol);
i = 0;
while(i < sol.length) {
	println(sol[i]);
//	println(stmts[i]);
//	println(moby2sol(stmts[i], []).join(" "));
	i = i + 1;
};
