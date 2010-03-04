CLASSES = Main.class Parser.class

run: $(CLASSES)
	java Main test.ls

all: $(CLASSES)

clean:
	rm -f *.class *.class.gz

.SUFFIXES: .java .class

.java.class:
	javac -source 1.2 $*.java
