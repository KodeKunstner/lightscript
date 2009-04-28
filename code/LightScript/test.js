var x;
function f(x, y, z) {
    var a, b, c, d, e;
    print(x);
    print(y);
    print(z);
}

f(3, 4);
f.prototype.hello = 4;
function g() { };
f.blah = 17;
x = {};
x.prototype.foo = 123;
print("f.length: " + f.length);
print("f.hello: " + f.hello);
print("f.blah: " + f.blah);
print("obj-foo: " + ({}).foo);
print("x.class" + x.class);
print("f.length" + f.length);
for(i=0;i<10;++i) {
    print(i);
}
y = {};
print(y);
print(({}).prototype);
