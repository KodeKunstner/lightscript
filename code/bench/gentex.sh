#!/bin/sh
rm -f marks.tex
echo '\\chapter{Benchmark source}' >> marks.tex
echo '\\label{benchmarksource}' >> marks.tex
for foo in */*; do 
echo '\\section*{'$foo'}' >> marks.tex
echo '{\\scriptsize ' >> marks.tex
echo '\\begin{verbatim}' >> marks.tex
cat $foo >> marks.tex
echo '\\end{verbatim}}' >> marks.tex
done

