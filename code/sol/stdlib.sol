
### Function for defining new functions ###
#
# ( name function -> )

"defun" ( compile swap set-global ) compile swap set-global

### Function executing a file ###
#
# ( filename -> )

"load" ( 
	# save current open file
	"current-input-file" get-global swap

	# open the file
	open-input-file
	"current-input-file" set-global

	# read-eval loop
	(	
		"current-input-file" get-global
		parse
		dup
	) (
		apply
	) while
	drop

	"current-input-file" 
	set-global
) defun

