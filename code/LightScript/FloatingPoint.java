// Floating point emulation for CLDC 1.0
// Copyright 2009 Rasmus Jensen
// Released under GPL
// The representation should be:
// | 24 bit exponent | 7 bit padding| sign bit| 0 | one-bit or zero-bit |30 bit fractional |
// 31 bit number part with bit 31 as one
//
// CHANGE from:
// The representation is like IEEE-754 double,
// but implementation typically only use 32 significant bits
// for performance, and does not allow negative zero.
// Result of overflow is undefined
class FloatingPoint {
    long val;
    FloatingPoint(long l) {
        val = l;
    }
    FloatingPoint(int i) {
        val = fromInt(i);
    }

    private static final long SIGN = 1L << 32;;
    private static final long ABS = ~SIGN;

    static long fromInt(int i) {
        int exponent = 1;
        boolean negative;
        if(i == 0) {
            return 0;
        }
        negative = i < 0;
        if(negative) {
            i = -i;
        }
        do {
            i <<= 1;
            --exponent;
        } while(i > 0);
        return ((long)exponent << 40) 
             + (i >>> 1) 
             + (negative ? SIGN : 0);
    }

    public static void main(String[] args) {
        //System.out.println(Integer.toString(toInt(fromInt(123456)), 10));
        //System.out.println(Long.toString(fromString("1"), 16));
        //System.out.println(Integer.toString((int)fromString("256.25"), 16));
        for(int i = 1; i <= 20; i++) {
            System.out.println(i);
            System.out.println(toString(div(fromInt(10), fromInt(i))));
        }
        //System.out.println(Double.longBitsToDouble(fromInt(-12345)));
    }

    public static int toInt(int i) {
        return i;
    }
    public static int toInt(long l) {
        int exponent = (int)(l >> 40);
        boolean negative =  (l & SIGN) != 0;
        int i = (int) l;
        while(exponent < 0) {
            i >>= 1;
            ++exponent;
        } 
        return negative
                ? -(int) i
                :  (int) i;
    }

    private static char charat(String s, int index) {
        if(index >= s.length()) {
            return '\000';
        } else {
            return s.charAt(index);
        }
    }
    public static long fromString(String s) {
        int pos = 0;
        long l = 0;
        long redux = 1;
        int exp = -31;
        char c = charat(s, pos);
        boolean neg = (c == '-');
        if(neg) {
            ++pos;
            c = charat(s, pos);
        }

        // read integer
        while('0' <= c  && c <= '9') {
            l = l * 10 + c - '0';
            ++pos;
            c = charat(s, pos);
        }

        // skip '.'
        ++pos;
        c = charat(s, pos);

        // read fractional
        while('0' <= c  && c <= '9') {
            l = l * 10 + c - '0';
            redux *= 10;
            ++pos;
            c = charat(s, pos);
        }

        if(l == 0) {
            return 0;
        }
        while(l > 0 && redux > 0) {
            l <<= 1;
            redux <<= 1;
        }
        l >>>= 1;
        redux >>>= 32;

        l /= redux;
        while(l >= (1L << 31)) {
            exp++;
            l >>= 1;
        }
        while(l < (1 << 30)) {
            exp--;
            l <<= 1;
        }

        return ((long)exp << 40) 
             + l
             + (neg ? SIGN : 0);
    }
    public String toString() {
        return toString(val);
    }
    static String toString(long l) {
        StringBuffer sb = new StringBuffer();
        int exponent = (int)(l >> 40);
        boolean negative =  (l & SIGN) != 0;
        int i = (int)l;
        long frac = (long) i << 32;
        if(i == 0) {
            return "0";
        }

        if(negative) {
            sb.append('-');
        }

        while(exponent < 0) {
            i >>= 1;
            frac >>= 1;
            ++exponent;
        } 
        while(exponent > 0) {
            i <<= 1;
            frac <<= 1;
            --exponent;
        } 
        frac = frac & 0xffffffffL;

        long lifo = 0;
        while(i > 0) {
            lifo = (lifo << 4) | (i % 10);
            i /= 10;
        }
        if(lifo > 0) { 
            do {
                sb.append((char) ((lifo & 15) + '0'));
                lifo >>= 4;
            } while(lifo > 0);
        } else {
            sb.append('0');
        }

        int fracSize = 5;

        sb.append('.');
        do {
            frac = frac & 0xffffffffL;
            frac = frac & 0xffffffffL;
            frac *= 10;
            sb.append((char)('0' + (frac >> 32)));
            --fracSize;
        } while(fracSize >= 0 && frac != 0);

        return sb.toString();
    }
    static boolean isIntegral(long l) {
        return false;
    }
    static long neg(long l) {
        return l ^ SIGN;
    }
    static long sub(long a, long b) {
        return add(a, b^SIGN);
    }
    static long add(long a, long b) {
        boolean subtract = ((a^b)&SIGN) != 0;
        if((a&ABS) < (b&ABS)) {
            long t=a;a=b;b=t;
        }
        long sign = a & SIGN;
        int exp = 0x7ff & (int)(a >> 52L);
                       // expa - expb
        int deltaexp = exp - 0x7ff & (int)(b >> 52L);


        return 0;
    }
    static long mul(long a, long b) {
        return 0;
    }
    static long div(long a, long b) {
        // division of integer part
        int deltaExp = -32;
        long result = (int)a;
        result <<= 31;
        result /= (int)b;
        while((int)result > 0) {
            result <<= 1;
            --deltaExp;
        }
        result >>>= 1;
        // subtract exponents;
        result |= (deltaExp + (a >> 40) - (b >> 40)) << 40;
        result |= a ^ b & SIGN;
        return result;
    }
    static long rem(long a, long b) {
        return 0;
    }
    static boolean less(long a, long b) {
        // both are negative
        if((a&b&SIGN) != 0) {
            return a > b;
        } else {
            return a < b;
        }
    }
    static boolean lessEq(long a, long b) {
        // both are negative
        if((a&b&SIGN) != 0) {
            return a >= b;
        } else {
            return a <= b;
        }
    }
}
