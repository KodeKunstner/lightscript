CLASSES=JSON.class MobyVM.class Test.class 

all: $(CLASSES)

run: $(CLASSES) ../test/test.json
	java Test ../test/test.json

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
