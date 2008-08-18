#!/usr/bin/js

load("stdmob.js");
load("tokeniser.js");
load("parser.js");
parse = parser(tokeniser(getch));

while((x = parse(0)) !== undefined) {
	        print_r(["output:", x]);
}

