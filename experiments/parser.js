var parser = function(iter) {
	var simplenud = function() {
		return this.id;
	}

	// strings and numbers
	var literal = function() {
		return [this.id + typeof(this.val), this.val];
	}

	// lists: {...}, [...], (...)
	var listnud = function() {
		var result = ["list"+this.id];
		var t = parse(0);
		while(t !== this.end) {
			result.push(t);
			t = parse(0);
		}
		return result;
	}

	// apply-list: function call() and subscript[]
	var listled = function(left) {
		var result = ["apply" + this.id, left];
		var t = parse(0);
		while(t !== this.end) {
			result.push(t);
			t = parse(0);
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
		return [this.id, parse(0)];
	}

	// function, for, while
	var prefix2 = function() {
		return [this.id, parse(0), parse(0)];
	}

	// if-else
	var ifnud = function() {
		var result = [this.id, parse(0), parse(0)];
		if(token.id === "else") {
			next();
			result.push(parse(0));
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
		"[": { "nud" : listnud, "end": "]",  "led" : listled, "lbp" : 80},
		"(": { "nud" : listnud, "end": ")",  "led" : listled, "lbp" : 80},
		"{": { "nud" : listnud, "end": "}"},
		"," : { "nud" : seperator, "lbp" : -100},
		":" : { "nud" : seperator, "lbp" : -100},
		";" : { "nud" : seperator, "lbp" : -200},
		")" : { "nud" : seperator, "lbp" : -300},
		"}" : { "nud" : seperator, "lbp" : -300},
		"]" : { "nud" : seperator, "lbp" : -300},
		"(literal)" : { "nud" : literal},
		"(comment)" : { "nud" : literal},
		"(end)" : { "nud" : function() { return undefined;}}
	};

	var defaultdenom = {
		"nud" : simplenud
	}

	var adddenom = function(elem) {
		appendObject(elem, parserObject[elem.id] || defaultdenom);
	};

	iter = filter(adddenom, iter);

	var token = iter();

	var next = function() {
		token = iter();
		//print_r(["token:", token])
	}

	var parse = function (rbp) {
		var left;
		var t = token;;

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

	return parse;
};
