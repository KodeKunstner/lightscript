(function() { global = this; })()

if(global.StopIteration === undefined) {
    global.StopIteration = "StopIteration exception"
}

function LightScriptArrayIterator(seq) {
    this.pos = -1;
    this.seq= seq;
}
LightScriptArrayIterator.prototype.__iterator__ = function() { return this; };
LightScriptArrayIterator.prototype.next = function() { 
    var pos = ++this.pos;
    var seq = this.seq;
    if(pos < seq.length) {
        return seq[pos];
    } else {
        throw StopIteration;
    }
}

function LightScriptIterator(seq) {
    if(seq.__iterator__ !== undefined) {
        return seq.__iterator__();
    } else if(seq instanceof Array || typeof(seq) === "string") {
        return new LightScriptArrayIterator(seq);
    } else {
        var keys = [];
        var elem;
        for(elem in seq) if(seq.hasOwnProperty(elem)) {
            keys.push(elem);
        }
        return LightScriptIterator(keys);
    }
}
