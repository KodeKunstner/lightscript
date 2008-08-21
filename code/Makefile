run: 
	cd compiler && ./run.js < ../test/test.js > ../test/test.json
	cd vm && make run

clean:
	rm test/*.json
	cd vm && make clean
