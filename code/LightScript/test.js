print(1/3);
x = {"foo": 1, "bar": 2};
for(i in x) {
        print(i + x[i]);
}

a = {"foo": null, "bar": false, "baz": undefined};
print(a);
a = {"foo": 1, "bar": true, "baz": undefined};
print(a);
a = {"foo": 1, "bar": 2};
print(a);

for(x in a) {
    print(x + a[x]);
}

j = 0;
for(i=0;i<100;i++) {
    j += i;
}
print(j);
