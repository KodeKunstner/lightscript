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

concat = function(a, b) {
        return a+b;
}

int2str= function(o) {
        return concat("", o);
}
toFun = function(o) { return o; }
toInt = function(o) { 
	if(typeof(o) === "string") {
		return parseInt(o, 10);
	} else {
		return o; 
	}
}
toObj = function(o) { return o; }
toArr = function(o) { return o; }
toVar = function(o) { return o; }
toBool = function(o) { return o; }

join = function(a, b) {
        return a.join(b);
}

pop = function(s) {
        return s.pop();
}

push = function(s, val) {
        return s.push(val);
}

line = "";
line_pos = -1;
line_nr = 1;
empty_line_count = 0;

getchar = function() {
        line_pos = line_pos + 1;
        if(line[line_pos] !== undefined) {
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

currentline = function() {
	return line_nr;
}
