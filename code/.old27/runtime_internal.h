#ifndef __RUNTIME_INTERNAL_H__
#define __RUNTIME_INTERNAL_H__
#include "runtime.h"

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

extern heap_entry *entries;
extern lsval free_entry;
extern unsigned int entries_size;

lsval new_entry();
heap_entry *ls_to_entry(lsval);
void delete_entry(lsval);
void ls_resize(heap_entry * cont);
int ls_type(lsval val);
int ls_internal_size(lsval val);
lsval ls_from_int(sint32 i);
sint32 ls_to_int(lsval val);
void ls_init_heap(void *mem, size_t size);
void gc();
void delete_entry(lsval val);
void ls_resize(heap_entry * cont);
heap_entry *ls_to_entry(lsval val);
lsval new_entry();
#endif /*__RUNTIME_INTERNAL_H__*/
