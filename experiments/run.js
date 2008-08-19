#!/usr/bin/js

load("stdmob.js");
load("parser.js");

parse = parser(getch);

while(node = parse()) {
	print(JSON.stringify(node));
}
