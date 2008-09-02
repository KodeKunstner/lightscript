#include <stdio.h>
#include <stdlib.h>
#include <gc/gc.h>

int main(int argc, char **argv) {
	int i, *j[1000], sum;
	GC_INIT();
	sum = 0;
	for(i=0;i<200000000;i++) {
		j[(i*1237)%1000] = GC_MALLOC(i % 2200);
		sum = sum + (i % 1000);
		if(i%1000 == 0) {
			printf("%d bytes alloc'ed\n", sum);
		}
	}
	return EXIT_SUCCESS;
}
