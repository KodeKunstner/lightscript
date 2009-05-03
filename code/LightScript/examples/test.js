function g() {
    print(this);
}
print(this);
o = {"g":g};
g();
o.g();
