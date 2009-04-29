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

obj = ["foo", 1, "bar",2];
print("HERE!!!!!");
iter = obj.__create_iterator();
while((elem = iter()) != undefined) {
    print("Elem: " + elem);
}

a = {"foo": "bar"};
b = {"prototype": a};
print("b.foo: " + b.foo);


print(1 + 1 + "" + 1 + 1);
a = 1;
b = 2;
c = 3;
a = b = c;
print("a:"+a+" b:"+b);

print("Hello") || true || true;
print(1 - 1 - 1);
