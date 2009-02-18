#include "ls_base.h"
#include "util.h"
#include <stdlib.h>

#define T_FREE 0
#define T_INT 1
#define T_STRING 2
#define T_NATIVE 3
#define T_OBJECT 4
#define T_ARRAY 5

#define T_RESIZABLE (T_OBJECT & T_ARRAY)


typedef struct {
    unsigned int type:3;
    unsigned int gc_bit:1;
    unsigned int count;
    union {
	struct {
	    lsval *ptr;
	    unsigned int size;
	};
	sint32 num;
    } val;
} heap_entry;

#define SUB(entry, key) ((entry)->val.ptr[key])

static heap_entry *entries = NULL;
static lsval free_entry = -1;
static unsigned int entries_size = 0;

static lsval new_entry();
static heap_entry *ls_to_entry(lsval);
static void delete_entry(lsval);
static void ls_resize(heap_entry * cont);

static int ls_type(lsval val)
{
    assert((val & 1) == 0);
    return ls_to_entry(val)->type;
}

int ls_size(lsval val)
{
    assert(ls_type(val) != T_INT);
    return ls_to_entry(val)->val.size;
}

void ls_set(lsval cont, lsval key, lsval val)
{
    heap_entry *entry = ls_to_entry(cont);
    if (entry->type == T_ARRAY) {
	assert(key & 1);
	int ikey = key >> 1;
	assert(ikey >= 0);
	if (ikey >= entry->count) {
	    entry->count = ikey + 1;
	    ls_resize(entry);
	}
	SUB(entry, ikey) = val;
    } else {
	assert(0);
    }
}

void ls_push(lsval stack, lsval val)
{
    assert(ls_type(stack) == T_ARRAY);
    ls_set(stack, ls_from_short(ls_size(stack)), val);
}

lsval ls_pop(lsval stack)
{
    assert(ls_type(stack) == T_ARRAY);
    heap_entry *entry = ls_to_entry(stack);
    assert(entry->count > 0);
    entry->count = entry->count - 1;;
    return SUB(entry, entry->count);
}

lsval ls_new_array()
{
    lsval result = new_entry();
    heap_entry *entry = ls_to_entry(result);
    entry->type = T_ARRAY;
    entry->count = 0;
    entry->val.size = 0;
    entry->val.ptr = NULL;
    return result;
}

lsval ls_get(lsval cont, lsval key)
{
    heap_entry *entry = ls_to_entry(cont);
    if (entry->type == T_ARRAY) {
	assert(key & 1);
	int ikey = key >> 1;
	assert(ikey >= 0);
	assert(ikey < entry->count);
	return SUB(entry, ikey);
    } else {
	assert(0);
    }
}

lsval ls_from_int(sint32 i)
{
    lsval result;
    heap_entry *entry;
    /* can it be stored in 15 bit? */
    result = i << 1;
    if ((i << 1) == result) {
	return result | 1;
    }

    result = new_entry();
    entry = ls_to_entry(result);
    entry->type = T_INT;
    entry->val.num = i;
    return result;
}

sint32 ls_to_int(lsval val)
{
    heap_entry *entry;
    if (val & 1) {
	return val >> 1;
    }
    entry = ls_to_entry(val);
    assert(entry->type == T_INT);
    return entry->val.num;
}

void ls_init_heap(void *mem, size_t size)
{
    assert((size & 7) == 0);
    /*assert(sizeof(heap_entry) == 8); */
}

void gc()
{
    delete_entry(new_entry());
}


static void delete_entry(lsval val)
{
}

static void ls_resize(heap_entry * cont)
{
    assert(cont->type & T_RESIZABLE);
    if (cont->count > cont->val.size) {
	cont->val.size = cont->count;
	cont->val.ptr =
	    realloc(cont->val.ptr, cont->count * sizeof(lsval));
    }
}

static heap_entry *ls_to_entry(lsval val)
{
    assert((val & 1) == 0);
    return entries + (((unsigned) val) >> 1);
}

static lsval new_entry()
{
    lsval result;
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
    return result;
}
