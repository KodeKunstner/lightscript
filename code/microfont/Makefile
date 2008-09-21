run: GenPixFont.class slim9px.png
	java GenPixFont slim9px.png

MicroFont.java: monofont2.xpm cogen.c MicroFont.header MicroFont.footer slimfont.xpm slimfont2.xpm monofont.xpm
	cc -Wall -Werror cogen.c -o cogen
	cat MicroFont.header > MicroFont.java
	./cogen >> MicroFont.java
	cat MicroFont.footer >> MicroFont.java

clean:
	rm -f *.o *.class *.xpm MicroFont.java cogen

.SUFFIXES: .png .xpm .java .class

.png.xpm:
	convert $*.png $*.xpm

.java.class:
	javac $*.java
