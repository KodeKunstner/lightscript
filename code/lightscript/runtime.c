#include "runtime.h"
#include "runtime_internal.h"
#include "util.h"

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

int ls_count(lsval var) {
    assert(ls_type(var) == T_ARRAY);
    return ls_to_entry(var)->count;
}

void ls_push(lsval stack, lsval val)
{
    assert(ls_type(stack) == T_ARRAY);
    ls_set(stack, ls_from_short(ls_count(stack)), val);
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
