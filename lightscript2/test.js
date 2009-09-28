load("ls2lib.js");
load("stdin.js");
load("token.js");
load("tokeniser.js");

tokens = tokenise(stdin);
for(token in tokens) {
    print(token.val);
}
