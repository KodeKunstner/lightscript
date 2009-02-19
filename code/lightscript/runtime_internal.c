#include "runtime.h"
#include "runtime_internal.h"
#include "util.h"
#include <stdlib.h>

heap_entry *entries = NULL;
lsval free_entry = -1;
unsigned int entries_size = 0;

int ls_type(lsval val)
{
    assert((val & 1) == 0);
    return ls_to_entry(val)->type;
}

int ls_internal_size(lsval val)
{
    assert(ls_type(val) != T_INT);
    return ls_to_entry(val)->val.size;
}

void ls_init_heap(void *mem, size_t size)
{
    assert((size & 7) == 0);
    /*assert(sizeof(heap_entry) == 8); */
    entries = malloc(sizeof(heap_entry));
    entries->type = T_ARRAY;
    entries->count = 0;
    entries->val.ptr = NULL;
    entries->val.size = 0;
}

void gc()
{
    delete_entry(new_entry());
}


void delete_entry(lsval val)
{
}

void ls_resize(heap_entry * cont)
{
    assert(cont->type & T_RESIZABLE);
    if (cont->count > cont->val.size) {
	cont->val.size = cont->count;
	cont->val.ptr =
	    realloc(cont->val.ptr, cont->count * sizeof(lsval));
    }
}

heap_entry *ls_to_entry(lsval val)
{
    assert((val & 1) == 0);
    return entries + (((unsigned) val) >> 1);
}

lsval new_entry()
{
    lsval result;
    /* add to live-list, pop from here when positioned */

    
    if (free_entry == -1) {
	int new_size = ((entries_size + 1) * 5) / 4;
	entries = realloc(entries, new_size * sizeof(heap_entry));
	assert(entries != NULL);
	entries[entries_size].val.num = -1;
	entries_size++;
	while (entries_size < new_size) {
	    entries[entries_size].val.num = (entries_size - 1) << 1;
	    entries_size++;
	}
	free_entry = (entries_size - 1) << 1;
    }
    result = free_entry;
    free_entry = ls_to_entry(result)->val.num;
    heap_entry *entry = ls_to_entry(result);
    entry->type = T_NATIVE;
    entry->count = 0;
    entry->val.size = 0;
    entry->val.ptr = NULL;
    return result;
}
