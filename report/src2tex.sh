#/bin/sh

for file in compiler/parser.js 
do
  cat ../code/$file |
    sed -e s/"\t"/"    "/g |
    sed -e s/"^$/ "/ |
    sed -e s/"^\([^/].*\)/~\\\\\\\\ {\\\\scriptsize \\\\verb#\1#}"/ |
    sed -e s/"\/\/"// |
    sed -e s/^" "// > ./code/$file.tex
done
