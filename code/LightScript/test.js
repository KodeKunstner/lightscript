function fib(x) {
    if(x < 2) {
        return 1;
    } else {
        return fib(x-1) + fib(x-2);
    }
}

print("Hello world");

for(i=0;i<100;++i) {
    print("fib("+i+") = "  + fib(i));
}
