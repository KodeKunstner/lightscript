CPP  = gcc
OBJ  = test.o runtime.o runtime_internal.o util.o
HEADERFILES = runtime.h util.h lightscript.h runtime_internal.h
LINKOBJ  = $(OBJ) 
LIBS = 
CINCS =  
CFLAGS = $(INCS) -DNDEBUG -O3 -fomit-frame-pointer -march=i686 -msse -mfpmath=sse
CFLAGS = $(INCS) -O0 -Wall -Werror -g3
RM = rm -rf
BIN = test 

.PHONY: all clean indent 
run: $(BIN)
	./test

all: $(BIN)

indent:
	indent -i4 -kr *.c *.h

$(BIN): $(OBJ)
	$(CC) $(LINKOBJ) -o $@ $(LIBS)

clean:
	rm -f *~ *.o $(BIN)


.SUFFIXES: .c .o

.c.o: $(HEADERFILES)
	$(CC) $(CFLAGS) -c $*.c -o $*.o
