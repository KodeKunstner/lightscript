str2int = function(s) {
	parseInt(s, 10);
}
is_int = function(o) {
	return typeof(o) === "number";
}

is_str = function(o) {
	return typeof(o) === "string";
}

is_arr = function(o) {
	return o instanceof Array;
}

is_obj = function(o) {
	return o instanceof Object;
}

strcat = function(a, b) {
        return a+b;
}

arrjoin = function(a, b) {
        return a.join(b);
}

arrpop = function(s) {
        return s.pop();
}

arrpush = function(s, val) {
        return s.push(val);
}

std = {};
std._ = {};

std._.line = "";
std._.line_pos = -1;
std._.line_nr = 0;
std._.empty_line_count = 0;

std.io = {};
std.io.getchar = function() {
	var _;
	_ = std._;
        _.line_pos = _.line_pos + 1;
	_.line = _.line;
        if(_.line[_.line_pos] !== undefined) {
                _.empty_line_count = 0;
                return _.line[_.line_pos];
        } else {
                _.line_nr = _.line_nr + 1;
                _.line_pos = -1;
                _.line = readline();
                _.empty_line_count = _.empty_line_count + 1;
                if(_.empty_line_count > 10) {
                        return undefined;
                } else {
                        return "\n";
                } 
        }
}

std.io.currentline = function() {
	return std._.line_nr;
}

std.io.println = function(obj) {
	var genstr;
	if(typeof(obj) === "string") {
		print(obj);
		return;
	}

	genstr = function(obj, acc) {
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
}
std.io.printerror = function(obj) {
	std.io.println(obj);
};

