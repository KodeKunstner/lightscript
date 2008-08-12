#!/usr/bin/js

load("simple-json.js");

report = parse_sjs(getch)();
print(report["header"]);

print(report["footer"]);
