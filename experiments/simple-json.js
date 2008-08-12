// require standard mobyscript library
load("stdmob.js");

///////////////
// function that, given a string iterator 
// returns an iterator of javascript objects
// parsed from the string as simplified json
//
var parse_sjs = function(iter) {

	var current = iter();

	var ws = " \n\r\t";
	var rparen = "]}";
	var symbs = ws + rparen + "[{";

	var next = function() {
		current = iter();
	}

	/* return whether ch is contained in the symbol set */
	var oneof = function(symbs) {
		symbiter = iterator(symbs);
		while((symb = symbiter()) !== undefined) {
			if (current === symb) {
				return true;
			}
		}
		return false;
	}

	var next = function() {
		current = iter();
	}

	var nextObject = function() {

		var result, key, val;

		// Skip whitespace
		while (oneof(ws)) {
			next();
		}

		// Beginning of list?
		if (current === "[") {
			next();
			result = [];
			val = nextObject();
			while(val !== undefined) {
				result.push(val);
				val = nextObject();
			}

		// Beginning of object?
		} else if (current === "{") {
			next();
			result = {};
			key = nextObject();
			while(key !== undefined) {
				val = nextObject();
				assert(val !== undefined);
				result[key] = val;
				key = nextObject();
			}

		// Beginning of quoted string?
		} else if (current === "\"") {
			next();
			result = "";
			while (current !== "\"" && current !== undefined) {
				// Escaped symbol
				if (current === "\\") {
					next();
				}
				result = result + current;
				next();
			}
			next();

		// End of parenthesis/string?
		} else if (oneof(rparen) || current === undefined) {
			next();
			result = undefined;

		// Some atom not needing escaping
		} else {
			result = "";
			while (!oneof(symbs)) {
				result = result + current;
				next();
			}
		}
		return result;
	}

	return nextObject;
} ;
