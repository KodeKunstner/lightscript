#!/usr/bin/js

load("stdmob.js");
load("parser.js");
load("backend_js.js");
load("yolan.js");

s = parser.parse();
while(s !== undefined) {
	std.io.println(arrjoin(yolan.print(s, []), ""));
	s = parser.parse();
}
std.io.println(backend_js.toJS(parser.parse));
