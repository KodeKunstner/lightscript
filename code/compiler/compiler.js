
var noop = function() {
}

code = [];
var macros = {
	"comment": noop,
	";": noop,
	",": noop,
	"=": function(stmt) {
		compile(stmt[2]);
		compile(stmt[1]);
		code.push("pop ref, pop val, store val in ref");
	},
	"literal": function(stmt) {
		code.push("push "+ stmt[1]+ " to stack");
	},
	"ident": function(stmt) {
		code.push("lookup "+ stmt[1]+ " and push ref to stack");
	},
	"var": function(stmt) {
		var id;
		if(stmt[1][0] === "ident") {
			id = stmt[1][1];
		} else {
			id = stmt[1][1][1];
		}
		code.push(id + " added to scope");
		if(stmt[1][0] !== "ident") {
			compile(stmt[1]);
		}
	}
}

var compile = function(stmt) {
	if(macros[stmt[0]]) {
		macros[stmt[0]](stmt);
	} else {
		code.push("unknown op: " + stmt[0]);
	}
}
var docompile = function(stmt) {
	code = [];
	compile(stmt);
	return code;
}


var x;

macros;

x = "hello";

while(node = parse()) {
	print_r(node);
	print(docompile(node).join("\n"));
	print();
}
