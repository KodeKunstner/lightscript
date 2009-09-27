// Function that reads one character at a time
getch = (function() {
    var line = "";
    var line_pos = -1;
    var line_nr = 0;
    var empty_line_count = 0;
    
    return function() {
        line_pos = line_pos + 1;
        line = line;
        if(line[line_pos] !== undefined) {
            empty_line_count = 0;
            return line[line_pos];
        } else {
            line_nr = line_nr + 1;
            line_pos = -1;
            line = readline();
            empty_line_count = empty_line_count + 1;
            if(empty_line_count > 10) {
                return undefined;
            } else {
                return "\n";
            } 
        }
    };
})()

while((c = getch()) !== undefined) {
    print(c);
}
