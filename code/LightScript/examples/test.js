function g() {
    print(this);
}
o = {"g":g};
g();
o.g();
