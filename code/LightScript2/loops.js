(function() {
    var x = 0;
    while(x < 1000) {
        x = x + 1;
        var y = 0;
        while(y < 10000) {
            y = y + 1;
        }
    }
    print(x + y);
})();
