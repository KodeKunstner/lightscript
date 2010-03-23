/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.solsort.lightscript;

/**
 *
 * @author rje
 */
class FixedPoint {

    public long val;

    public FixedPoint(int i) {
        val = (long) i << 32;
    }

    public FixedPoint(long l) {
        val = l;
    }

    public String toString() {
        long l = val;
        StringBuffer sb = new StringBuffer();
        if (l < 0) {
            sb.append('-');
            l = -l;
        }
        sb.append((int) (l >> 32));
        l = (l & 0xffffffffL) + (1L << 31) / 100000;
        sb.append('.');
        for (int i = 0; i < 5; i++) {
            l *= 10;
            sb.append(l >>> 32);
            l = l & 0xffffffffL;
        }
        return sb.toString();
    }
}
class FixedPointOps {
    /*`\subsection{Arithmetics}'*/
    //<editor-fold>

    static int toInt(Object o) {
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else if (o instanceof FixedPoint) {
            return (int) (((FixedPoint) o).val >> 32);
        } else /* String */ {
            return Integer.parseInt((String) o);
        }
    }

    static long toFp(Object o) {
        if (o instanceof FixedPoint) {
            return ((FixedPoint) o).val;
            //} else if(o instanceof Integer) {
        } else { // TODO: maybe add support for string to fp
            return (long) ((Integer) o).intValue() << 32;
        }
    }

    static Object toNumObj(long d) {
        int i = (int) d;
        if (-32 < i && i < 32) {
            return new Integer((int) (d >> 32));
        } else {
            return new FixedPoint(d);
        }
    }

    static Object fpNeg(long a) {
        return toNumObj(-a);
    }

    static Object fpAdd(long a, long b) {
        return toNumObj(a + b);
    }

    static Object fpSub(long a, long b) {
        return toNumObj(a - b);
    }

    static Object fpMul(long a, long b) {
        long t = a & 0xffffffffL;
        long result = (t * (b & 0xffffffffL));
        result = (result >>> 32) + ((result >>> 31) & 1);
        result += t * (b >>> 32);
        result += b * (a >>> 32);
        return toNumObj(result);
    }

    static Object fpDiv(long a, long b) {
        boolean neg = false;
        if (a == 0) {
            return toNumObj(0);
        }
        if (a < 0) {
            neg = !neg;
            a = -a;
        }

        int shift = 33;
        do {
            a <<= 1;
            shift -= 1;
        } while (a > 0 && shift > 0);
        a >>>= 1;

        b = b >> shift;

        if (b == 0) {
            a = ~0L >>> 1;
        } else {
            a /= b;
        }
        return toNumObj(neg ? -a : a);
    }

    static Object fpRem(long a, long b) {
        return toNumObj(a % b);
    }

    static boolean fpLess(long a, long b) {
        return a < b;
    }

    static boolean fpLessEq(long a, long b) {
        return a <= b;
    }

    //</editor-fold>

}