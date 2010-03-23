f = function(x) { print(x); };
menu0 = Menu()
  .addItem("Hello world!")
  .addItem("Menu1", function() { menu1.show(); })
  .addItem("Menu2", function() { menu2.show(); })
  .show();

menu1 = Menu()
  .addItem("tada", function() { TextInput("Title", foop);});

menu2 = Menu()
  .addItem("to menu 1", function() { menu1.show();});

function foop(text) {
   Menu()
      .addItem(text, function() {menu0.show()})
      .show();
}
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
