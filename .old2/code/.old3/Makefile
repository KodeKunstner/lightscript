CPP  = gcc
OBJ  = test.o
HEADERFILES =
LINKOBJ  = $(OBJ) /usr/lib/libgc.a
LIBS = -lpthread
CINCS =  
CFLAGS = $(INCS) -DNDEBUG -O3 -fomit-frame-pointer -march=i686 -msse -mfpmath=sse
CFLAGS = $(INCS) -D__DEBUG__ -O0 -Wall -Werror -g3
RM = rm -rf
BIN = test

.PHONY: all clean indent 

all: $(BIN)

indent:
	indent -i8 -kr *.c *.h

$(BIN): $(OBJ)
	$(CC) $(LINKOBJ) -o $@ $(LIBS)

clean:
	rm -f *.o $(BIN)


.SUFFIXES: .c .o

.c.o: $(HEADERFILES)
	$(CC) $(CFLAGS) -c $*.c -o $*.o
