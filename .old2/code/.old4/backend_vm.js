argc = 0;
locals = {};
nextlocal = 0;
localid = (function (val) {
	id = locals[val];
	if ((id === undefined)) {
		id = nextlocal;
		nextlocal = (nextlocal + 1);
		locals[val] = id;
	};
	return id;
});
literals = {};
literalarray = [];
nextliteral = 0;
literalid = (function (val) {
	id = literals[val];
	if ((id === undefined)) {
		id = nextliteral;
		literalarray.push(val);
		nextliteral = (nextliteral + 1);
		literals[val] = id;
	};
	return id;
});
opcode = {
	"arrpush": 0,
	"arrjoin": 1,
	"arrpop": 2,
	"jump": 3,
	"eq": 4,
	"getlocal": 5,
	"global_get": 6,
	"global_set": 7,
	"iadd": 8,
	"isub": 9,
	"leq": 10,
	"lt": 11,
	"multiget": 12,
	"multiput": 13,
	"neg": 14,
	"neq": 15,
	"not": 16,
	"object_put": 17,
	"pop": 18,
	"push_emptyarray": 19,
	"push_emptyobject": 20,
	"pushfalse": 21,
	"pushliteral": 22,
	"pushnil": 23,
	"condjumpfalse": 24,
	"pushtrue": 25,
	"return": 26,
	"setlocal": 27,
};
node2vm = (function (withresult, elem, acc) {
	var i, jumpadr_pos, startpos;
	if ((elem.id === "(string literal)")) {
		arrpush(acc, "pushliteral");
		arrpush(acc, literalid(elem.val));
	} else if ((elem.id === "(integer literal)")) {
		arrpush(acc, "pushliteral");
		arrpush(acc, literalid(parseInt(elem.val, 10)));
	} else if ((elem.id === "(nil)")) {
		arrpush(acc, "pushnil");
	} else if ((elem.id === "(true)")) {
		arrpush(acc, "pushtrue");
	} else if ((elem.id === "(false)")) {
		arrpush(acc, "pushfalse");
	} else if ((elem.id === "(noop)")) {
		withresult = true;
	} else if ((elem.id === "(if)")) {
		node2vm(true, elem.args[0], acc);
                arrpush(acc, "condjumpfalse");
                jumpadr_pos = acc.length;
                arrpush(acc, 0);
		node2vm(false, elem.args[1], acc);
		// else
		if(elem.args[2] !== undefined) {
                	arrpush(acc, "jump");
                	startpos = acc.length;
                	arrpush(acc, 0);
		}
                acc[jumpadr_pos] = acc.length - jumpadr_pos - 1;
		// else
		if(elem.args[2] !== undefined) {
			node2vm(false, elem.args[2], acc);
                	acc[startpos] = acc.length - startpos - 1;
		}
		if (withresult) {
			arrpush(acc, "pushnil");
		};
		withresult = true;
	} else if ((elem.id === "(block)")) {
		for(i in elem.args) {
			node2vm(false, elem.args[i], acc);
		}
		if (withresult) {
			arrpush(acc, "pushnil");
		};
		withresult = true;
	} else if ((elem.id === "(object literal)")) {
		arrpush(acc, "push_emptyobject");
		i = 0;
		while ((i < elem.args.length)) {
			node2vm(true, elem.args[i], acc);
			node2vm(true, elem.args[(i + 1)], acc);
			arrpush(acc, "object_put");
			i = (i + 2);
		};
	} else if ((elem.id === "(array literal)")) {
		arrpush(acc, "push_emptyarray");
		for (i in elem.args) {
			node2vm(true, elem.args[i], acc);
			arrpush(acc, "arrpush");
		};
	} else if ((elem.id === "(iter)")) {
		node2vm(true, elem.args[i], acc);
		arrpush(acc, "getiter");

//TODO
	} else if ((elem.id === "(while)")) {
                startpos = acc.length - 1;
		node2vm(true, elem.args[0], acc);
                arrpush(acc, "condjumpfalse");
                jumpadr_pos = acc.length;
                arrpush(acc, 0);
		node2vm(false, elem.args[1], acc);
                arrpush(acc, "jump");
                arrpush(acc, startpos - acc.length);
                acc[jumpadr_pos] = acc.length - jumpadr_pos - 1;
		if (withresult) {
			arrpush(acc, "pushnil");
		};
		withresult = true;
	} else if ((elem.id === "(and)")) {
		node2vm(true, elem.args[0], acc);
                arrpush(acc, "getlocal");
                arrpush(acc, 1);
                arrpush(acc, "condjumpfalse");
                jumpadr_pos = acc.length;
                arrpush(acc, 0);
                arrpush(acc, "pop");
		node2vm(true, elem.args[1], acc);
                acc[jumpadr_pos] = acc.length - jumpadr_pos - 1;
	} else if ((elem.id === "(or)")) {
		node2vm(true, elem.args[0], acc);
                arrpush(acc, "getlocal");
                arrpush(acc, 1);
                arrpush(acc, "not");
                arrpush(acc, "condjumpfalse");
                jumpadr_pos = acc.length;
                arrpush(acc, 0);
                arrpush(acc, "pop");
		node2vm(true, elem.args[1], acc);
                acc[jumpadr_pos] = acc.length - jumpadr_pos - 1;
	} else if ((elem.id === "(local)")) {
		arrpush(acc, "getlocal");
		arrpush(acc, (nextlocal - localid(elem.val)));
	} else if ((elem.id === "(global)")) {
		arrpush(acc, "global_get");
		arrpush(acc, literalid(elem.val));
	} else if ((elem.id === "(setglobal)")) {
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "global_set");
		arrpush(acc, literalid(elem.args[0].val));
		if (withresult) {
			arrpush(acc, "pushnil");
		};
		withresult = true;
	} else if ((elem.id === "(setlocal)")) {
		node2vm(true, elem.args[1], acc);
		i = (nextlocal - localid(elem.args[0].val));
		if ((i !== 0)) {
			arrpush(acc, "setlocal");
			arrpush(acc, i);
		};
		if (withresult) {
			arrpush(acc, "pushnil");
		};
		withresult = true;
	} else if ((elem.id === "(subscript)")) {
		node2vm(true, elem.args[1], acc);
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "multiget");
	} else if ((elem.id === "(put)")) {
		node2vm(true, elem.args[2], acc);
		node2vm(true, elem.args[1], acc);
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "multiput");
		if (withresult) {
			arrpush(acc, "pushnil");
		};
		withresult = true;
	} else if ((elem.id === "(function)")) {
//	TODO
//
	} else if ((elem.id === "(function call)")) {
// Calling convention:
//  fncall(argn):
//  args..., this, fn, argc -> result
		if(elem.args[0].id === "(subscript)") {
			//get the arguments
			i = 1;
			while(i < elem.args.length) {
				node2vm(true, elem.args[i], acc);
				i = i + 1;
			}

			// get the this-object
			node2vm(true, elem.args[0].args[0], acc);
			// get the function
			node2vm(true, elem.args[0].args[1], acc);
			//   duplicate elem.args[0].args[0] 
			arrpush(acc, "getlocal");
			arrpush(acc, 2);
			// get the function
			arrpush(acc, "multiget");
			// call the function with i arguments
			arrpush(acc, "fncall");
			arrpush(acc,  elem.args.length - 1);
		} else if (elem.args[0].val === "arrjoin") {
			node2vm(true, elem.args[1], acc);
			node2vm(true, elem.args[2], acc);
			arrpush(acc, "arrjoin");
		} else if (elem.args[0].val === "arrpush") {
			node2vm(true, elem.args[2], acc);
			node2vm(true, elem.args[1], acc);
			arrpush(acc, "arrpush");
		} else {
			// push arguments
			i = 1;
			while(i < elem.args.length) {
				node2vm(true, elem.args[i], acc);
				i = i + 1;
			}
			// push empty this
			arrpush(acc, "pushnil");
			node2vm(true, elem.args[0], acc);
			// call the function with i arguments
			arrpush(acc, "fncall");
			arrpush(acc,  elem.args.length - 1);
		}
	} else if ((elem.id === "(return)")) {
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "return");
		arrpush(acc, nextlocal);
		withresult = true;
	} else if ((elem.id === "(sign)")) {
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "neg");
	} else if ((elem.id === "(plus)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "iadd");
	} else if ((elem.id === "(minus)")) {
		node2vm(true, elem.args[1], acc);
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "isub");
	} else if ((elem.id === "(equals)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "eq");
	} else if ((elem.id === "(not equals)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "neq");
	} else if ((elem.id === "(not)")) {
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "not");
	} else if ((elem.id === "(less)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "lt");
	} else if ((elem.id === "(less or equal)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "leq");
	} else if ((elem.id === "(array pop)")) {
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "arrpop");
	} else if ((elem.id === "(this)")) {
		arrpush(acc, "getlocal");
		arrpush(acc, nextlocal + argc + 1);
	} else {
		std.io.printerror(strcat("Unknown id: ", elem.id));
		std.io.printerror(elem);
	};
	if ((!withresult)) {
		arrpush(acc, "pop");
	};
	return acc;
});

