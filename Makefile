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
doc/javadoc: $(DEPS) README.md TODO
	markdown2pdf --toc README.md 
	mv README.pdf doc/
	
	cat doc/source/header.inc > doc/index.html
	cat doc/source/logo.inc >> doc/index.html
	cat doc/source/menu.readme.inc >> doc/index.html
	echo "<h1>README</h1>" >> doc/index.html
	pandoc --toc README.md >> doc/index.html
	cat doc/source/footer.inc >> doc/index.html
	
	cat doc/source/header.inc > doc/roadmap.html
	cat doc/source/logo.inc >> doc/roadmap.html
	cat doc/source/menu.roadmap.inc >> doc/roadmap.html
	echo "<h1>Roadmap</h1>" >> doc/roadmap.html
	cat TODO Changelog | pandoc --toc >> doc/roadmap.html
	cat doc/source/footer.inc >> doc/roadmap.html
	
	javadoc -use -notree -nodeprecatedlist -stylesheetfile doc/static/style.css -doctitle doctitleX -top "`cat doc/source/logo.inc doc/source/menu.javadoc.inc` <h1>Java API</h1>" -bottom "`cat doc/source/menu.javadoc.inc`" -d doc/javadoc com.solsort.lightscript
	javadoc -package -d doc/javadoc/package com.solsort.lightscript
	javadoc -private -d doc/javadoc/private com.solsort.lightscript

doc/README.html: README.md
	pandoc -s README.md -o doc/README.html

doc/TODO.html: TODO
	pandoc -s TODO -o doc/TODO.html

clean:
	rm -rf doc/javadoc doc/TODO.html doc/README.html `find com examples testsuite -name "*.class"` examples/*.jar examples/*/*.jar com.solsort.lightscript/package.html `find * -name "*.java.orig"` doc/*.html doc/*.pdf doc/index.yaml

examples/moby/moby.jar: examples/moby/*.java examples/moby/manifest examples/moby/*.jad $(DEPS)
	javac -source 1.2 -classpath .:external_dependencies/midpapi10.jar examples/moby/*.java
	jar -cvfm examples/in.jar examples/moby/manifest com/solsort/*/*.class examples/moby/*.class
	java -jar external_dependencies/proguard.jar @examples/midlets.pro
	mv examples/out.jar examples/moby/moby.jar

examples/guitest/guitest.jar: examples/guitest/* $(DEPS)
	cd examples/guitest; ln -sf ../../com .
	cd examples/guitest; javac -source 1.2 -classpath .:../../external_dependencies/midpapi10.jar:../../external_dependencies/cldcapi10.jar *.java
	cd examples/guitest; jar -cvfm ../in.jar manifest com/solsort/*/*.class *.class script.ls
	cd examples/guitest; rm -f com
	java -jar external_dependencies/proguard.jar @examples/midlets.pro
	mv examples/out.jar examples/guitest/guitest.jar

guitest: examples/guitest/guitest.jar
	../../WTK2.5.2/bin/emulator -cp examples/guitest/guitest.jar Midlet

ex: examples/moby/moby.jar

reindent:
	astyle `find * -name "*.java"`

st: clean
	git status || echo

diff: clean
	git diff 

commit: clean
	git add `find * -name "*.java"` `find * -name "*.ls"` Makefile README.md TODO
	git commit -m autocommit

.SUFFIXES: .java .class

.java.class: $(DEPS)
	javac -source 1.2 $*.java

