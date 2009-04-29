SRC = Main.java LightScriptException.java LightScriptFunction.java LightScript.java
LS_SRC = UnpreprocessedLightScript.java
DEPS = $(SRC) Makefile
run: Main.class test.js
	java Main test.js

LightScript.java: $(LS_SRC)
	cpp -C -P $(LS_SRC) > LightScript.java 

Main.class: $(DEPS) $(SRC)
	javac -source 1.2 $(SRC)

all: Main.class

clean:
	rm -rf *.class *.class.gz LightScript.java doc

doc: $(SRC)
	mv $(LS_SRC) $(LS_SRC).nodoc
	javadoc -d doc -private $(SRC); mv $(LS_SRC).nodoc $(LS_SRC)
