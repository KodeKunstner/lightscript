f = function() {
    throw "blah";
}
try {
    f();
} catch(exp) {
    print(exp);
}
