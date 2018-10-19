package aobf;

import java.util.BitSet;

import static java.lang.Math.max;

public abstract class Function {
    public int n;
    public double apply(BitSet bs) {
        int[] x = new int[n];
        for (int i = 0; i<n; i++) {
            x[i] = bs.get(i)? -1: 1;
        }
        return apply(x);
    }

    public double apply(int[] x) {
        return 1;
    }

    public void next(BitSet S){
        for (int i = 0; i<n; i++) {
            if (S.get(i)) {
                S.set(i, false);
            } else {
                S.set(i, true);
                break;
            }
        }
    }

    public double chi(BitSet S, BitSet T) {
        BitSet U = new BitSet();
        U.or(S);
        U.and(T);
        return ((U.cardinality() & 1) == 1)? -1: 1;
    }

    public double chi(BitSet S, BitSet T, BitSet U) {
        U.clear();
        U.or(S);
        U.and(T);
        return ((U.cardinality() & 1) == 1)? -1: 1;
    }

    public double fourierCoef(BitSet S) {
        double acc = 0.0;
        double m = Math.pow(2, n);
        BitSet T = new BitSet();
        BitSet U = new BitSet();
        acc += apply(T);
        next(T);
        while (!T.isEmpty()) {
            acc += apply(T) * chi(S, T, U);
            next(T);
        }
        return acc / m;
    }

    public double[] fourierSpectral() {
        int m = 1 << n;
        double[] coef = new double[m];
        double[] f = new double[m];
        BitSet S = new BitSet();
        BitSet T = new BitSet();
        BitSet U = new BitSet();

        for (int i = 0; i<m; i++) {
            f[i] = apply(T);
            next(T);
        }

        for (int i = 0; i<m; i++) {
            double acc = 0.0;
            T.clear();
            for (int j = 0; j<m; j++) {
                acc += f[j] * chi(S, T, U);
                next(T);
            }
            coef[i] = acc / m;
            next(S);
        }
        return coef;
    }

    public static BitSet indicesToBitSet(int ... x) {
        BitSet S = new BitSet();
        for (int i = 0; i<x.length; i++) {
            S.set(x[i]);
        }
        return S;
    }

    public static BitSet longToBitSet(long x) {
        int i = 0;
        BitSet S = new BitSet();
        while (x != 0) {
            if ((x & 1) == 1) {
                S.set(i);
            }
            i++;
            x >>>= 1;
        }
        return S;
    }

    public static long bitSetToLong(BitSet S) {
        return S.toLongArray()[0];
    }

    public static void main(String[] args) {
        Function IP = new Function() {
            {
                n = 6;
            }

            @Override
            public double apply(int[] x) {
                int m = n/2;
                int a = 0;
                for (int i = 0; i<m; i++) {
                    a += (1 - max(x[i], x[i+m])) / 2;
                }
                return (a % 2) == 1? -1: 1;
            }
        };

        Function f = new Function() {
            {
                n = 7;
            }

            @Override
            public double apply(int[] x) {
                return x[0]+x[1]+x[2]+x[3]+x[4]+x[5]+x[6]>0? 1: -1;
            }
        };

        double[] coef = f.fourierSpectral();
        for (int i = 0; i<(1<<f.n); i++) {
            System.out.printf("%s, %f\n", Integer.toBinaryString(i), coef[i] * (1<<f.n));
        }
        //System.out.println(Arrays.toString(f.fourierSpectral()));
    }
}
