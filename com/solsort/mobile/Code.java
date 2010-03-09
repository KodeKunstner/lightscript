package com.solsort.mobile;


class Code implements Function {

        private static final boolean DEBUG_ENABLED = true;
        public Object apply(Object[] args, int argpos, int argcount)
                throws ScriptException {
            if (!DEBUG_ENABLED || argcount == argc) {
                Object stack[];
                if (argpos != 0) {
                    stack = new Object[argcount + 1];
                    for (int i = 0; i <= argcount; i++) {
                        stack[i] = args[argpos + i];
                    }
                } else {
                    stack = args;
                }
                return LightScript.execute(this, stack, argcount);
            } else {
                throw new ScriptException("Wrong number of arguments");
            }
        }
        public int argc;
        public byte[] code;
        public Object[] constPool;
        public Object[] closure;
        public int maxDepth;

        public Code(int argc, byte[] code, Object[] constPool, Object[] closure, int maxDepth) {
            this.argc = argc;
            this.code = code;
            this.constPool = constPool;
            this.closure = closure;
            this.maxDepth = maxDepth;
        }

        public Code(Code cl) {
            this.argc = cl.argc;
            this.code = cl.code;
            this.constPool = cl.constPool;
            this.maxDepth = cl.maxDepth;
        }
    }

