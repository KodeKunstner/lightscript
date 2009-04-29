(function() {
 var x = 0;
 while(x < 100) {
 print(x);
 x = x + 1;
 }
 print( 1 && 2 && 3 && 4 && false || null || undefined || "HELLO");

 print({
     "foo": 1,
     "bar": 2,
     "baz": 3});

 var x = 2;
 if(x === 1) {
 print("foo");
 } else if(x === 2) {
 print("boo");
 } else if(x === 3) {
 print("moo");
 } else {
 print("poo");
 }
})();
