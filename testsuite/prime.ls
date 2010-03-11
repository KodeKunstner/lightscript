test('
function isPrime(x) {
    for(d = 2; d < x; ++d) {
        if((x % d) === 0 ) {
            return false;
        }
    }
    return true;
}

count = 0;
for(i = 10000; i< 10100; ++i){
    if(isPrime(i)) {
        ++count;
    }
}
count', 11);
