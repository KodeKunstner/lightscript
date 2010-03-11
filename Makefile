LS = LightScript
DEPS = com/solsort/*/*
BACKUP=

run: examples/Main.class examples/test.js
	java examples/Main examples/test.js
examples/Main.class: $(DEPS)

test: testsuite/LightScriptTest.class
	java testsuite.LightScriptTest testsuite/*.ls

testsuite/LightScriptTest.class: $(DEPS)

all: doc 

backup: clean
	tar cv --no-recursion `find . | grep -v svn` | gzip > backup.tar.gz 

dist: clean all
	tar cv --no-recursion `find Makefile src README COPYING | grep -v svn` | gzip > dist.tar.gz 

doc: doc/javadoc doc/README.html doc/TODO.html
doc/javadoc: $(DEPS)
	mkdir -p doc/javadoc
	javadoc -d doc/javadoc/public com.solsort.mobile com.solsort.lightscript
	javadoc -package -d doc/javadoc/package com.solsort.mobile com.solsort.lightscript
	javadoc -private -d doc/javadoc/private com.solsort.mobile com.solsort.lightscript

doc/README.html: README.md
	pandoc -s README.md -o doc/README.html

doc/TODO.html: TODO
	pandoc -s TODO -o doc/TODO.html

clean:
	rm -rf doc/javadoc doc/TODO.html doc/README.html `find com examples testsuite -name "*.class"` examples/*.jar examples/*/*.jar

examples/moby/moby.jar: examples/moby/*.java examples/moby/manifest examples/moby/*.jad $(DEPS)
	javac -source 1.2 -classpath .:external_dependencies/midpapi10.jar examples/moby/*.java
	jar -cvfm examples/in.jar examples/moby/manifest com/solsort/*/*.class examples/moby/*.class
	java -jar external_dependencies/proguard.jar @examples/midlets.pro
	mv examples/out.jar examples/moby/moby.jar

ex: examples/moby/moby.jar

st: clean
	git status

diff: clean
	git diff

commit: clean
	git add `find * -name "*.java"` testsuite/*.ls Makefile README.md 
	git commit -m autocommit

.SUFFIXES: .java .class

.java.class: $(DEPS)
	javac -source 1.2 $*.java

