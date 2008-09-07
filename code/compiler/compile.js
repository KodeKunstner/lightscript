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
};

var result = {"locals": locals, "literals": literalarray, "code": acc};
for(i in result.code) {
	t = opcode[result.code[i]];
	if(t !== undefined) {
		result.code[i] = t;
	}
}
std.io.println(result);
