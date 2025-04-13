package mathUtils.isPrime;

import java.math.BigInteger;

import static mathUtils.isPrime.FermatTest.uniformRandom;

public class SolovayStrassenTest {

    public static boolean solovayStrassenTest(BigInteger candidate, int rounds) {
        if (candidate.compareTo(BigInteger.valueOf(3)) <= 0) {
            return true;
        }
        BigInteger exponent = candidate.subtract(BigInteger.ONE).divide(BigInteger.TWO);
        for (int i = 0; i < rounds; i++) {
            BigInteger a = uniformRandom(BigInteger.TWO, candidate.subtract(BigInteger.ONE));
            if (!a.gcd(candidate).equals(BigInteger.ONE)) {
                return false;
            }
            BigInteger modExp = a.modPow(exponent, candidate);
            int jacobi = jacobi(a, candidate);
            BigInteger jacobiMod = (jacobi == -1) ? candidate.subtract(BigInteger.ONE) : BigInteger.valueOf(jacobi);
            if (!modExp.equals(jacobiMod.mod(candidate))) {
                return false;
            }
        }
        return true;
    }

    public static int jacobi(BigInteger a, BigInteger n) {
        if (n.signum() <= 0 || !n.testBit(0)) {
            throw new IllegalArgumentException("n должно быть положительным нечётным числом.");
        }
        a = a.mod(n);
        int result = 1;
        while (a.compareTo(BigInteger.ZERO) != 0) {
            while (!a.testBit(0)) {
                a = a.shiftRight(1);
                BigInteger nMod8 = n.mod(BigInteger.valueOf(8));
                if (nMod8.equals(BigInteger.valueOf(3)) || nMod8.equals(BigInteger.valueOf(5))) {
                    result = -result;
                }
            }
            BigInteger temp = a;
            a = n;
            n = temp;
            if (a.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3)) &&
                    n.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                result = -result;
            }
            a = a.mod(n);
        }
        return n.equals(BigInteger.ONE) ? result : 0;
    }
}
