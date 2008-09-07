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
	"array_put": 0,
	"arrjoin": 1,
	"arrpop": 2,
	"arrput": 3,
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
	"???": 24,
	"pushtrue": 25,
	"return": 26,
	"setlocal": 27,
	"strcat": 28,
};
node2vm = (function (withresult, elem, acc) {
	var i;
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


//TODO
	} else if ((elem.id === "(block)")) {


//TODO
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
			arrpush(acc, "array_put");
		};
	} else if ((elem.id === "(for)")) {


//TODO
	} else if ((elem.id === "(while)")) {


//TODO
	} else if ((elem.id === "(and)")) {


//TODO
	} else if ((elem.id === "(or)")) {


//TODO
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
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "multiget");
	} else if ((elem.id === "(put)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		node2vm(true, elem.args[2], acc);
		arrpush(acc, "multiput");
	} else if ((elem.id === "(function)")) {


//	TODO
//
	} else if ((elem.id === "(function call)")) {


//	TODO
//
	} else if ((elem.id === "(delete)")) {


//	TODO
//
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
	} else if ((elem.id === "(string concat)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "strcat");
	} else if ((elem.id === "(array join)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "arrjoin");
	} else if ((elem.id === "(array push)")) {
		node2vm(true, elem.args[0], acc);
		node2vm(true, elem.args[1], acc);
		arrpush(acc, "arrput");
	} else if ((elem.id === "(array pop)")) {
		node2vm(true, elem.args[0], acc);
		arrpush(acc, "arrpop");
	} else if ((elem.id === "(this)")) {


//	TODO
//
	} else {
		std.io.printerror(strcat("Unknown id: ", elem.id));
		std.io.printerror(elem);
	};
	if ((!withresult)) {
		arrpush(acc, "pop");
	};
	return acc;
});

