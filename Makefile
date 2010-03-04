LS = LightScript
DEPS = src/* Makefile
BACKUP=

SRCPATH = src/
INT_VER= net/lightscript/i1/
FP_VER= net/lightscript/f1/
DEFAULT_VER=$(INT_VER)

run: examples/Main.class examples/test.js
	java examples/Main examples/test.js

all: doc classes

backup: clean
	tar cv --no-recursion `find . | grep -v svn` | gzip > backup.tar.gz 

dist: clean all
	tar cv --no-recursion `find Makefile src README COPYING | grep -v svn` | gzip > dist.tar.gz 

examples/Main.class: $(DEFAULT_VER)$(LS).class
	javac -source 1.2 examples/Main.java
	ls -l $(DEFAULT_VER)$(LS).class

doc: . com/solsort/mobile $(INT_VER)$(LS).java $(FP_VER)$(LS).java
	javadoc -d doc/public com.solsort.mobile
	javadoc -private -d doc/private com.solsort.mobile
	javadoc -d doc/old-public/ net.lightscript.f1
	javadoc -d doc/old-public-intonly/ net.lightscript.i1
	javadoc -private -d doc/old-private net.lightscript.f1
	javadoc -private -d doc/old-private-intonly net.lightscript.i1

classes: $(INT_VER)$(LS).class $(FP_VER)$(LS).class

$(INT_VER)$(LS).class: $(INT_VER)$(LS).java
	javac -source 1.2 $(INT_VER)*.java

$(FP_VER)$(LS).class: $(FP_VER)$(LS).java
	javac -source 1.2 $(FP_VER)*.java

$(INT_VER)$(LS).java: $(DEPS)
	install -d $(INT_VER)
	cp src/* $(INT_VER)
	cpp -C -P src/$(LS).java > $(INT_VER)$(LS).java
	for foo in $(INT_VER)/*.java; \
	    do echo "0i\npackage net.lightscript.i1;\n.\nwq\n" | ed -s $$foo;\
	    done;

$(FP_VER)$(LS).java: $(DEPS)
	install -d $(FP_VER)
	cp src/* $(FP_VER)
	cpp -C -P -D__HAVE_DOUBLE__ src/$(LS).java > $(FP_VER)$(LS).java
	for foo in $(FP_VER)/*.java; \
	    do echo "0i\npackage net.lightscript.f1;\n.\nwq\n" | ed -s $$foo;\
	    done;
clean:
	rm -rf $(FP_VER) $(INT_VER) com/solsort/mobile/*.class doc/public* doc/private* doc/old-* `find . -name "*.class"` *.tar.gz examples/*.jar examples/*/*.jar

examples/moby/moby.jar: examples/moby/*.java examples/moby/manifest examples/moby/*.jad
	javac -source 1.2 -classpath .:external_dependencies/midpapi10.jar examples/moby/*.java
	jar -cvfm examples/in.jar examples/moby/manifest net/lightscript/i1/*.class examples/moby/*.class
	java -jar external_dependencies/proguard.jar @examples/midlets.pro
	mv examples/out.jar examples/moby/moby.jar

examples/mob2/mob2.jar: examples/mob2/*.java examples/mob2/manifest examples/mob2/*.jad
	javac -source 1.2 -classpath .:external_dependencies/midpapi10.jar examples/mob2/*.java
	jar -cvfm examples/in.jar examples/mob2/manifest com/solsort/mobile/*.class examples/mob2/*.class
	java -jar external_dependencies/proguard.jar @examples/midlets.pro
	mv examples/out.jar examples/mob2/mob2.jar

ex: examples/mob2/mob2.jar examples/moby/moby.jar
