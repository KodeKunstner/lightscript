test("{'foo': 123}.foo", 123);
test("a = {'foo': 113}; a['foo']", 113);
test("x = Object.create(a); x.foo", 113);
test("a.foo = 123; x.foo", 123);
test("x.foo = 117; x.foo", 117);
test("x.foo = 117; x.foo", 117);
test("a.boo = 111; x.boo", 111);
test("x.hasOwnProperty('boo')", false);
test("x.hasOwnProperty('foo')", true);
test('a = ""; for(b in {"a":1, "b":2, "c":3}) a+=b; a', "bac"); // LightScript Objects do not preserve order of elements
