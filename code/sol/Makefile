CLASSES= Code.class Core.class Function.class Yolan.class Builtin.class Immediate.class

run: $(CLASSES)
	java Yolan test.sol

all: $(CLASSES)

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
