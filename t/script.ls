menu = Menu()
  .addItem("Hello")
  .addItem("foobar", function() {
     Menu().addItem("blah").show();
   })
   .show();

/* menu0 = Menu('front page')
  .addItem("Hello world!")
  .addItem("Menu1", function() { menu1.show(); })
  .addItem("Menu2", function() { menu2.show(); })
  .show();

menu1 = Menu('menu1')
  .addItem("tada", function() { TextInput("Title", foop);});

menu2 = Menu('menu2')
  .addItem("to menu 1", function() { menu1.show();});

function foop(text) {
   Menu(text)
      .addItem(text, function() {menu0.show()})
      .show();
}*/
/*
function editfoo() {
    TextInput("foo", Storage["foo"], commitfoo);
}
function commitfoo(text) {
    Storage["foo"] = text;
    menu();
}
function menu() {
    Menu().addItem("foo: " + Storage.foo, editfoo).addItem("list", menu2).show();
}
function menu2() {
    var m = Menu().addItem("back", menu).show();
    for(x in Storage) {
        m.addItem(x + ": " + Storage[x]);
    }
}
print("HERE");
Storage.set("hello", "world");
for(x in Storage) {
     print(x + ": " + Storage[x]);
}

print("HERE2" + Storage.foo);
TextInput("foo", Storage["foo"], commitfoo);

menu();
*/