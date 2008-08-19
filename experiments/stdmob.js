load("json2.js");
var print_r = function(obj) {
	var str = JSON.stringify(obj);
	print(str);
};

var getch = function() {
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
				return undefined;
			}
			pos = -1;
			line = readline();
			if(line === "") {
				emptycount = emptycount + 1;
			} else {
				emptycount = 0;
			}
			return "\n";
		}

		return line[pos];
	}
} ();
