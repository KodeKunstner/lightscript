default:
	cd report && make view

all:
	cd code && make all
	cd report && make all

clean:
	cd code && make clean
	cd report && make clean
