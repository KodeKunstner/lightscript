str2int= function(s) {
	parseInt(s, 10);
};

is_a = function(o, t) {
	return (typeof(o) === t)
	|| (t === "array" && o instanceof Array)
	|| (t === "function" && o instanceof Function)
	|| (t === "object" && o instanceof Object);
};

length = function(x) {
	return x.length;
};

join = function(a, b) {
        return a.join(b);
};

pop = function(s) {
        return s.pop();
};

push = function(s, val) {
        return s.push(val);
};


iterator = function(x) {
	var key, result;
	result = [];
	for(key in x) {
		if(x instanceof Array) {
			push(result, x[key]);
		} else {
			push(result, key);
		}
	}
	return result.reverse();
};
next = pop;

_ = {};
_input_line = "";
_input_line_pos = -1;
_input_line_nr = 0;
_input_empty_line_count = 0;

getch = function() {
        _input_line_pos = _input_line_pos + 1;
	_input_line = _input_line;
        if(_input_line[_input_line_pos] !== undefined) {
                _input_empty_line_count = 0;
                return _input_line[_input_line_pos];
        } else {
                _input_line_nr = _input_line_nr + 1;
                _input_line_pos = -1;
                _input_line = readline();
                _input_empty_line_count = _input_empty_line_count + 1;
                if(_input_empty_line_count > 10) {
                        return undefined;
                } else {
                        return "\n";
                } 
        }
};

current_line = function() {
	return _input_line_nr;
};

println = function(obj) {
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
};

