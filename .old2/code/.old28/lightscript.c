int printf(const char *fmt, ...);
#define log(...) { printf("%d: ", __LINE__); printf(__VA_ARGS__); printf("\n"); }
#define assert(a) {if(!(a)) { log("Assert error\n"); for(;;); } }
#define EXIT_SUCCESS 0
typedef unsigned int size_t;
typedef unsigned int uint32;
typedef signed int sint32;
typedef unsigned short uint16;
typedef signed short sint16;
typedef sint16 lsval;

#define LS_NUM_UNSAFE(val) entries(((uint16) lsval) + 1)
#define LS_SET_UNSAFE(val) entries(((uint16) lsval) + 1)

/* +-----+ <- lsvals
 * |     |
 * |     |
 * |HEAP |
 * |     |
 * +-----+ <- lsvals - heapsize
 * |FREE |
 * +-----+ <- entries + entrycount * 2
 * |     |
 * |DESC |
 * |     |
 * +-----+ <- entries
 */
uint32 *entries;
lsval *lsvals;
uint32 heapsize;
uint16 entrycount;

void init(void *p, size_t size) {
    base = p;
    top = (char *)p + size;
    split = (char *)p + size;
}

#define MEM_SIZE 100000
char mem[MEM_SIZE];

int main(int argc, char *argv[]) {
    init(mem, MEM_SIZE);
    return EXIT_SUCCESS;
}
