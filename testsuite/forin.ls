test('
arr = {};
for(i = 0; i< 50; ++i) {
    arr[i] = i;
}
for(x in arr)
    for(y in arr)
        for(z in arr)
                    ++i;
i;', 125050);

