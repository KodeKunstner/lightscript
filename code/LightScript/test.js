try {
    f = function(x, y, z) {
                    print(x);
                    print(y);
                    print(z);
                    return y;
                };
    f("foo", 1, 2);
    print(Main(f , 123, 123));
} catch(exp) {
    print(exp);
}
