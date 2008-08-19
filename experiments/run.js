#!/usr/bin/js

load("stdmob.js");
load("parser.js");

parse = parser(getch);

load("compiler.js");

while(node = parse()) {
	print(JSON.stringify(node));
}
