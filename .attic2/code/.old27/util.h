#ifndef __UTIL_H__
#define __UTIL_H__

typedef unsigned short uint16;
typedef signed short sint16;
typedef unsigned int uint32;
typedef signed int sint32;
typedef unsigned char uint8;
typedef uint32 size_t;

#define MAX(a, b) ((a)>(b)?(a):(b))
#define MIN(a, b) ((a)<(b)?(a):(b))

#define NULL ((void *)0)
#define EXIT_SUCCESS 0

void print(const char *s);
void error(const char *s);

/**
 * Logging and assert
 */
#ifdef NDEBUG
#   define log(...) { }
#   define assert(...) { }
#else
int printf(const char *fmt, ...);
#   define log(...) { printf("%s %d: ", __FILE__, __LINE__); printf(__VA_ARGS__); printf("\n"); }
#   define assert(a) {if(!(a)) { log("Assert error\n"); for(;;); } }
#endif

#endif				/* __UTIL_H__ */
