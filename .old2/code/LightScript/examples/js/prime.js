function isPrime(x) {
    for(d = 2; d < x; ++d) {
        if((x % d) === 0 ) {
            return false;
        }
    }
    return true;
}

for(i = 9999990; i< 10000020; ++i){
    if(isPrime(i)) {
        print(i);
    }
}
