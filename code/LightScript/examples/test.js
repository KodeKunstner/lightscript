function g() {
    print(this);
}
print(this);
o = {"g":g};
g();
o.g();

x = {"foo":123, "bar":33};
print(x);
delete x.foo;
print(x);
