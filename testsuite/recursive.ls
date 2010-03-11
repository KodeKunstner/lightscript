test(" function fib(x) {
        if(1 < x) {
            return fib(x - 1) + fib(x - 2);
        } else {
            return 1;
        }

    }; fib(20);", 10946);
// The Computer Language Shootout
// http://shootout.alioth.debian.org/
// contributed by Isaac Gouy
//
// modified for LightScript

test('
function ack(m,n){
   if (m===0) { return n+1; }
   if (n===0) { return ack(m-1,1); }
   return ack(m-1, ack(m,n-1) );
}

function fib(n) {
    if (n < 2){ return 1; }
    return fib(n-2) + fib(n-1);
}

function tak(x,y,z) {
    if (y >= x) return z;
    return tak(tak(x-1,y,z), tak(y-1,z,x), tak(z-1,x,y));
}

acc = [];
for ( var i = 1; i <= 4; ++i ) {
    acc.push(ack(3,i));
    acc.push(fib(13+i));
    acc.push(tak(3*i+3,2*i+2,i+1));
}; acc.join()', "13,610,3,29,987,6,61,1597,5,125,2584,10");
