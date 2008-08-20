var macros = {
	"(default)": function() {},
};

var compile = function(node) {
	return (macros[node] || macros["(default)"])(node);
}; 

while(node = parse()) {
	print_r(node);
	print_r(compile(node));
	print("\n");
}
