o = {
    "t": "BAR", 
    "x": function() {print(this.t);}
};

p = clone(o);
p.t = "FOO";

q = clone(p);
q.t = "BAZ";

p.x(); o.x(); q.x();
