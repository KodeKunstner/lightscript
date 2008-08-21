var run_macro;
run_macro = function(obj) {
	var macro_args, macros, run_macro, fn, fn_stack, functions, new_id;
	
	new_id = (function() {
		var id = 0;
		return function(str) {
			str = str || "id";
			id = id + 1;
			return str + "_" + id;
		}
	} ());
	
	
	fn = {
		"vars": {},
		"fid": "globals",
	};
	
	functions = { "globals": fn }
	
	fn_stack = [];
	
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
			obj.args[0].id = "var";
			obj.args[1].id = "(codeblock)"
			fn_stack.push(fn);
			fn = {  "fid": new_id("fn"), 
				"vars": {},
				//"obj": obj,
			}
			obj.fid = fn.fid;
			functions[fn.fid] = fn;
	
			macro_args(obj);
	
			fn_stack.pop(fn);
		},
		"var": function(obj) {
			var iter, name;
	 		iter = iterator(obj.args);
			while(iter.next()) {
				// the name of the variables
				name = iter.val.val;
				fn.vars[name] = "local";
			}
			macro_args(obj);
		},
		"=": function(obj) {
			macro_args(obj);
		},
		"identifier": function(obj) {
			var iter, scope, name;
	
			name = obj.val;
			obj.scope = "inner";
			if(!fn.vars[name]) {
				obj.scope = "globals";
				iter = iterator(fn_stack);
				while(iter.next()) {
					if(iter.val.vars[name]) {
						obj.scope = iter.val.fid;
					}
				}
				functions[obj.scope].vars[name] = "shared";
			}
			macro_args(obj);
		},
	}
	
	run_macro(obj);
	return functions;
}
