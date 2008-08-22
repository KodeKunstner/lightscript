all:
	cd report && make all

clean:
	cd report && make clean
	cd code/vm && make clean
	wc `find code report -type f | grep -v svn` > .wc.`date +%y%m%d` ;
