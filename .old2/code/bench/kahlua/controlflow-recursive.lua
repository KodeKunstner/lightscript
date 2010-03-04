function ack(m,n)
   if m==0 then return n+1 end
   if n==0 then return ack(m-1,1) end
   return ack(m-1, ack(m,n-1))
end

function fib(n) 
    if n < 2 then return 1 end
    return fib(n-2) + fib(n-1)
end

function tak(x,y,z) 
    if y >= x then return z end
    return tak(tak(x-1,y,z), tak(y-1,z,x), tak(z-1,x,y))
end

for i = 3, 7 do
    print(ack(3,i))
    print(fib(17+i))
    print(tak(3*i+3,2*i+2,i+1))
end
