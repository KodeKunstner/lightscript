function isPrime(x) 
    for d = 2, x - 1 do
        if (x % d) == 0 then
            return false
        end
    end
    return true;
end

for i = 9999990, 10000019 do
    if isPrime(i) then
        print(i)
    end
end
