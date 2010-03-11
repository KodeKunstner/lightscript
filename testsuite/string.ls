test('"a" + "b"', "ab");
test('"a" + 1', "a1");
test('1 + "b"', "1b");
test('(undefined).toString()', "undefined");
test('true.toString()', "true");
test('"foo".toString()', "foo");
