#!/bin/sh
javac -source 1.2 Sol.java &&
js compiler.js < "$1.js"  > "$1.sol" &&
java Sol "$1.sol"
