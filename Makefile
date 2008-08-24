all:
	cd code && make all
	cd report && make all

clean:
	cd report && make clean
	cd code/vm && make clean
