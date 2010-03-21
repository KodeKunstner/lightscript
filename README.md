README
======

LightScript is a scripting language for rapid application development for mobile phones.

- Documentation is available on [lightscript.net](http://www.lightscript.net/)
- Latest source code can be downloaded from [github.com/rasmusjensen/lightscript](http://github.com/rasmusjensen/lightscript)
- Updates on recent commits on [twitter.com/lightscript](http://twitter.com/lightscript)


Features include:

- Runs on very low end mobile phone, requires only CLDC/1.0
- JavaScript/EcmaScript-like semantics and syntax
- Closures and first class functions
- Objects with prototypical inheritance
- Small size ( < 30KB code footprint) and fast execution
- Exceptions, object literals
- Good integration with Java


Content of this document
------------------------

- Library Reference
- Language Reference
- Implementation details

For details on the Java API and embedding the language, see the Javadoc on [lightscript.net](http://www.lightscript.net/javadoc/com/solsort/lightscript/package-summary.html)


Library Reference
=================

Standard Library
----------------
(to be more in concordance with standard soon. Non-EcmaScript-functions may change api before in next version)

- print(text)
- clone(Object)
- parseint(string, base)
- global
    - .\_\_getter\_\_(key)
    - .\_\_setter\_\_(key, val)
- *All types*
    - .toString()
    - +
- *java.util.Stack type*
    - [elem1, elem2, ...]
    - Array()
    - .\_\_getter\_\_(key)
    - .\_\_setter\_\_(key, val)
    - .\_\_iter\_\_()
    - .push(val)
    - .pop()
    - .join([seperator])
    - .slice(start[, end])
    - .concat(...)
    - Array.concat(...)
    - .sort([compare function])
    - .toTuple()
- *java.util.Hashtable type*
    - { key: val, key:val, ... }
    - Object()
    - .hasOwnProperty(x)
    - .\_\_getter\_\_(key)
    - .\_\_setter\_\_(key, val)
    - .\_\_iterator\_\_
- *java.lang.String type*
    - "..."
    - '...'
    - String()
    - .\_\_getter\_\_(key)
    - .toInt()
    - .slice(start[, end])
    - .charCodeAt(pos)
    - .concat
    - String.fromCharCode(code)
- *java.lang.Object[] type*
    - Tuple(...)
    - .sort([compare function[, start[, end]]])
    - .toArray()
- *java.lang.Integer type*
    - 123...
    - .toInt()

Midp1 library (api not stabilised)
----------------------------------

- Menu()
    - .addItem(text[, callback])
    - .show()
- TextBox(prompt[, default text], callback)
- Storage.set(key, value)
- Storage.get(key)


Language Reference
==================

Data types
----------

Builtin operations
------------------

Incompatibilities with JavaScript
---------------------------------

Implementation details
======================

Type system
-----------

Exception handling
------------------

Implementation internals
------------------------

Optimisation for Java Mobile Edition
------------------------------------
