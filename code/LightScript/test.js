print(1/3);
x = {"foo": 1, "bar": 2};
for(i in x) {
        print(i + x[i]);
}

a = {"foo": undefined, "bar": true, "baz": true};
print(a);
a = {"foo": 1, "bar": true, "baz": undefined};
print(a);
a = {"foo": 1, "bar": 2};
print(a);

for(x in a) {
    print(x + a[x]);
}
