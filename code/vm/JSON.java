import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Tiny JSON parser/printer
 * with safe utf8 handling:
 * "objects" maps to Hashtables,
 * "arrays" maps to Vector/Stack,
 * "strings" maps to String, and
 * "numbers" maps to Double
 *  (or Integer depending on comments).
 */
class JSON {
	/**
	 * Read data in JavaScript Object Notation
	 * from an InputStream into a Java object.
	 * It assumes valid JSON and utf-8, and the result
	 * is undefined if that is not the case 
	 */
	static Object parse(InputStream is) throws IOException {
		Stack r = new Stack(); /* r contains the result */
		int c = is.read(); /* c is the current character */
		for(;;) {
			StringBuffer s = new StringBuffer();

			/* Atom: null */
			if(c == 'n') {
				is.skip(3);
				c = is.read();
				r.push(null);

			/* Atom: true */
			} else if(c == 't') {
				is.skip(3);
				c = is.read();
				r.push(new Boolean(true));

			/* Atom: false */
			} else if(c == 'f') {
				is.skip(4);
				c = is.read();
				r.push(new Boolean(false));

			/* Strings */
			} else if(c == '"' || c == '\'') {
				int v = c;
				c = is.read();
				while(c != v) {
					/* 
					 * Handle escape chars
					 *
					 * can be commented out
					 */
					if(c == '\\') {
						c = is.read();
						switch(c) {
							case 'b':
								c = '\b';
								break;
							case 'f':
								c = '\f';
								break;
							case 'n':
								c = '\n';
								break;
							case 'r':
								c = '\r';
								break;
							case 't':
								c = '\t';
								break;
							case 'u':
								c = ((is.read() - 1) & 15);
								c = c * 16 + ((is.read() - 1) & 15);
								c = c * 16 + ((is.read() - 1) & 15);
								c = c * 16 + ((is.read() - 1) & 15);
								break;
						}
						s.append((char) c);
						c = is.read();
					} else
					/* end of escape handling */

					/*
					 * Unicode/utf8 handling 
					 * Only supports up to char 65536
					 * which makes sense in java
					 * Can be commented out 
					 */
					if(c > 128) {
						int t;

						// remove length indicating bits
						//   this only works if the encoded
						//   unicode symbol is less than 65536
						//   which is also the size of a char
						//   in java
						t = c & 0x1f; 

						c = is.read();
						// continue reading bytes
						// as long as they claim to be
						// a part of the current symbol
						while((c & 0xc0) == 0x80) {
							t = (t << 6) + c - 128;
							c = is.read();
						}
						s.append((char)t);
					} else
					/* end of unicode handling */
					
					{
						s.append((char) c);
						c = is.read();
					}


				};
				r.push(new String(s.toString()));
				c = is.read();

			/* Numbers */
			} else if(c == '-' || c >= '0' && c <= '9') {
				/*
				 * Comment either floating point or integer section out.
				 */
				do {
					s.append((char)c);
					c = is.read();
				} while(c >= '0' && c <= '9');
				/*
				 * Start of floating point section
				 */
				/*
				if(c == '.') {
					do {
						s.append((char)c);
						c = is.read();
					} while(c >= '0' && c <= '9');
				}

				if(c == 'e' || c == 'E') {
					s.append((char)c);
					c = is.read();
					do {
						s.append((char)c);
						c = is.read();
					} while(c >= '0' && c <= '9');
				}
				r.push(new Double(Double.parseDouble(s.toString())));
				*/
				/*
				 * End of floating point section
				 * Beginning of integer section
				 */
				r.push(new Integer(Integer.parseInt(s.toString())));
				/*
				 * End of integer section
				 */
			/* Beginning of array or object */
			} else if(c == '{' || c == '[') {
				/* hack: we use the result itself 
				 * to indicate that we have a
				 * left parenthesis
				 */
				r.push(r);
				c = is.read();

			/* Array end */
			} else if(c == ']') {
				int i;
				for(i = r.size() - 1; r.elementAt(i) != r; i--) {
				}
				Stack array= new Stack();
				for(int j = i+1; j < r.size(); j++) {
					array.push(r.elementAt(j));
				}
				r.setElementAt(array, i);
				r.setSize(i+1);
				c = is.read();

			/* Object end */
			} else if(c == '}') {
				int i;
				for(i = r.size() - 1; r.elementAt(i) != r; i--) {
				}
				Hashtable h = new Hashtable();
				for(int j = i+1; j < r.size(); j+= 2) {
					h.put(r.elementAt(j), r.elementAt(j+1));
				}
				r.setElementAt(h, i);
				r.setSize(i+1);
				c = is.read();

			/* Whitespace, separator, anything else */
			} else {
				c = is.read();
			}

			/* When we have read excactly one element, return */
			if(r.size() == 1 && r.elementAt(0) != r) {
				return r.elementAt(0);
			}
		}
	}

	/**
	 * Serialise Vector, Hashtable, Integer, (Double), and String 
	 * as JSON. Must be noncyclic.
	 */
	static void print(Object o, OutputStream s) throws IOException {

		if(o == null) {
			s.write("null".getBytes());

		} else if(o instanceof Boolean) {
			s.write(o.toString().toLowerCase().getBytes());

		} else if(o instanceof String) {
			s.write('"');
			/*
			 * Unicode encoding
			 *
			 * comments can be used to choose between
			 *   - escaped encoding, safest
			 *   - manual unicode, work on all platforms
			 *   - default encoding, might work depending on platform
			 */
			for(int i=0;i<((String)o).length();i++) {
				char c = ((String)o).charAt(i);
				/* escape encoding */
			 	if(c < 32 || c >= 128) {
					s.write('\\');
					s.write('u');
					s.write((Integer.toHexString(c)).getBytes());
				} else {
					s.write(c);
				}
				/* utf8-encoding */
				/*
				if(c < 128) {
					s.write(c);
				} else if(c < 1920 ) {
					s.write(0xc0 | (c >>> 6));
					s.write((c & 0x3f) | 128);
				} else {
					s.write(0xe0 | (c >>> 12));
					s.write(((c>>6) & 0x3f) | 128);
					s.write((c & 0x3f) | 128);
				}
				*/

			}
			/* 
			 * alternative implementation, 
			 * with default charset
			 */
			/*
			s.write(((String)o).getBytes());
			*/
			/* 
			 * end of encoding
			 */
			s.write('"');

		} else if(o instanceof Integer) {
			s.write((new String(o.toString())).getBytes());
		/*
		 * Print a floating point number
		 * can be commented out, in case
		 * the platform does not support
		 * floats
		 */
			/*
		} else if(o instanceof Double) {
			s.write((new String(o.toString())).getBytes());
			*/
		/* end of float */

		} else if(o instanceof Hashtable) {
			Hashtable h = (Hashtable)o;
			boolean f = false;
			s.write('{');
			for(Enumeration e = h.keys();e.hasMoreElements();) {
				if(f) {
					s.write(',');
				}
				f = true;
				o = e.nextElement();
				print(o, s);
				s.write(':');
				print(h.get(o), s);
			}
			s.write('}');

		} else if(o instanceof Vector) {
			Vector v = (Vector)o;
			boolean f = false;
			s.write('[');
			for(Enumeration e = v.elements();e.hasMoreElements();) {
				if(f) {
					s.write(',');
				}
				f = true;
				o = e.nextElement();
				print(o, s);
			}
			s.write(']');
		}
	}
}
