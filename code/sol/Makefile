CLASSES= Code.class Globals.class Function.class Sol.class Builtin.class Immediate.class MobyCode.class

run: $(CLASSES)
	java Sol test.sol

all: $(CLASSES)

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
