CLASSES=Yolan.class Test.class

run: $(CLASSES) test.yl
	java Test test.yl

all: $(CLASSES)

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
