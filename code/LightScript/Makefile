source = Main.java LightScript.javapp LightScriptException.java LightScriptFunction.java LightScriptStdLib.java Makefile

run: Main.class test.js
	java Main test.js

Main.class: $(source)
	for file in *.javapp; do cpp -C $(CPPFLAGS) -P $$file `echo $$file | sed -e s/javapp/java/`; done
	javac -source 1.2 *.java
	for file in *.javapp; do mv `echo $$file | sed -e s/javapp/java/` oldimp; done

all: Main.class

clean:
	rm -f *.class *.class.gz LightScript.java

doc: $(source)
	cd doc && javadoc ../*.java
