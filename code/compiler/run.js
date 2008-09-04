#!/usr/bin/js

load("stdmob.js");
load("parser.js");
load("backend_js.js");

std.io.println(backend_js.toJS(parser.parse));
