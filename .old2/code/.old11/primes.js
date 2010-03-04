var i = 2, j;
var primes = [];
var prime;
while(i<1000) {
	prime = true;
	j = 0;
	while(j < length(primes) && prime) {
		prime = prime && ((i % primes[j]) !== 0);
		j = j + 1;
	}
	if(prime) {
		push(primes, i);
	}
	i = i + 1;
}
println(primes);
