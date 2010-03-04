(function() { global = this; })()

if(global.StopIteration === undefined) {
    global.StopIteration = "StopIteration exception"
}

if(Object.create === undefined) {
    Object.create = function(obj) {
        F = function() {};
        F.prototype = obj;
        return new F();
    }
} else if(typeof(Object.create) !== "function") {
    throw "Error: Object.create is not the cloning function";
}

LightScript = {}

LightScriptIterator = (function(){
    var ArrayIterator = function(seq) {
        this.pos = -1;
        this.seq= seq;
    }
    ArrayIterator.prototype.__iterator__ = function() { return this; };
    ArrayIterator.prototype.next = function() { 
        var pos = ++this.pos;
        var seq = this.seq;
        if(pos < seq.length) {
            return seq[pos];
        } else {
            throw StopIteration;
        }
    }
    
    return function(seq) {
        if(seq.__iterator__ !== undefined) {
            return seq.__iterator__();
        } else if(seq instanceof Array || typeof(seq) === "string") {
            return new ArrayIterator(seq);
        } else {
            var keys = [];
            var elem;
            for(elem in seq) if(seq.hasOwnProperty(elem)) {
                keys.push(elem);
            }
            return LightScriptIterator(keys);
        }
    }
})();

function LightScriptContains(elem, list) {
    for(var e in LightScriptIterator(list)) {
        if(e === elem) {
            return true;
        }
    }
    return false;
}

function assert(val) {
    if(!val) {
        throw("assert error");
    }
}
