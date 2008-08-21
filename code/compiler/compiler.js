macro_args =  function(node) {
	map(run_macro, node.args);
}

run_macro = function(node) {
	(macros[node.id]||macro_args)(node); 
}

macros = {
	".": function(obj) {
		obj.args[1].id = "(literal)";
		obj.id = "(subscript)";
		macro_args(obj);
	},
	"apply[": function(obj) {
		obj.id = "(subscript)";
		macro_args(obj);
	},
	"apply(": function(obj) {
		if(obj.args[0].id === ".") {
			obj.id = "(methodcall)";
			obj.method = obj.args[0].args[1].val;
			obj.args[0] = obj.args[0].args[0];
		} else {
			obj.id = "(call)";
		}
		macro_args(obj);
	},
	"while": function(obj) {
		obj.args[0].id = "(condition)";
		obj.args[1].id = "(codeblock)";
		macro_args(obj);
	},
	"if": function(obj) {
		obj.args[0].id = "(condition)";
		obj.args[1].id = "(codeblock)";
		if(obj.args[2]) {
			obj.args[2].id = "(codeblock)";
		}
		macro_args(obj);
	},
	"function": function(obj) {
		/* TODO: add scope resolution*/
		obj.args[0].id = "var";
		obj.args[1].id = "(codeblock)"
		macro_args(obj);
	},
	"var": function(obj) {
		/* TODO: add scope resolution*/
		macro_args(obj);
	},
	"=": function(obj) {
		/* TODO: add scope resolution*/
		macro_args(obj);
	},
	"identifier": function(obj) {
		/* TODO: add scope resolution*/
		macro_args(obj);
	},
}

while(iter.next()) {
	run_macro(iter.val);
	print_r(iter.val);
	print();
}

foo["x"].bar(1, 2,3);
bar(1, 2, 3);
