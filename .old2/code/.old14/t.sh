#!/bin/sh
javac -source 1.2 Sol.java &&
js compiler.js < "t.js"  > "t.sol" &&
java Sol "t.sol" < t.js
