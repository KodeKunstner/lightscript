load("stdmob.js");

getch();

/**
 * Character reader
 */
var c; // current character

var nextc = function() {
	c = getch();
}

nextc();

/**
 * Id-reader 
 */
var id;
var val;
var led;
var nud;
var lbp;

var oneof = function(symbs) {
	symbiter = iterator(symbs);
	while((symb = symbiter()) !== undefined) {
		if (c === symb) {
			return true;
		}
	}
	return false;
}

var skipws = function() {
	while(oneof(" \n\r\t")) {
		nextc();
	}
}

var nextid =  function() {
	var num = "0123456789";
	var ident = "$_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	var oper = "<>/|=+-*&^%!~";

	var symbs = "";
	id = c;
	val = false;

	if(oneof(num)) {
		symbs = num;
		val = true;
	} else if(oneof(ident)) {
		symbs = num + ident;
	} else if(oneof(oper)) {
		symbs = oper;
	}

	nextc();

	if(id === "/" && oneof("/*")) {
		id += c;
		nextc();
	} else while(oneof(symbs)) {
		id += c;
		nextc();
	}
}

/**
 * Skip white-space, literals and comments;
 */

// value, being used as is_number flag, and later containing value of literals or comments.

var nexttoken = function() {
	skipws();
	nextid();

	if(val) {
		id = "(number)";
		val = parseInt(id);
		nud = literal;
	} else if(id === "//") {
		val = id;
		while(id !== undefined && id !== "\n") {
			nextid();
			val += id;
		}
		id = "(comment)";
		nud = literal;
	} else if(id === "/*") {
		val = id;
		while(id !== undefined && id !== "*/") {
			nextid();
			val += id;
		}
		id = "(comment)";
		nud = literal;
	} else if(id === "\"") {
		val = "";
		nextid();
		while(id !== undefined && id !== "\"") {
			if(id === "\\") {
				nextid();
			}
			val += id;
			nextid();
		}
		id = "(number)";
		nud = literal;
	} else {
		obj = parserObject[id] || defaultdenom;
		led = obj.led;
		nud = obj.nud;
		lbp = obj.lbp;
		val = obj.val;
	}
}

var iter = function() { 
	nexttoken();
	var obj = {"id": id, "val": val, "led": led, "nud": nud, "lbp": lbp }; 
	//print(nud === undefined);
//	print_r(obj);
	return obj;
};

var simplenud = function() {
	return this.id;
}

// strings and numbers
var literal = function() {
	return [this.id, this.val];
}

// lists: {...}, [...], (...)
var listnud = function() {
	var result = ["list"+this.id];
	var t = parse();
	while(t !== this.val) {
		result.push(t);
		t = parse();
	}
	return result;
}

// apply-list: function call() and subscript[]
var listled = function(left) {
	var result = ["apply" + this.id, left];
	var t = parse();
	while(t !== this.val) {
		result.push(t);
		t = parse();
	}
	return result;
}

var infix = function (left) {
	var result = [this.id, left]
	result.push(parse(this.lbp));
	return result;
}

var infixr = function (left) {
	var result = [this.id, left]
	result.push(parse(this.lbp - 1));
	return result;
}

// return, var
var prefix = function() {
	return [this.id, parse()];
}

// function, for, while
var prefix2 = function() {
	return [this.id, parse(), parse()];
}

// if-else
var ifnud = function() {
	var result = [this.id, parse(), parse()];
	if(token.id === "else") {
		next();
		result.push(parse());
	}
	return result;
}

// seperators (if led, bypass nud)
var seperator = function() {
	this.seperator = true;
	return this.id;
}

var parserObject= {
	"return": {"nud" : prefix},
	"var": {"nud" : prefix},
	"delete": {"nud" : prefix},
	"function": {"nud" : prefix2},
	"for": {"nud" : prefix2},
	"while": {"nud" : prefix2},
	"if": {"nud" : ifnud},
	"+": {"led" : infix, "lbp" : 50},
	".": {"led" : infix, "lbp" : 80},
	"-": {"nud" : prefix, "led" : infix, "lbp" : 50},
	"*": {"led" : infix, "lbp" : 60},
	"/": {"led" : infix, "lbp" : 60},
	"===": {"led" : infix, "lbp" : 40},
	"!==": {"led" : infix, "lbp" : 40},
	"<=": {"led" : infix, "lbp" : 40},
	">=": {"led" : infix, "lbp" : 40},
	">": {"led" : infix, "lbp" : 40},
	"<": {"led" : infix, "lbp" : 40},
	"&&": {"led" : infixr, "lbp" : 30},
	"||": {"led" : infixr, "lbp" : 30},
	"=": {"led" : infixr, "lbp" : 10},
	"[": { "nud" : listnud, "val": "]",  "led" : listled, "lbp" : 80},
	"(": { "nud" : listnud, "val": ")",  "led" : listled, "lbp" : 80},
	"{": { "nud" : listnud, "val": "}"},
	"," : { "nud" : seperator, "lbp" : -100},
	":" : { "nud" : seperator, "lbp" : -100},
	";" : { "nud" : seperator, "lbp" : -200},
	")" : { "nud" : seperator, "lbp" : -300},
	"}" : { "nud" : seperator, "lbp" : -300},
	"]" : { "nud" : seperator, "lbp" : -300},
	"(number)" : { "nud" : literal},
	"(string)" : { "nud" : literal},
	"(comment)" : { "nud" : literal},
	"(end)" : { "nud" : function() { return undefined;}}
};

var defaultdenom = {
	"nud" : simplenud
}


/////////////////////////////////////////////////////////////

var token = iter();

var next = function() {
	token = iter();
	//print_r(["token:", token])
}

var parse = function (rbp) {
	var left;
	var t = token;;

	rbp = rbp || 0;

	t = token;
	next();
	//print_r(["nud", t, rbp, token]);
	left = t.nud();

	while (!t.seperator && rbp < (token.lbp || 0)) {
		t = token;
		next();
		//print_r(["led", t]);
		left = t.led(left);
	}
	//print_r({"result": left});
	return left;
}


while((x = parse(0)) !== undefined) {
        print_r(["output:", x]);
}

