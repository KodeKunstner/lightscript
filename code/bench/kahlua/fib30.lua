function fib(x) 
   if 1 < x then 
      return fib(x - 1) + fib(x - 2)
   else 
      return 1
   end
end

print(fib(30))
