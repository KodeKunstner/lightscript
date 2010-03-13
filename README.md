LightScript - a scripting language for low-end mobile phones
============================================================

LightScript is a subscript of EcmaScript,
with a compact implementation running
on low end mobile devices.

Currently under major redevelopment
todo more introduction...

Current Standard Library
------------------------
(to be more in concordance with standard soon. Non-EcmaScript-functions may change api before in next version)

- print(text)
- clone(Object)
- parseint(string, base)
- global
    - .__getter__(key)
    - .__setter__(key, val)
- All types
    - .toString()
    - +
- Stack type 
    - Array(), [...]
    - .__getter__(key)
    - .__setter__(key, val)
    - .__iter__()
    - .push(val)
    - .pop()
    - .join([seperator])
    - .slice(start[, end])
    - .concat(...)
    - Array.concat(...)
    - .sort([compare function])
    - .toTuple()
- Hashtable type 
    - Object(), {...}
    - .hasOwnProperty(x)
    - .__getter__(key)
    - .__setter__(key, val)
    - .__iterator__
- String type
    - String()
    - .__getter__(key)
    - .toInt()
    - .slice(start[, end])
    - .charCodeAt(pos)
    - .concat
    - String.fromCharCode(code)
- Object[] type
    - Tuple(...)
    - .sort([compare function[, start[, end]]])
    - .toArray()
- Integer type
    - .toInt()

Midp1 library (api not stabilised)
----------------------------------

- Menu()
    - .addItem(text[, callback])
    - .show()
- TextBox(prompt[, default text], callback)
- Storage.set(key, value)
- Storage.get(key)

Examples
--------

todo: some examples her

Longer examples can be found in the examples/ catalogue in the source distribution
