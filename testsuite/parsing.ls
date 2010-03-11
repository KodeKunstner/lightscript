test("f = function (a, b, c) { return b }; f(1, -2, 4) ", -2);
test("i = 1; f(a, ++i, b)", 2);
test("true?1:2", 1);
test("false?1:2", 2);
test("x = 1; (function() { var a = 2, x = 5; 4})(); x", 1);
test("var f = function () { }");
// test("i = 1; f(a, i++, b)", 1); // postfix++ currently not supported
