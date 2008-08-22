run: 
	cd compiler && ./run.js < ../test/test.js > ../test/test.json
	cd vm && make run

run-compiler:
	cd compiler && ./test-run.js < ../test/test.js 

clean:
	rm test/*.json
	cd vm && make clean
