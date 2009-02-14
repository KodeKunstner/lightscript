x = [1, 2, 3, 4, 5, 6, 7];
print(x);
print(x.join("-"));
for(x in {"a":1, "b": 2}) {
    print(x);
}
print(x);
x = 0;
do {
    x += 2;
    x -= 1;
    print(x);
} while(10 > x);
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
++x;
