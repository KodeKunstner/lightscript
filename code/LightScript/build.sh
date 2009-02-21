for file in *.javapp; do 
    cpp -P $file `echo $file | sed -e s/javapp/java/`
done
javac -source 1.2 *.java
