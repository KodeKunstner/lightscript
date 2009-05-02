// Floating point emulation for CLDC 1.0
// Copyright 2009 Rasmus Jensen
// Released under GPL
// The representation should be:
// | sign bit | 31 bit exponent | 0 | one-bit or zero-bit |30 bit fractional |
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

    private static final long SIGN_MASK = 0x8000000000000000L;
    private static final long ABS = ~SIGN_MASK;
    private static final long FRAC = 0x000FFFFFFFFFFFFFL;
    private static final long IMPLICIT_ONE = 0x0010000000000000L;



    static long fromInt(int i) {
        long result = 0;
        if(i == 0) {
            return 0;
        }
        if(i < 0) {
            i = -i;
            result |= SIGN_MASK;
        }
        int exponent = 0x41e;
        do {
            i <<= 1;
            --exponent;
        } while(i > 0);
        result |= (long)exponent << 52;
        i &= 0x7fffffff;
        result |= (long)i << 52 - 31;
        
        return result;
    }
    public static void main(String[] args) {
        System.out.println(toInt(Double.doubleToLongBits(-12345)));
        System.out.println(toInt(Double.doubleToLongBits(12345)));
        //System.out.println(Double.longBitsToDouble(fromInt(-12345)));
    }

    static int toInt(long l) {
        int exp = 0x7ff & (int)(l >> 52L);
        boolean negative =  (l & SIGN_MASK) != 0;
        if(negative) {
            System.out.println("NEGATIVE");
        }
        exp = - exp + 0x3ff + 52; 
        //System.out.println(exp);
        l = ((l & FRAC) | IMPLICIT_ONE) >>> exp;
        return negative
                ? -(int) l
                :  (int) l;
    }
    static long fromString(String s) {
        return 0;
    }
    static boolean isIntegral(long l) {
        return false;
    }
    static long neg(long l) {
        return l ^ SIGN_MASK;
    }
    static long sub(long a, long b) {
        return add(a, b^SIGN_MASK);
    }
    static long add(long a, long b) {
        boolean subtract = ((a^b)&SIGN_MASK) != 0;
        if((a&ABS) < (b&ABS)) {
            long t=a;a=b;b=t;
        }
        long SIGN = a & SIGN_MASK;
        int exp = 0x7ff & (int)(a >> 52L);
                       // expa - expb
        int deltaexp = exp - 0x7ff & (int)(b >> 52L);


        return 0;
    }
    static long mul(long a, long b) {
        return 0;
    }
    static long div(long a, long b) {
        return 0;
    }
    static long rem(long a, long b) {
        return 0;
    }
    static boolean less(long a, long b) {
        // both are negative
        if((a&b&SIGN_MASK) != 0) {
            return a > b;
        } else {
            return a < b;
        }
    }
    static boolean lessEq(long a, long b) {
        // both are negative
        if((a&b&SIGN_MASK) != 0) {
            return a >= b;
        } else {
            return a <= b;
        }
    }
}
