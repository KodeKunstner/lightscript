SRC = Main.java LightScriptException.java LightScript.java LightScriptObject.java LightScriptFunction.java
LS_SRC = LightScript.java
DEPS = $(SRC) Makefile

SRCPATH = src/
CLDC10PATH= net/lightscript/CLDC10/

$(CLDC10PATH)$(LS_SRC): src/*
	install -d $(CLDC10PATH)
	cp src/* $(CLDC10PATH)
	cpp -C -P src/$(LS_SRC) > $(CLDC10PATH)LightScript.java 

run: Main.class test.js
	java Main test.js

LightScript.java: $(LS_SRC)
	cpp -C -P $(LS_SRC) > LightScript.java 

Main.class: $(DEPS) $(SRC)
	javac -source 1.2 $(SRC)

all: Main.class

clean:
	rm -rf *.class *.class.gz LightScript.java doc private-doc

doc: $(SRC)
	mv $(LS_SRC) $(LS_SRC).nodoc
	javadoc -d private-doc -private $(SRC); mv $(LS_SRC).nodoc $(LS_SRC)
	mv $(LS_SRC) $(LS_SRC).nodoc
	javadoc -d doc $(SRC); mv $(LS_SRC).nodoc $(LS_SRC)
