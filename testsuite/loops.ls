test('
    var x = 0;
    while(x < 50) {
        x = x + 1;
        var y = 0;
        while(y < 5000) {
            y = y + 1;
        }
    }; x+y;', 5050);
