function f(x) { throw x + 1; };

i = 0;
while(i<500000) {
    try {
        f(i);
    } catch(e) {
        i = e;
    }
}

