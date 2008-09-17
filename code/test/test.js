fib = function(n) {
	if(n<=2) {
		hello();
		return 1;
	} else {
		return fib(n-1) + fib(n-2);
	}
}
hello = function() {
	println("hello");
}
var i = 1;
while(i<=4) {
	println(fib(i));
	i = i + 1
}

