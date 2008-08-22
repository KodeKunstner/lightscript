var op, functions;

nextconst = 0;
strconsts = {};
numconsts = {};
consttable = [];
code = [];

ops = [
["getconst", 0],
["callglobal", 1],
["getglobal", 2],
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

};

cogen_args =  function(node) {
	map(cogen, node.args);
};

cogen = function(node) {
	if(!node.sep) {
		cogens[node.id](node); 
	}
};


gencode = function(obj) {
};


