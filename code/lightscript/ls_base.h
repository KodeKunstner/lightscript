#ifndef __LS_BASE_H__
#define __LS_BASE_H__
#include "util.h"

/*
 * A lightscript value,
 * this consists of a 1 bit
 * type tag and a 15 bit
 * signed integer, or 
 * index into the object table
 */
typedef sint16 lsval;

/** Initialise the heap */
void ls_init_heap(void *mem, size_t size);

/** Create a lightscript value from a 15bit signed int */
#define ls_from_short(num) ((sint16)((num) << 1) | 1)

/** Cast a lightscript value to a 15bit signed int */
#define ls_to_short(ls_val) (assert(lsval & 1), ((sint16)ls_val) >> 1)

lsval ls_from_int(sint32 num);
sint32 ls_to_int(lsval val);

lsval ls_new_array();
void ls_push(lsval stack, lsval val);
size_t ls_size(lsval container);
void ls_set(lsval container, lsval key, lsval val);
lsval ls_get(lsval container, lsval key);
lsval ls_pop(lsval stack);


#endif				/* __LS_BASE_H__ */
