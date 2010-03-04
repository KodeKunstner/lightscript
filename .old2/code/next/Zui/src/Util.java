/*
 import java.util.Random;

public class Util {
    public static Random random_generator = new Random();

    public static int hilbert(int hilbert) {
        int x = 0;
        int y = 0;
        int lvl = 1;
        for(int i = 0; i < 16; i++) {
            int pos = hilbert % 4;
            if(pos < 2) {
                if(pos == 0) {
                    int t = x; x = y; y = t;
                } else { //1
                    y += lvl;
                }
            } else {
                if(pos == 2) {
                    x += lvl;
                    y += lvl;
                } else { // 3
                    int t = lvl - y - 1;
                    y = lvl - x - 1;
                    x = t + lvl;
                }
            }
            hilbert = hilbert >>> 2;
            lvl <<= 1;
        }
        return (x << 16) + y;
    }
    public static int random(int n) {
        return (random_generator.nextInt() & 0x7ffffff) % n;
    }
    
    public static void qsort(Object arr[], Order o) {
        qsort(arr, 0, arr.length - 1, o);
    }
    public static void qsort(Object arr[], int first, int last, Order o) {
        while (first < last) {
            int l = first;
            int r = last;
            Object pivot = arr[(l + r) / 2];
            arr[(l + r) / 2] = arr[r];
            arr[r] = pivot;
            while (l < r) {
                while (o.leq(arr[l], pivot) && l < r) {
                    l++;
                }
                if (l < r) {
                    arr[r] = arr[l];
                    r--;
                }
                while (o.leq(pivot, arr[r]) && l < r) {
                    r--;
                }
                if (l < r) {
                    arr[l] = arr[r];
                    l++;
                }
            }
            arr[r] = pivot;
            qsort(arr, l + 1, last, o);
            last = l - 1;
        }
    }
    public static boolean isSorted(Object[] arr, Order o) {
        for(int i = 1; i < arr.length;i++) {
            if(!o.leq(arr[i-1], arr[i])) {
                return false;
            }
        }
        return true;
    }
        
    public static int unique(Object[] arr, Order o) {
        if(!isSorted(arr, o)) {
            qsort(arr, o);
        }
        int dst = 0;
        for(int src = 1; src < arr.length; ++src) {
            if(arr != null && !arr[src].equals(arr[dst])) {
                ++dst;
                arr[dst] = arr[src];
            }
        }
        ++dst;
        for(int i = dst; i < arr.length; i++) {
            arr[i] = null;
        }
        return dst;
    }
}
*/