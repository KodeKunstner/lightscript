CLASSES= Builtin.class Code.class Core.class Function.class Main.class Yolan.class

run: $(CLASSES)
	java Main test.sol

all: $(CLASSES)

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
