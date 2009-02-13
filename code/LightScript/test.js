for(x in {"a":1, "b": 2}) {
    print(x);
}
print(x);
x = 0;
do {
    print("Hello");
    x = x + 1;
} while(x < 10);
print(null + null);
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
