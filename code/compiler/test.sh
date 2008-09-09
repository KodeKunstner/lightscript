#!/bin/sh
echo "Recompiling Parser..."
js compiler.js < compiler.js > /tmp/t.js
diff compiler.js /tmp/t.js
echo "Done."
