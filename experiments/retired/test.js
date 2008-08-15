#!/usr/bin/js

// require standard mobyscript library
load("simple-json.js");

next = parse_sjs(iterator("   blob [  foo { hoocha boocha} bar baz]"));



	print_r(next());
	print_r(next());
	print_r(next());

print_r(["\\", "\"foo\"", "[123]", "afge", "grs"]);

str = "";
while((c = getch())  !== undefined) {
	str = str + c;
}
print(str);
