" " "parser.char" set-global


#####################
# Predicate functions for testing type of a char
# c -> c bool
##
"is-num" (
	dup ( dup "9" str<= ( "0" dup2 str<= ) and ) and
) defun

"is-ws" (
	dup ( dup " " str<= ) and
) defun

###############33
# get next character
#
"next-char" (
	"parser.inputstream" get-global
	input-stream-read
) defun

#################
# skip white spaces
# (c -> c)
"skip-ws" ( ( is-ws ) ( drop next-char ) while ) defun

########################
# Read the next token
#
# ( -> token )
"next-token" (
	#dup "parser.inputstream" set-global
	"parser.char" get-global	
	next-char
	skip-ws
	"parser.char" set-global
) defun

###########
# Testrunner, open a file for input
# and print tokens from it
#
# ( filename -> )
"run" ( 
	open-input-file "parser.inputstream" set-global
		( next-token dup )
		( print )
	while
	drop
) defun
