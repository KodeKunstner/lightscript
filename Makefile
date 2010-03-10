LS = LightScript
DEPS = com/solsort/*/*
BACKUP=

SRCPATH = src/
INT_VER= net/lightscript/i1/
FP_VER= net/lightscript/f1/
DEFAULT_VER=$(INT_VER)

test: testsuite/LightScriptTest.class
	java testsuite.LightScriptTest testsuite/*.ls

testsuite/LightScriptTest.class: $(DEPS)

run: examples/Main.class examples/test.js
	java examples/Main examples/test.js

all: doc 

backup: clean
	tar cv --no-recursion `find . | grep -v svn` | gzip > backup.tar.gz 

dist: clean all
	tar cv --no-recursion `find Makefile src README COPYING | grep -v svn` | gzip > dist.tar.gz 

examples/Main.class: $(DEFAULT_VER)$(LS).class
	javac -source 1.2 examples/Main.java
	ls -l $(DEFAULT_VER)$(LS).class

doc: * $(DEPS)
	mkdir -p doc/javadoc
	javadoc -d doc/javadoc/public com.solsort.mobile com.solsort.lightscript
	javadoc -package -d doc/javadoc/package com.solsort.mobile com.solsort.lightscript
	javadoc -private -d doc/javadoc/private com.solsort.mobile com.solsort.lightscript
	pandoc -s README -o doc/README.html
	pandoc -s TODO -o doc/TODO.html

clean:
	rm -rf doc/javadoc doc/TODO.html doc/README.html `find com examples testsuite -name "*.class"` examples/*.jar examples/*/*.jar

examples/moby/moby.jar: examples/moby/*.java examples/moby/manifest examples/moby/*.jad $(DEPS)
	javac -source 1.2 -classpath .:external_dependencies/midpapi10.jar examples/moby/*.java
	jar -cvfm examples/in.jar examples/moby/manifest com/solsort/*/*.class examples/moby/*.class
	java -jar external_dependencies/proguard.jar @examples/midlets.pro
	mv examples/out.jar examples/moby/moby.jar

ex: examples/moby/moby.jar

.SUFFIXES: .java .class

.java.class: $(DEPS)
	javac -source 1.2 $*.java

