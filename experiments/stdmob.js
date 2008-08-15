// printing utility
load("json2.js");
var print_r = function(obj) {
	var str = JSON.stringify(obj);
	print(str);
};

///////////////////////////////
// Assert (builtin in language)
var assert = function (ok, str) {
	if(!ok) {
		throw "Assert error: " + str;
	}
}

/////////////////////////////////////////////////////
// Function that defines an iterator across a string or array (builtin)
// iteration can be done like:
//
// var iter = iterator(foo);
// while((elem = iter()) !== undefined) {
//     ...
// }
//
var iterator = function(set) {
	var pos = -1;
	assert(typeof(set) === "string" || set instanceof Array);
	return function() {
		pos = pos + 1;
		return set[pos];
	}
}

var empty_iterator = function() { return undefined; };

//
var getch = function() {
	// "" instead of readline() avoids reading from stdin,
	// when getch is not used. On the other hand, this inserts
	// an empty line in the beginning of stdin.
	//
	//var iter = iterator(readline());
	var iter = iterator(""); 
	var emptycount = 0;
	return function() {
		// no eof symbol, so 
		// if 10 empty lines occur, assume eof
		if(emptycount > 10) {
			return undefined;
		}
		var c = iter();
		if(c === undefined) {
			iter = iterator(readline());
			emptycount = emptycount + 1;
			return "\n";
		} else {
			emptycount = 0;
			return c;
		}
	}
} ();

//////////////////////////////////////////
// Copy the content of an object onto another object
var appendObject = function(dst, src) {
        for(elem in src) {
                dst[elem] = src[elem];
        }
}


/////////////////////////////////////////////////////////
// Add a filter to an iterator, such that elements
// where predicate is true is removed

var filter = function(predicate, iter) {
        return function() {
                var result;
                do {
                        result = iter();
                } while (result !== undefined && predicate(result));
                return result
        }
}
