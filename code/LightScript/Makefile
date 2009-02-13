CLASSES = Main.class LightScript.class LightScriptException.class LightScriptFunction.class LightScriptObject.class

run: $(CLASSES)
	java Main test.js

all: $(CLASSES)

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
