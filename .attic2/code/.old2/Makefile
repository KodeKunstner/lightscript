CLASSES=JSON.class MobyBuiltin.class MobyCode.class MobyFunction.class MobyVM.class Test.class Print.class


all: $(CLASSES)

run: $(CLASSES) ../test/test.json
	java Test ../test/test.json

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
