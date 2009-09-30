load("ls2lib.js");
load("stdin.js");
load("tokeniser.js");
load("parser.js");

//while(c = parse()) {
for(c in parser) {
    print("##### " + c.subtype + " '" + c.val +"' " + c.children.length);
    for(elem in LightScriptIterator(c.children)) {
        print(elem.val);
    }
}
//tokens = tokenise(stdin);
//for(token in tokens) {
    //print(token.val);
//}
