// \section{Small MobyScript Parser}
//
// The following is a MobyScript parser written in MobyScript. It 
// is designed to be as small as possible and omits error checking. 
// It is using the top down operator precedence parsing technique, 
// as described in \cite{pratt-top-down-operator-precedence}. The 
// implementation is also inspired by the JavaScript parser in 
// \cite{beautiful-code}, which is also available online\cite{crockford-tdop}.
//
// It is stream oriented/lazy, such that it does not need to keep
// the entire source in memory, but can be read from other streams
// without large intermediate results in memory.
//
// The parser is encapsulated in the function

var parser = function(iter) {

// which takes a character iterator as an argument, and returns an iterator
// of parsetrees.
//
// \subsection{Tokeniser variables}
//
// The state of the tokeniser is stored in

	var c;
	var id; 
	var isnum;
	var token;

// where \verb|c| is the next character, \verb|id| is the parsed 
// identifier/token as a string, \verb|isnum| indicates whether \verb|id| 
// is a number, and \verb|token| is an object containing the token 
// currently being generated, and is also used to pass the token to the 
// parse tree generator.
//
// \verb|token| has several elements:
// 
// TODO: describe token.t, token.... here
//
// \subsection{Tokeniser}
// \subsubsection{Utility functions}
//
// Read the next character from the input iterator:

	var nextc = function() {
		iter.next();
		c = iter.val;
	};

//
// Check if the current character is contained in a given string:
	
	var oneof = function(symbs) {
		return has_element(symbs, c);
	};
	
//
// Skip white spaces:

	var skipws = function() {
		while(oneof(" \n\r\t")) {
			nextc();
		}
	};
	
// \subsubsection{String tokeniser}
//
// The following is a simple tokeniser, that generates a string from a sequence
// of character. It TODO:spelling:distengueses integers (\verb|[0-9]+|), 
// beginning of comment (\verb|/[/*]|), identifiers 
// (\verb|[$_a-zA-Z][$_a-zA-Z0-9]*|), operaters 
// (\verb"[<>/|=+-*&^%!~]+") and single symbols (the rest).
// It also sets a flag if it found an integer.
//
// Forward declarations, not needed, but nice for JSLint

	var nextid =  function() {
		var num = "0123456789";
		var ident = "$_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		var oper = "<>/|=+-*&^%!~";
	
		var symbs = "";
		id = c;
	
		if(oneof(num)) {
			symbs = num;
			isnum = true;
		} else if(oneof(ident)) {
			symbs = num + ident;
		} else if(oneof(oper)) {
			symbs = oper;
		}
	
		nextc();
	
		if(id === "/" && oneof("/*")) {
			id = id + c;
			nextc();
		} else {
			while(oneof(symbs)) {
				id = id + c;
				nextc();
			}
		}
	};

// \subsubsection{String tokeniser}
//
// This is the core tokeniser.
	
	var nexttoken = function() {
		var val;
		skipws();
		nextid();

		if(id === undefined) {
			id = "(end)";
		} else if(isnum) {
			isnum = false;
			val = parseInt(id, 10);
			id = "(literal)";
		} else if(id === "//") {
			val = id;
			while(id !== undefined && id !== "\n") {
				nextid();
				val = val + id;
			}
			id = "(comment)";
		} else if(id === "/*") {
			val = id;
			while(id !== undefined && id !== "*/") {
				nextid();
				val = val + id;
			}
			id = "(comment)";
		} else if(id === "\"") {
			val = "";
			nextid();
			while(id !== undefined && id !== "\"") {
				if(id === "\\") {
					val = val + ({"n": "\n", "r": "\r", "t": "\t"}[c] || c);
					nextc();
				} else {
					val = val + id;
				}
				nextid();
			}
			id = "(literal)";
		} 

		token = copy(parserObject[id]);
		token.n = token.n || function() {};
		token.l = token.l || infix;
		token.p = token.p || 0;
		token.id = id;
		if(val !== undefined) {
			token.val = val;
		}
	};
	
	var infix = function (prev) { 
		this.args = [prev, parse(this.p)];
	};

	var infixr = function (prev) { 
		this.args = [prev, parse(this.p - 1)];
	};

	var prefix = function() { 
		this.args = [parse()];
	};

	var prefix2 = function() { 
		this.args = [parse(), parse()]; 
	};

	var readlist = function(arr) {
		var t = parse();
		while(!t.rpar) {
			if(!t.sep) {
				arr.push(t);
			}
			t = parse();
		}
	};

	var list = function() {
		this.id = "list" + this.id;
		this.args = [];
		readlist(this.args);
	};
	
	var apply = function(prev) {
		this.id = "apply" + this.id;
		this.args = [prev];
		readlist(this.args);
	};
	
	var if_else = function() {
		this.args = [parse(), parse()];
		if(token.id === "else") {
			nexttoken();
			this.args.push(parse());
		}
	};
	
	var parserObject = {
		"return": {"n" : prefix},
		"var": {"n" : prefix},
		"delete": {"n" : prefix},
		"function": {"n" : prefix2},
		"while": {"n" : prefix2},
		"if": {"n" : if_else},
		"+": {"p" : 400},
		".": {"p" : 600},
		"-": {"n" : prefix, "l" : infix, "p" : 400},
		"*": {"p" : 500},
		"===": {"p" : 300},
		"!==": {"p" : 300},
		"<=": {"p" : 300},
		">=": {"p" : 300},
		">": {"p" : 300},
		"<": {"p" : 300},
		"&&": {"l" : infixr, "p" : 200},
		"||": {"l" : infixr, "p" : 200},
		"=": {"l" : infixr, "p" : 100},
		"[": { "n" : list, "l" : apply, "p" : 600},
		"(": { "n" : list, "l" : apply, "p" : 600},
		"{": { "n" : list},
		"," : { sep: true, "p" : -100},
		":" : { sep: true, "p" : -100},
		";" : { sep: true, "p" : -200},
		")" : { rpar: true, "p" : -300},
		"}" : { rpar: true, "p" : -300},
		"]" : { rpar: true, "p" : -300},
		"(end)" : {rpar: true, "p" : -300}
	};

//
// \subsection{Initialisation}
// 
	nextc();
	nexttoken();

// 
// \subsection{The parser core}
	
	var parse = function (rbp) {
		var prev;
		var t = token;
	
		rbp = rbp || 0;
	
		nexttoken();
		t.n();
	
		while (rbp < token.p && !(t.sep || t.rpar)) {
			delete t.p; delete t.n; delete t.l;
			prev = t; t = token; nexttoken();
			t.l(prev);
		}
		delete t.p; delete t.n; delete t.l;
		if(t.id === "(end)") {
			return undefined;
		}
		return t;
	};

// \subsection{End of code}
// Just return the parser.

	return parse;
};
