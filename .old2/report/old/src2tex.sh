#/bin/sh

for file in `cd ../code; find compiler/ -name "*.js"` `cd ../code; find vm/ -name "*.java"` 
do
  cat ../code/$file |
    sed -e s/"\t"/".   "/g |
    sed -e 's/^\/\///' > ./code/$file.tex
#    sed -e s/"\t"/".   "/g |
#    sed -e s/"^$/ "/ |
#    sed -e s/"^\([^/].*\)/~\\\\\\\\ {\\\\scriptsize \\\\verb#\1#}"/ |
#    sed -e s/.verb"# #"// |
#    sed -e s/"\/\/"// |
#    sed -e s/^" "// > ./code/$file.tex
done
