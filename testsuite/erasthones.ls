test('function erasthones(n) {
    sieve = [];
    for(i = 2; i <= n; ++i) {
        sieve[i] = true;
    }
    for(i = 2; i <= n;) {
        for(j = i * 2; j <= n; j += i) {
            sieve[j] = false;
        }
        ++i;
        while(i <= n && !sieve[i]) {
            ++i;
        }
    }
    count = 0;
    for(i = 2; i <= n; ++i) {
        if(sieve[i]) {
            ++count;
        }
    }
    return count;
}; erasthones(20000)', 2262);
