source = Main.java LightScript.javapp LightScriptException.java LightScriptFunction.java LightScriptObject.java LightScriptStdLib.java

run: Main.class test.js
	java Main test.js

Main.class: $(source)
	for file in *.javapp; do cpp -P $$file `echo $$file | sed -e s/javapp/java/`; done
	javac -source 1.2 *.java

all: Main.class

clean:
	rm -f *.class *.class.gz
