SOURCES = Main.java LightScriptException.java LightScriptFunction.java 
DEPS = $(SOURCES) LightScript.in.java Makefile
run: Main.class test.js
	java Main test.js

Main.class: $(DEPS)
	cpp -C -P LightScript.in.java > LightScript.java 
	javac -source 1.2 $(SOURCES) LightScript.java
	mv -f LightScript.java debugsrc.java

all: Main.class

clean:
	rm -f *.class *.class.gz LightScript.java debugsrc.java

doc: $(source)
	cd doc && javadoc ../*.java
