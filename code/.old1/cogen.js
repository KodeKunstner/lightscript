var op, functions;

nextconst = 0;
strconsts = {};
numconsts = {};
consttable = [];
code = [];

ops = [
["getconst", 100],
["callglobal", 101],
["getglobal", 102],
["jump", 103],
["condjump", 104],
["putglobal", 105],
["popn", 106],
];

op = {};

iter = iterator(ops);

while(iter.next()) {
	op[iter.val[0]] = iter.val[1];
};

newconst = function(obj) {
	if(typeof(obj) === "number") {
		numconsts[obj] = nextconst;
	} else {
		strconsts[obj] = nextconst;
	}
	nextconst = nextconst + 1;
	consttable.push(obj);
};

writeconst = function(obj) {
	var consts;
	if(typeof(obj) === "number") {
		consts = numconsts;
	} else {
		consts = strconsts;
	}

	if(consts[obj] === undefined) {
		newconst(obj);
	}
	code.push(consts[obj]);
};

noop = function(obj) {};
cogens = {
	"(comment)": noop,
	"(codeblock)": function(obj) {
		var iter;
		iter = iterator(obj.args);
		while(iter.next()) {
			cogen(iter.val);
		}
		code.push(op.popn);
		code.push(obj.args.length - 1);
	},
	"var": noop,
	"(call)": function(obj) {
		var iter;
		iter = iterator(obj.args);
		iter.next();
		while(iter.next()) {
			cogen(iter.val);
		}
		cogen(obj.args[0]);
		code.push(op.callglobal);
		code.push(obj.args.length - 1);
	},
	"(literal)": function(obj) {
		code.push(op.getconst);
		writeconst(obj.val);
	},
	"(identifier)": function(obj) {
		// TODO: currently only get global values.
		code.push(op.getglobal);
		writeconst(obj.val);
	},
	"while": function(obj) {
		var jumpadr_pos, startpos;
		startpos = code.length - 1;
		cogen(obj.args[0]);
		code.push(op.condjump);
		jumpadr_pos = code.length;
		code.push(0);
		cogen(obj.args[1]);
		code.push(op.jump);
		code.push(startpos - code.length);
		code[jumpadr_pos] = code.length - jumpadr_pos - 1;
	},
	"if": function(obj) {
		var jumpadr_pos;
		cogen(obj.args[0]);
		code.push(op.condjump);
		jumpadr_pos = code.length;
		code.push(0);
		cogen(obj.args[1]);
		code[jumpadr_pos] = code.length - jumpadr_pos - 1;
		/* if-else */
		if(obj.args === 3) {
			code[jumpadr_pos] = code[jumpadr_pos] + 2;
			code.push(op.jump);
			jumpadr_pos = code.length;
			code.push(0);
			cogen(obj.args[2]);
			code[jumpadr_pos] = code.length - jumpadr_pos - 1;
		}
	},
	"=": function(obj) {
		// TODO: currently only set global values.
		cogen(obj.args[1]);
		code.push(op.putglobal);
		writeconst(obj.args[0].val);
	},
};

cogen_args =  function(node) {
	map(cogen, node.args);
};

cogen = function(node) {
	//print_r(["NODE", node]);
	if(!node.sep) {
		cogens[node.id](node); 
	}
};


gencode = function(obj) {
};


