function erasthones(n) {
    sieve = [];
    for(i = 2; i <= n; ++i) {
        sieve[i] = 1;
    }
    for(i = 2; i <= n;) {
        for(j = i * 2; j <= n; j += i) {
            sieve[j] = 0;
        }
        ++i;
        while(i <= n && !sieve[i]) {
            ++i;
        }
    }
    count = 0;
    for(i = 2; i <= n; ++i) {
        if(sieve[i] === 1) {
            ++count;
        }
    }
    print(count);
}

erasthones(300000);
