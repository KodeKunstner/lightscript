#!/usr/bin/js

load("stdmob.js");
load("parser.js");
load("macro.js");
load("cogen.js");



iter = parser(getch);

while(iter.next()) {
        //functions = run_macro(iter.val);
        print_r(iter.val);
        //cogen(iter.val);
        print();
}
//print_r(functions);
//print_r(ops);
//print_r(op);

print_r({"consts": consttable, "code": code});

