function erasthones(n)
    sieve = {}
    for i = 2, n do
        sieve[i] = true
    end
    for i = 2, n do
        while i <= n and not sieve[i] do
            i = i + 1
        end
        for j = i * 2, n, i do
            sieve[j] = false
        end
    end
    count = 0
    for i = 2, n do
        if sieve[i] then
            count = count + 1
        end
    end
    print(count)
end

erasthones(300000)
