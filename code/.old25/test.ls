5*5+4*4;

fib = function(x) {
    print(strjoin("fib", x));
    if(1 < x) {
        return fib(x-1) + fib(x-2);
    } else {
        return 1;
    }
}
