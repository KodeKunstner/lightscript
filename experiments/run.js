#!/usr/bin/js

load("stdmob.js");
load("parser.js");

parse = parser(getch);

while((x = parse(0)) !== undefined) {
	        print_r(["output:", x]);
}

