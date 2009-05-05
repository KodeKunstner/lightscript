x = {};
x.a = function() { print(this); };
x.a();

print("Hello");

function f(j) {
    var i;
    for(i = 0; i < j; ++i) {
        print(i);
    }
}

x = [3, 6, 2, 3, 7, 1, 6, 4, 0, 9];
function c(a, b) { return a - b; }
function test() {
    print("ABC");
    print(this);
    return "HERE";
}
x.prototype.test = test;

print(x);
print(x.test());
print(x.sort(c));
