{
document [
]

bibliography {
}

header "\\documentclass[12pt]{report} 
\\usepackage{a4} 
\\usepackage{makeidx}
%\\usepackage[danish]{babel} 
\\usepackage[utf8]{inputenc}
\\usepackage{textcomp}
\\usepackage{amsmath}
\\usepackage{amssymb}
\\usepackage{amsthm}
\\usepackage{graphicx} 
\\usepackage{verbatim} 
\\usepackage{fancyhdr}
\\usepackage{listings} 
\\usepackage[colorlinks,pagebackref]{hyperref}
\\usepackage{backref}
\\usepackage{url}
\\frenchspacing
\\makeindex
\\pagestyle{plain}
\\newcommand{\\Oh}[0]{ \\mathcal{O} }
%\\addtolength{\\voffset}{-88pt}
%\\addtolength{\\textheight}{93pt}

\\title{
\\emph{Document currently under development} \\\\ ~ \\\\
Design and implementation of \\\\ 
an EcmaScript-like language \\\\ 
targeted mobile devices}

\\author{
  Rasmus Erik Voel Jensen\\footnote{
    sumsar@solsort.dk
  }
} 

\\date{2008-2009}

\\begin{document}
\\maketitle
\\begin{abstract}
\\end{abstract}

\\setcounter{tocdepth}{1}
\\tableofcontents
\\listoffigures
\\nocite{sicp}
"

footer "\\newpage
\\addcontentsline{toc}{chapter}{Bibliography}
\\bibliography{bibliography}
\\bibliographystyle{alpha}

\\appendix

\\newpage
\\addcontentsline{toc}{chapter}{Index}
\\printindex

\\end{document}
"
}
