var i = 2, j = 0;
var primes = [];
var prime = true;
while(i<1000) {
	j = 0;
	while(j < length(primes) && prime) {
	println("here");
		println(j);
		println(length(primes));
		prime = prime && ((i % primes[j]) !== 0);
		j = j + 1;
	}
	if(prime) {
		push(primes, i);
	}
	i = i + 1;
}
println(primes);
