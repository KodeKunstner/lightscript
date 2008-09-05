#!/usr/bin/js

load("stdmob.js");
load("parser.js");
load("backend_vm.js");

nodes = [];
node = parser.parse();
while ((node !== undefined)) {
	std.io.println("");
	std.io.println(node);
	std.io.println({"locals": locals, "literals": literals});
	std.io.println(node2vm(true, node, []));
	node = parser.parse();
};
