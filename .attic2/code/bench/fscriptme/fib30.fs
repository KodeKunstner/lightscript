func fib(int x) 
    if 1<x
        return fib(x-1)+fib(x-2)
    else 
        return 1
    endif
endfunc
println(fib(30))
