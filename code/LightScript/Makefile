CLASSES = Main.class LightScript.class LightScriptException.class LightScriptFunction.class LightScriptObject.class LightScriptStdLib.class

run: $(CLASSES) test.js
	java Main test.js

all: $(CLASSES) test.js

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 *.java
