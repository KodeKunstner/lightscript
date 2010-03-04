var copy = function(obj) {
	var result = {};
	for(x in obj) {
		result[x] = obj[x];
	}
	return result;
}

var has_element = function(set, elem) {
	for(key in set) {
		if(set[key] === elem) {
			return true;
		}
	}
	return false;
}

var iterator = function(obj) {
	var keys = [];
	for(key in obj) {
		keys.push(key);
	}
	keys.reverse();
	return { 
		next: function() {
			this.key= keys.pop();
			if(this.key) {
				this.val = obj[this.key];
			} 
			return this.key !== undefined;
		}
	}
}; 

var map = function(fn, obj) {
	for(key in obj) {
		fn(obj[key]);
	}
};

var print_r = function(obj) {
	var genstr = function(obj, acc) {
		var t, i;
		if(typeof(obj) === "string") {
			return "\""+obj+"\"";
		} else if(obj instanceof Array) {
			acc.push("[");
			t = [];
			for(i=0;i<obj.length;i++) {
				t.push(genstr(obj[i], []));
			}
			acc.push(t.join(", "));
			acc.push("]");
		} else if(obj instanceof Object) {
			acc.push("{");
			t = [];
			for(key in obj) {
				t.push( "\"" + key + "\": "+ genstr(obj[key], []));
			}
			acc.push(t.join(", "));
			acc.push("}");
		} else {
			acc.push(obj);
		}
		return acc.join("");

	}
	print(genstr(obj, []));
};

var getch = {"next": function() {
	// "" instead of readline() avoids reading from stdin,
	// when getch is not used. On the other hand, this inserts
	// an empty line in the beginning of stdin.
	//
	//var iter = iterator(readline());
	var line = readline();
	var pos = -1;
	var emptycount = 0;
	return function() {
		pos = pos + 1;

		if(pos >= line.length) {
			if(emptycount > 10) {
				this.val = undefined;
				return false;
			}
			pos = -1;
			line = readline();
			if(line === "") {
				emptycount = emptycount + 1;
			} else {
				emptycount = 0;
			}
			this.val = "\n";
			return true;
		}
		this.val = line[pos];

		return true;
	}
}()};
