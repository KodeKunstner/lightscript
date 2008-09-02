load("stdmob.js");

//////////////////////////////
// Utility functions for reading a char at a time
////

var line, line_pos, line_nr, empty_line_count, getchar;

line = readline();
line_pos = -1;
line_nr = 1;

getchar = function() {
	line_pos = line_pos + 1;
	if(line[line_pos]) {
		empty_line_count = 0;
		return line[line_pos];
	} else {
		line_nr = line_nr + 1;
		line_pos = -1;
		line = readline();
		empty_line_count = empty_line_count + 1;
		if(empty_line_count > 10) {
			return undefined;
		} else {
			return "\n";
		} 
	}
}

////////////////////////////////
// Tokeniser
////


// strings
var c; 
// objs
var token;
// functions
var is_multisymb, is_ws, is_num, is_alphanum, nextc, nexttoken;

// initialisation
c = getchar(); // current char

//////
//Predicate functions
//////

is_multisymb = function() {
	return c === "|" || c === "<" || c === "=" || c === ">"
		|| c === "!";
};

is_ws = function() {
	return c === " " || c === "\n" || c === "\r" || c === "\t";
};

is_num = function() { 
	return c >= "0" && c <= "9"; 
};

is_alphanum = function() { 
	return ("a" <= c && c <= "z")
	    || ("A" <= c && c <= "Z")
	    || is_num() || c === "_";
};

nextc = function() {
	c = getchar();
}


nexttoken = function() {
	var str, default_token;

	// Skip whitespaces
	while(is_ws()) {
		nextc();
	};

	// Initialisation;
	token = {};
	token.line = line_nr;
	str = c;

	// Create token
	
	// End-token
	if(c === undefined) {
		str = "(end)";

	// Number literal
	} else if(is_num()) {

		nextc();
		while(is_num()) {
			str = str + c;
			nextc();
		}
		token.type = "int";
		token.val = parseInt(str, 10);
		str = "(literal)";

	// Identifier
	} else if(is_alphanum()) {

		nextc();
		while(is_alphanum()) {
			str = str + c;
			nextc();
		}

	// String literal
	} else if(c === "\"") {

		nextc();
		str = "";
		while(c !== "\"") {
			if(c === "\\") {
				nextc();
				c = {"n": "\n",
					"r": "\r",
					"t": "\t",
					"f": "\f",
					"b": "\b",
					"l": "\l",
				}[c] || c;
			}
			str = str + c;
			nextc();
		}
		nextc();
		token.type = "string";
		token.val = str;
		str = "(literal)";

	// Comment (and division passhtrough)
	} else if(c === "/") {
		nextc();
		if(c === "/") {
			nextc();
			str = "";
			while(c != "\n") {
				str = str + c;
				nextc();
			}
			token.val = str;
			str = "(comment)";
		} 

	// Symbol consisting of several chars
	} else if(is_multisymb()) {
		nextc();
		while(is_multisymb()) {
			str = str + c;
			nextc();
		}
	// Single symbol
	} else {
		nextc();
	}

	// Add properties to token
	token.id = str;
	default_token = parsers[str];
	for(key in default_token) {
		token[key] = default_token[key];
	}
};

/////////////////////////////////
// The parser
////

// functions 
var parse;
// objects
var parsers, ctx, globals;
// arrays
var ctx_stack;

parsers = {
};

ctx_stack = [];
ctx = {locals: {}, };
globals = {"readline": "fun", "print" : "fun", "load": "fun"};

// The parsing functions

decl = function() {
}

// The parser itself

////////////////////////////////
// Test code
////
nexttoken();

while(token.id !== "(end)") {
	print_r(token);
	nexttoken();
}
