/*
Copyright (c) 2008 Kristofer Karlsson <kristofer.karlsson@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package se.krka.kahlua.vm;

public class LuaCallFrame {
	public LuaThread thread;
	
	public LuaCallFrame(LuaThread thread) {
		this.thread = thread;
	}
	
	public LuaClosure closure;
	public int pc;

	public int localBase;
	int returnBase;
	public int nArguments;

	boolean fromLua;
	public boolean insideCoroutine;
	
	boolean restoreTop;
	
	public void set(int index, Object o) {
		thread.objectStack[localBase + index] = o;
	}

	public Object get(int index) {
		return thread.objectStack[localBase + index];
	}

	public void push(Object x) {
		int top = getTop();
		setTop(top + 1);
		set(top, x);
	}

	public void push(Object x, Object y) {
		int top = getTop();
		setTop(top + 2);
		set(top, x);
		set(top + 1, y);
	}
	
	public final void stackCopy(int startIndex, int destIndex, int len) {
		thread.stackCopy(localBase + startIndex, localBase + destIndex, len);
	}
	
	public void stackClear(int startIndex, int endIndex) {
		for (; startIndex <= endIndex; startIndex++) {
			thread.objectStack[localBase + startIndex] = null;
		}
	}
	
	public void setTop(int index) {
		thread.setTop(localBase + index);
	}

	public void closeUpvalues(int a) {
		thread.closeUpvalues(localBase + a);
	}

	public UpValue findUpvalue(int b) {
		return thread.findUpvalue(localBase + b);
	}

	public int getTop() {
		return thread.getTop() - localBase;
	}

	public void init() {
		pc = 0;
		
		if (closure != null) {
			if (closure.prototype.isVararg) {
				localBase += nArguments;
				
				setTop(closure.prototype.maxStacksize);
				int toCopy = Math.min(nArguments, closure.prototype.numParams);
				stackCopy(-nArguments, 0, toCopy);
			} else {
				setTop(closure.prototype.maxStacksize);
			}
		}
	}

	public void pushVarargs(int index, int n) {
		int nParams = closure.prototype.numParams;
		int nVarargs = nArguments - nParams;
		if (nVarargs < 0) nVarargs = 0;
		if (n == -1) n = nVarargs;
		if (nVarargs > n) nVarargs = n;
		
		setTop(index + n);
		
		stackCopy(-nArguments + nParams, index, nVarargs);
		
		int numNils = n - nVarargs;
		if (numNils > 0) {
			stackClear(index + nVarargs, index + n - 1);
		}
	}
	
	public LuaTable getEnvironment() {
		if (closure != null) {
			return closure.env;
		}
		return thread.state.environment;
	}
}
