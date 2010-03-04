#!/bin/sh
echo "Recompiling Parser..."
js run.js < parser.js > /tmp/t.js
diff parser.js /tmp/t.js
echo "Recompiling JavaScript Backend..."
js run.js < backend_js.js > /tmp/t.js
diff backend_js.js /tmp/t.js
echo "Recompiling Yolan Parser..."
js run.js < yolan.js > /tmp/t.js
diff yolan.js /tmp/t.js
echo "Done."
