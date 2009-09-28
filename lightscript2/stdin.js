// stdin iterator
stdin = { "__iterator__": function() { return this; },
    "pos": -1,
    "line": 0,
    "next": getch = (function() {
    var line = "";
    var empty_line_count = 0;
    
    return function() {
        this.pos = this.pos + 1;
        if(line[this.pos] !== undefined) {
            empty_line_count = 0;
            return line[this.pos];
        } else {
            this.line = this.line + 1;
            this.pos = -1;
            line = readline();
            empty_line_count = empty_line_count + 1;
            if(empty_line_count > 10) {
                return undefined;
            } else {
                return "\n";
            } 
        }
    };
})()}
