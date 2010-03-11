test('"a" + "b"', "ab");
test('"a" + 1', "a1");
test('1 + "b"', "1b");
test('(undefined).toString()', "undefined");
test('true.toString()', "true");
test('"foo".toString()', "foo");
test('"abcde".slice(2,4)', "cd");
test('"abcde".slice(-3,-1)', "cd");
test('"abcde".slice(0)', "abcde");
