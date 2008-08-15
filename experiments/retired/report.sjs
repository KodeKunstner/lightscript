[document 
  { title 
      [text Design and implementation of an [newline] EcmaScript-like 
        language [newline] targeted mobile devices]
    author [text Rasmus Erik Voel Jensen]
    authormail sumsar@solsort.dk
    date 2008-2009}
  [section {title Survey} 
    [section {title "Related projects"}
      [p This section investigates projects with similar goals or features as this project - e.g. light-weight scripting. Projects directly related to EcmaScript are discussed in section [ref ecmascript]]
      [section {title Hecl}]
      [section {title Lua}]
      [section {title FlashLite}]
    ]
    [section {title "Mobile programming"}]
    [section {title "Programming language techniques"}]
    [section {title EcmaScript} 
      [label ecmascript]
      [section {title "Dialects}
        [section {title "Mbed EcmaScript}]
      ]
    ]
  ]
  [section {title "Moby expressions"}
    
    [p Moby expressions (MEx) are JSON-like objects written similarly to lisp s-expressions. 
    There are no seperators between elements, or key-value pairs, and "[]" are used for lists and "{}" are used for objects. For example: [code "{foo bar fox [a b c]}"] would be shorthand for [code "{\"foo\": \"bar\", \"fox\": [\"a\", \"b\", \"c\"]}"]. The omission of [quote "\""], [quote ,] and [quote :] makes it simpler to write, and both suitable for text markup, and for writing lisp-like syntax trees etc.
    Unlike JSON, MEx does not distinguish between strings and numbers.
    ]
  ]
]
