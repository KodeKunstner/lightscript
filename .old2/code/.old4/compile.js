#!/usr/bin/js

load("stdmob.js");
load("parser.js");
load("backend_vm.js");

nodes = [];
acc = [];
node = parser.parse();
while (node) {
	node2vm(false, node, acc);
	node = parser.parse();
	arrpush(nodes, node);
};

var result = {"locals": locals, "literals": literalarray, "code": acc, "nodes": nodes};
result.code2 = [];
for(i in result.code) {
	result.code2[i] = result.code[i];
	t = opcode[result.code[i]];
	if(t !== undefined) {
		result.code[i] = t;
	}
}
std.io.println(result);
