MAINFILE=thesis

view: $(MAINFILE).pdf $(MAINFILE).tex
	evince $(MAINFILE).pdf

sharable: $(MAINFILE).pdf Makefile
	rm -f /tmp/thesis-no-search*.ppm /tmp/thesis-no-search*.ppm.pdf
	pdftoppm -r 273 $(MAINFILE).pdf /tmp/thesis-no-search
	for x in /tmp/thesis-no-search*.ppm; do convert $$x $$x.pdf; rm $$x; done
	pdfjoin --outfile report.pdf /tmp/thesis-no-search*.ppm.pdf

part: excerpt.pdf 
	evince excerpt.pdf

synopsis.pdf: synopsis.tex
	pdflatex $*

syn: synopsis.pdf
	evince synopsis.pdf

excerpt.pdf: excerpt.tex */*.tex
	pdflatex excerpt

all: $(MAINFILE).pdf  
#excerpt.pdf  synopsis.pdf

$(MAINFILE).pdf: $(MAINFILE).tex *.tex bibliography.bib */*.tex ../code/bench/*.tex ../code/LightScript/LightScript.javapp ../code/Yolan/src/Yolan.java

synopsis.pdf: synopsis.tex bibliography.bib

.SUFFIXES: .fig .eps .dvi .ps .pdf .tex .dot 

.eps.pdf:
	epstopdf $*.eps

.tex.pdf:
	# ./src2tex.sh
	./version.sh > version.tex ; \
	pdflatex $*.tex && bibtex $* && makeindex $*.idx && \
	pdflatex $*.tex && pdflatex $*.tex

.dvi.ps:
	dvips -o $*.ps $*.dvi

.fig.eps:
	fig2dev -L eps  $*.fig $*.eps

.fig.pdf:
	fig2dev -L pdf  $*.fig $*.pdf
	
.dot.eps:
	dot -Tps < $*.dot > $*.eps

clean:
	rm -f *.bbl *.aux *.blg *.log *.dvi *.idx *.ind *.ilg *.toc *.pdf *.ps *.lof *.png *.eps interviews/[0-9]*.tex *.out *.brf ekstraeval/interview.tex code/*/*.tex */*.aux version.tex /tmp/thesis-no-search*ppm*
