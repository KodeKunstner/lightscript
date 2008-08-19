CLASSES=JSON.class Test.class MobyFunction.class MobyVM.class

run: $(CLASSES) test.json
	java Test test.json

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
