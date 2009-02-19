#include "runtime.h"
#include "util.h"

#define HEAPSIZE 100000
uint8 heap[HEAPSIZE];

void testheap()
{
    lsval stack;
    ls_init_heap(heap, HEAPSIZE);

    stack = ls_new_array();
    ls_push(stack, ls_from_int(10000));
    ls_push(stack, ls_from_int(100000));

    assert(ls_to_int(ls_pop(stack)) == 100000);
    assert(ls_to_int(ls_pop(stack)) == 10000);

}

int main(int argc, char *argv[])
{
    testheap();
    return EXIT_SUCCESS;
}
