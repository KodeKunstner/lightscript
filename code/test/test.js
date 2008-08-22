a = 1;
b = 1;
t = 0
print("Fibonaccital under 1000:");
while(a<1000) {
	print(a);
	t = b;
	b = b + a;
	a = t;
}
