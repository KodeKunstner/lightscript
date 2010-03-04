#include "runtime.h"
#include "runtime_internal.h"
#include "util.h"

static int find_key(heap_entry * obj_entry, lsval key)
{
    int result = 1;
    int count = obj_entry->count;
    while (result < count && SUB(obj_entry, result) != key) {
	result += 2;
    }
    return (result < count) ? result : -1;
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
    } else if (entry->type == T_ARRAY) {
	int pos = find_key(entry, key);
	if (pos == -1) {
	    pos = entry->count;
	    entry->count += 2;
	    ls_resize(entry);
	    SUB(entry, pos) = key;
	}
	pos++;
	SUB(entry, pos) = val;
    } else {
	assert(0);
	error("Not subscriptable");
    }
}

int ls_count(lsval var)
{
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
    if (ls_type(stack) != T_ARRAY) {
	error("Trying to pop something not an array");
    }

    heap_entry *entry = ls_to_entry(stack);

    if (entry->count == 0) {
	error("Popping from empty stack");
    }
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

lsval ls_new_object(lsval prototype)
{
    lsval result = new_entry();
    heap_entry *entry = ls_to_entry(result);
    entry->type = T_OBJECT;
    entry->count = 1;
    ls_resize(entry);
    SUB(entry, 0) = prototype;
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
    } else if (entry->type == T_OBJECT) {
        for(;;) {
            lsval parent;
            int pos = find_key(entry, key);
            if(pos != -1) {
                return SUB(entry, pos + 1);
            }
            parent = SUB(entry, 0);
            if(parent == UNDEFINED) {
                return parent;
            }
            entry = ls_to_entry(parent);
        }
    } else {
	assert(0);
	error("Get not supported on that datastructure");
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
