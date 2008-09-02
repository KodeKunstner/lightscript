run: 
	cd compiler && ./run.js < ../test/test.js > ../test/test.json
	cd vm && make run
all:
	cd vm && make all
	cd cvm && make all
	cd mobileapp && ant jar-all

eval:
	cd compiler && ./test-run.js < ../test/test.js 

clean:
	rm -f test/*.json
	cd cvm && make clean
	cd vm && make clean
	cd mobileapp && ant clean
