test("this.x = 3", this);
test("this.x", 3);
test("x = 6; this.x", 6);
test("(function() { return 7; })()", 7);
test("x = 9; (function() { return this.x; })()", 9);
