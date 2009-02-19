#include "util.h"
int printf(const char *fmt, ...);

void print(const char *s)
{
    printf("%s", s);
}

void error(const char *s)
{
    print("Error: ");
    print(s);
    print("\n");
    for (;;);
}
