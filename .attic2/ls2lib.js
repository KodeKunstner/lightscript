(function() { global = this; })()

if (Object.create === undefined) {
    Object.create = function (o) {
        function F() {};
        F.prototype = o;
        return new F();
    };
} 

if(global.StopIteration === undefined) {
    global.StopIteration = "StopIteration exception"
}

if(global.LightScriptIterator === undefined) {
    var arrayIter = {
        "__iterator__": function() { return this; },
        "next": function() { 
            var pos = ++this.pos;
            var seq = this.seq;
            if(pos < seq.length) {
                return seq[pos];
            } else {
                throw StopIteration;
            }
        }
    };
    global.LightScriptIterator = (function() {
        return function(seq) {
            if(seq.__iterator__ !== undefined) {
                return seq.__iterator__();
            } else if(seq instanceof Array || typeof(seq) === "string") {
                var result = Object.create(arrayIter);
                result.pos = -1;
                result.seq = seq;
            } else {
                var keys = [];
                var elem;
                for(elem in seq) if(seq.hasOwnProperty(elem)) {
                    keys.push(elem);
                }
                return LightScriptIterator(keys);
            }
        };
    })();
};
