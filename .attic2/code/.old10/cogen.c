#include <stdlib.h>
#include <stdio.h>

#include "slimfont2.xpm"
#include "slimfont.xpm"

void printfont(char **p) {
	unsigned int i, j, k, result;
	for(i=0;i<90;i++) {
		for(k=0;k<3;k++) {
			result = 0;
			for(j=6;j<8;j--) {
				result = (result << 1) | (p[j+3][k + 4*i] == '.');
			}
			if(result == '\n') {
				printf("\\n");
			} else if(result == '\\') {
				printf("\\\\");
			} else if(result == '\r') {
				printf("\\r");
			} else if(result == '\"') {
				printf("\\\"");
			} else if((result >= 32) && (result < 127)) {
				printf("%c", result);
			} else {
				printf("\\u00%02X", result);
			}
		}
	}
}

int main(int argc, char** argv) {

	printf("\tstatic byte[] monofont = \"");
	printfont(slimfont_);
	printf("\".getBytes();\n");
	printf("\tstatic byte[] slimfont = \"");
	printfont(slimfont);
	printf("\".getBytes();\n");
	return EXIT_SUCCESS;
}
