var op, functions;

nextconst = 0;
consts = {};
code = [];

ops = [
["getconst", 0, "followed by const-id, pops none, push 1"],
["callglobal", 1, "followed by number-of-arguments and global-id as const-id, pops number of arguments, push one"],
];

op = {};

iter = iterator(ops);

while(iter.next()) {
	op[iter.val[0]] = iter.val[1];
};

newconst = function(obj) {
	consts[obj] = nextconst;
	nextconst = nextconst + 1;
};

writeconst = function(obj) {
	if(consts[obj] === undefined) {
		newconst(obj);
	}
	code.push(consts[obj]);
};

cogens = {
	"(call)": function(obj) {
		// TODO: currently only call const functions, fix this.
		var iter;
		iter = iterator(obj.args);
		iter.next();
		while(iter.next()) {
			cogen(iter.val);
		}
		code.push(op.callglobal);
		writeconst(obj.args[0].val);
		code.push(obj.args.length - 1);
	},
	"(literal)": function(obj) {
		code.push(op.getconst);
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




iter = parser(getch);

while(iter.next()) {
	functions = run_macro(iter.val);
	//print_r(iter.val);
	cogen(iter.val);
	//print();
}
//print_r(functions);
//print_r(ops);
//print_r(op);

consttable = [];
iter = iterator(consts);
while(iter.next()) {
	consttable[iter.val] = iter.key;
}

print_r({"consts": consttable, "code": code});

