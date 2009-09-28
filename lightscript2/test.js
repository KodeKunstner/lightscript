load("ls2lib.js");
load("stdin.js");
load("tokeniser.js");
load("parser.js");

while(c = parse()) {
    print(c.val);
}
//tokens = tokenise(stdin);
//for(token in tokens) {
    //print(token.val);
//}
